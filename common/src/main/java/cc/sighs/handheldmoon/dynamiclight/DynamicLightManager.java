package cc.sighs.handheldmoon.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior.Bounds;
import cc.sighs.handheldmoon.util.AsyncLightExecutor;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/** Minimal client dynamic-light registry and section invalidation engine. */
public final class DynamicLightManager {
    private static final int MAX_SECTION_DIRTY_MARKS_PER_TICK = 64;
    private static final int FAIR_SECTION_DIRTY_MARKS_PER_TICK = 4;
    private static final int ASYNC_INDEX_SOURCE_THRESHOLD = 64;
    private static final int BATCH_REQUEST_THRESHOLD = 24;
    private static final int MAX_BATCH_CACHE_ENTRIES = 1024;
    private static final int MAX_PENDING_BATCH_TASKS = 8;
    private static final int QUERY_CACHE_SLOTS_PER_THREAD = 16_384;
    private static final Set<DynamicLightBehavior> SOURCES = ConcurrentHashMap.newKeySet();
    private static final Object DIRTY_LOCK = new Object();
    private static final LongLinkedOpenHashSet DIRTY_SECTIONS = new LongLinkedOpenHashSet();
    private static final AtomicLong SECTION_INDEX_REVISION = new AtomicLong();
    private static final AtomicLong INDEX_BUILD_GENERATION = new AtomicLong();
    private static final Object INDEX_BUILD_LOCK = new Object();
    private static final ThreadLocal<QueryCache> QUERY_CACHES = ThreadLocal.withInitial(QueryCache::new);
    /**
     * Uses the already-existing section bucket as the identity key. Creating a
     * record key for every block-light query showed up as the largest
     * HandheldMoon allocation in the latest profile.
     */
    private static final ConcurrentHashMap<SectionBucket, BatchEntry> BATCH_LIGHT_CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger PENDING_BATCH_TASKS = new AtomicInteger();

    private static Future<?> pendingIndexBuild;

    private static volatile DynamicLightBehavior[] snapshot = new DynamicLightBehavior[0];
    private static volatile SectionIndex sectionIndex = emptySectionIndex();
    private static ClientLevel currentLevel;

    private DynamicLightManager() {
    }

    /** Returns true when the client level changed and source owners must repopulate the registry. */
    public static boolean syncLevel(Minecraft minecraft) {
        ClientLevel next = minecraft.level;
        if (next == currentLevel) {
            return false;
        }
        currentLevel = next;
        SOURCES.clear();
        snapshot = new DynamicLightBehavior[0];
        sectionIndex = emptySectionIndex();
        invalidateAsyncWork();
        BATCH_LIGHT_CACHE.clear();
        synchronized (DIRTY_LOCK) {
            DIRTY_SECTIONS.clear();
        }
        if (next != null) {
            minecraft.levelRenderer.allChanged();
        }
        return true;
    }

    public static void add(DynamicLightBehavior behavior) {
        if (SOURCES.add(behavior)) {
            invalidateAsyncIndexBuild();
            refreshSnapshot();
            if (!appendToSectionIndex(behavior)) {
                rebuildSectionIndex();
            }
            IndexedSource indexed = sectionIndex.byBehavior.get(behavior);
            schedule(indexed != null ? indexed.bounds : behavior.getBounds());
        }
    }

    public static void remove(DynamicLightBehavior behavior) {
        if (SOURCES.remove(behavior)) {
            invalidateAsyncIndexBuild();
            IndexedSource indexed = sectionIndex.byBehavior.get(behavior);
            Bounds bounds = indexed != null ? indexed.bounds : behavior.getBounds();
            schedule(bounds);
            refreshSnapshot();
            if (!removeFromSectionIndex(behavior)) {
                rebuildSectionIndex();
            }
        }
    }

    public static void tick(Minecraft minecraft) {
        DynamicLightBehavior[] current = snapshot;
        boolean removedAny = false;
        boolean requiresFullIndexRebuild = false;
        boolean anyChanged = false;
        List<CoverageChange> coverageChanges = null;
        for (DynamicLightBehavior behavior : current) {
            Bounds before = behavior.getBounds();
            boolean changed = behavior.hasChanged();
            if (behavior.isRemoved()) {
                if (SOURCES.remove(behavior)) {
                    schedule(before);
                    removedAny = true;
                    anyChanged = true;
                }
                continue;
            }
            if (changed) {
                anyChanged = true;
                schedule(before);
                Bounds after = behavior.getBounds();
                schedule(after);
                SectionIndex index = sectionIndex;
                IndexedSource indexed = index.byBehavior.get(behavior);
                if (indexed == null) {
                    requiresFullIndexRebuild = true;
                    continue;
                }
                if (sameSectionCoverage(before, after)) {
                    // Keep the candidate section membership and refresh the
                    // exact block bounds in place for sub-section movement.
                    indexed.update(after, behavior.getBatchLightSnapshot());
                    invalidateSectionRevisions(after);
                } else {
                    if (coverageChanges == null) {
                        coverageChanges = new java.util.ArrayList<>();
                    }
                    coverageChanges.add(new CoverageChange(behavior, before, after, indexed));
                }
            }
        }
        if (removedAny) {
            refreshSnapshot();
        }
        if (anyChanged) {
            invalidateAsyncIndexBuild();
        }
        if (removedAny || requiresFullIndexRebuild) {
            rebuildSectionIndex(minecraft, true);
        } else if (coverageChanges != null && !coverageChanges.isEmpty()) {
            rebuildSectionIndexIncremental(coverageChanges);
        }
        rebuildDirtySections(minecraft);
    }

    public static double getLightLevel(BlockPos pos) {
        return getLightLevel(pos.getX(), pos.getY(), pos.getZ());
    }

    public static double getLightLevel(int blockX, int blockY, int blockZ) {
        SectionIndex index = sectionIndex;
        long sectionKey = sectionKey(
                SectionPos.blockToSectionCoord(blockX),
                SectionPos.blockToSectionCoord(blockY),
                SectionPos.blockToSectionCoord(blockZ)
        );
        SectionBucket bucket = index.sources.get(sectionKey);
        if (bucket == null) {
            return 0.0;
        }
        IndexedSource[] candidates = bucket.sources;

        long blockKey = BlockPos.asLong(blockX, blockY, blockZ);
        QueryCache cache = QUERY_CACHES.get();
        long revision = bucket.revision;
        float cached = cache.get(revision, blockKey);
        if (!Float.isNaN(cached)) {
            return cached;
        }

        double light = 0.0;
        BatchLightValues batchValues = getBatchLightValues(
                sectionKey, bucket, revision, blockX, blockY, blockZ
        );
        if (batchValues != null) {
            light = batchValues.value(blockX, blockY, blockZ);
        }
        for (IndexedSource source : candidates) {
            if (batchValues != null && source.batchSource != null) {
                continue;
            }
            Bounds bounds = source.bounds;
            if (blockX < bounds.minX() || blockX > bounds.maxX()
                    || blockY < bounds.minY() || blockY > bounds.maxY()
                    || blockZ < bounds.minZ() || blockZ > bounds.maxZ()) {
                continue;
            }
            light = Math.max(light, source.behavior.lightAt(blockX, blockY, blockZ, 1.0));
            if (light >= 15.0) {
                light = 15.0;
                break;
            }
        }
        light = Math.max(0.0, light);
        if (index == sectionIndex && bucket.revision == revision) {
            cache.put(revision, blockKey, (float) light);
        }
        return light;
    }

    private static BatchLightValues getBatchLightValues(
            long sectionKey,
            SectionBucket bucket,
            long revision,
            int blockX,
            int blockY,
            int blockZ
    ) {
        if (!hasBatchSource(bucket.sources)) {
            return null;
        }
        BatchEntry entry = BATCH_LIGHT_CACHE.get(bucket);
        if (entry == null || entry.revision != revision) {
            synchronized (bucket) {
                entry = BATCH_LIGHT_CACHE.get(bucket);
                if (entry == null || entry.revision != revision) {
                    if (BATCH_LIGHT_CACHE.size() >= MAX_BATCH_CACHE_ENTRIES) {
                        BATCH_LIGHT_CACHE.clear();
                    }
                    entry = new BatchEntry(revision);
                    BATCH_LIGHT_CACHE.put(bucket, entry);
                }
            }
        }

        int requests = entry.requests.incrementAndGet();
        if (requests >= BATCH_REQUEST_THRESHOLD && entry.future == null && !entry.disabled) {
            synchronized (entry) {
                if (entry.future == null && !entry.disabled) {
                    BatchSource[] snapshots = batchSources(bucket.sources);
                    if (snapshots.length > 0 && PENDING_BATCH_TASKS.incrementAndGet() <= MAX_PENDING_BATCH_TASKS) {
                        try {
                            entry.future = CompletableFuture
                                    .supplyAsync(
                                            () -> computeBatchLightValues(sectionKey, snapshots),
                                            AsyncLightExecutor.executor()
                                    )
                                    .whenComplete((ignored, error) -> PENDING_BATCH_TASKS.decrementAndGet());
                        } catch (RuntimeException rejected) {
                            entry.disabled = true;
                            PENDING_BATCH_TASKS.decrementAndGet();
                        }
                    } else if (snapshots.length > 0) {
                        entry.disabled = true;
                        PENDING_BATCH_TASKS.decrementAndGet();
                    }
                }
            }
        }

        CompletableFuture<BatchLightValues> future = entry.future;
        if (future == null || !future.isDone()) {
            return null;
        }
        try {
            return future.getNow(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean hasBatchSource(IndexedSource[] sources) {
        for (IndexedSource source : sources) {
            if (source.batchSource != null) {
                return true;
            }
        }
        return false;
    }

    private static BatchSource[] batchSources(IndexedSource[] sources) {
        ArrayList<BatchSource> snapshots = new ArrayList<>();
        for (IndexedSource source : sources) {
            BatchSource batchSource = source.batchSource;
            if (batchSource != null) {
                snapshots.add(batchSource);
            }
        }
        return snapshots.toArray(BatchSource[]::new);
    }

    private static BatchLightValues computeBatchLightValues(
            long sectionKey,
            BatchSource[] sources
    ) {
        int sectionX = sectionX(sectionKey);
        int sectionY = sectionY(sectionKey);
        int sectionZ = sectionZ(sectionKey);
        float[] values = new float[16 * 16 * 16];
        int index = 0;
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                for (int localY = 0; localY < 16; localY++) {
                    int blockX = (sectionX << 4) + localX;
                    int blockY = (sectionY << 4) + localY;
                    int blockZ = (sectionZ << 4) + localZ;
                    double light = 0.0;
                    for (BatchSource source : sources) {
                        Bounds bounds = source.bounds;
                        if (blockX < bounds.minX() || blockX > bounds.maxX()
                                || blockY < bounds.minY() || blockY > bounds.maxY()
                                || blockZ < bounds.minZ() || blockZ > bounds.maxZ()) {
                            continue;
                        }
                        light = Math.max(light, source.snapshot.lightAt(blockX, blockY, blockZ));
                        if (light >= 15.0) {
                            light = 15.0;
                            break;
                        }
                    }
                    values[index++] = (float) Math.max(0.0, light);
                }
            }
        }
        return new BatchLightValues(values);
    }

    private static void refreshSnapshot() {
        snapshot = SOURCES.toArray(DynamicLightBehavior[]::new);
    }

    private static boolean appendToSectionIndex(DynamicLightBehavior behavior) {
        SectionIndex current = sectionIndex;
        if (current.byBehavior.containsKey(behavior)) {
            return false;
        }

        Bounds bounds = behavior.getBounds();
        IndexedSource source = new IndexedSource(
                behavior, bounds, behavior.getBatchLightSnapshot()
        );
        Long2ObjectOpenHashMap<SectionBucket> rebuilt = copySources(current.sources);
        IdentityHashMap<DynamicLightBehavior, IndexedSource> byBehavior =
                new IdentityHashMap<>(current.byBehavior);
        byBehavior.put(behavior, source);
        addSourceToSections(rebuilt, source, bounds);
        sectionIndex = new SectionIndex(rebuilt, byBehavior);
        return true;
    }

    private static boolean removeFromSectionIndex(DynamicLightBehavior behavior) {
        SectionIndex current = sectionIndex;
        IndexedSource source = current.byBehavior.get(behavior);
        if (source == null) {
            return false;
        }

        Long2ObjectOpenHashMap<SectionBucket> rebuilt = copySources(current.sources);
        IdentityHashMap<DynamicLightBehavior, IndexedSource> byBehavior =
                new IdentityHashMap<>(current.byBehavior);
        byBehavior.remove(behavior);
        removeSourceFromSections(rebuilt, source, source.bounds);
        sectionIndex = new SectionIndex(rebuilt, byBehavior);
        return true;
    }

    private static void rebuildSectionIndex() {
        rebuildSectionIndex(null, false);
    }

    private static void rebuildSectionIndex(Minecraft minecraft, boolean allowAsync) {
        IndexSourceState[] current = captureIndexSources(snapshot);
        if (allowAsync && minecraft != null && current.length >= ASYNC_INDEX_SOURCE_THRESHOLD) {
            requestAsyncSectionIndex(minecraft, current);
            return;
        }

        invalidateAsyncIndexBuild();
        sectionIndex = buildSectionIndex(current);
    }

    private static IndexSourceState[] captureIndexSources(DynamicLightBehavior[] current) {
        IndexSourceState[] captured = new IndexSourceState[current.length];
        for (int i = 0; i < current.length; i++) {
            DynamicLightBehavior behavior = current[i];
            captured[i] = new IndexSourceState(
                    behavior,
                    behavior.getBounds(),
                    behavior.getBatchLightSnapshot()
            );
        }
        return captured;
    }

    private static SectionIndex buildSectionIndex(IndexSourceState[] current) {
        IndexedSource[] indexed = new IndexedSource[current.length];
        Long2IntOpenHashMap counts = new Long2IntOpenHashMap();
        counts.defaultReturnValue(0);
        for (int sourceIndex = 0; sourceIndex < current.length; sourceIndex++) {
            IndexSourceState state = current[sourceIndex];
            Bounds bounds = state.bounds;
            IndexedSource source = new IndexedSource(state.behavior, bounds, state.batchSnapshot);
            indexed[sourceIndex] = source;
            int minSectionX = SectionPos.blockToSectionCoord(bounds.minX());
            int minSectionY = SectionPos.blockToSectionCoord(bounds.minY());
            int minSectionZ = SectionPos.blockToSectionCoord(bounds.minZ());
            int maxSectionX = SectionPos.blockToSectionCoord(bounds.maxX());
            int maxSectionY = SectionPos.blockToSectionCoord(bounds.maxY());
            int maxSectionZ = SectionPos.blockToSectionCoord(bounds.maxZ());
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
                    for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                        long key = sectionKey(sectionX, sectionY, sectionZ);
                        counts.addTo(key, 1);
                    }
                }
            }
        }

        long revision = nextSectionRevision();
        Long2ObjectOpenHashMap<SectionBucket> rebuilt = new Long2ObjectOpenHashMap<>(counts.size());
        IdentityHashMap<DynamicLightBehavior, IndexedSource> byBehavior = new IdentityHashMap<>(current.length);
        for (IndexedSource source : indexed) {
            byBehavior.put(source.behavior, source);
        }
        for (Long2IntMap.Entry entry : counts.long2IntEntrySet()) {
            rebuilt.put(entry.getLongKey(), new SectionBucket(
                    new IndexedSource[entry.getIntValue()], revision
            ));
        }
        Long2IntOpenHashMap offsets = new Long2IntOpenHashMap(counts.size());
        offsets.defaultReturnValue(0);
        for (IndexedSource source : indexed) {
            Bounds bounds = source.bounds;
            int minSectionX = SectionPos.blockToSectionCoord(bounds.minX());
            int minSectionY = SectionPos.blockToSectionCoord(bounds.minY());
            int minSectionZ = SectionPos.blockToSectionCoord(bounds.minZ());
            int maxSectionX = SectionPos.blockToSectionCoord(bounds.maxX());
            int maxSectionY = SectionPos.blockToSectionCoord(bounds.maxY());
            int maxSectionZ = SectionPos.blockToSectionCoord(bounds.maxZ());
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
                    for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                        long key = sectionKey(sectionX, sectionY, sectionZ);
                        IndexedSource[] sources = rebuilt.get(key).sources;
                        int offset = offsets.get(key);
                        sources[offset] = source;
                        offsets.put(key, offset + 1);
                    }
                }
            }
        }
        rebuilt.trim();
        return new SectionIndex(rebuilt, byBehavior);
    }

    private static void requestAsyncSectionIndex(
            Minecraft minecraft,
            IndexSourceState[] sources
    ) {
        long generation = INDEX_BUILD_GENERATION.incrementAndGet();
        ClientLevel level = currentLevel;
        synchronized (INDEX_BUILD_LOCK) {
            if (pendingIndexBuild != null) {
                pendingIndexBuild.cancel(false);
            }
            CompletableFuture<SectionIndex> build;
            try {
                build = CompletableFuture.supplyAsync(
                        () -> buildSectionIndex(sources), AsyncLightExecutor.executor()
                );
            } catch (RuntimeException rejected) {
                // Keep the currently published index when the executor is
                // unavailable (for example during client shutdown).
                pendingIndexBuild = null;
                return;
            }
            pendingIndexBuild = build;
            build.whenComplete((index, error) -> {
                if (error != null) {
                    synchronized (INDEX_BUILD_LOCK) {
                        if (pendingIndexBuild == build) {
                            pendingIndexBuild = null;
                        }
                    }
                    return;
                }
                minecraft.execute(() -> {
                    synchronized (INDEX_BUILD_LOCK) {
                        if (generation != INDEX_BUILD_GENERATION.get()
                                || level != currentLevel
                                || minecraft.level != level) {
                            if (pendingIndexBuild == build) {
                                pendingIndexBuild = null;
                            }
                            return;
                        }
                        sectionIndex = index;
                        pendingIndexBuild = null;
                    }
                });
            });
        }
    }

    private static void invalidateAsyncIndexBuild() {
        INDEX_BUILD_GENERATION.incrementAndGet();
        synchronized (INDEX_BUILD_LOCK) {
            if (pendingIndexBuild != null) {
                pendingIndexBuild.cancel(false);
                pendingIndexBuild = null;
            }
        }
    }

    private static void invalidateAsyncWork() {
        invalidateAsyncIndexBuild();
    }

    private static void rebuildSectionIndexIncremental(List<CoverageChange> changes) {
        SectionIndex current = sectionIndex;
        Long2ObjectOpenHashMap<SectionBucket> rebuilt = copySources(current.sources);
        IdentityHashMap<DynamicLightBehavior, IndexedSource> byBehavior =
                new IdentityHashMap<>(current.byBehavior);

        for (CoverageChange change : changes) {
            IndexedSource currentSource = byBehavior.get(change.behavior);
            if (currentSource != change.previousSource) {
                // The index changed unexpectedly while collecting updates.
                // Falling back to a complete rebuild keeps publication atomic.
                rebuildSectionIndex();
                return;
            }

            removeSourceFromSections(rebuilt, currentSource, change.before);
            IndexedSource replacement = new IndexedSource(
                    change.behavior,
                    change.after,
                    change.behavior.getBatchLightSnapshot()
            );
            addSourceToSections(rebuilt, replacement, change.after);
            byBehavior.put(change.behavior, replacement);
        }

        rebuilt.trim();
        sectionIndex = new SectionIndex(rebuilt, byBehavior);
    }

    private static Long2ObjectOpenHashMap<SectionBucket> copySources(
            Long2ObjectOpenHashMap<SectionBucket> sources
    ) {
        Long2ObjectOpenHashMap<SectionBucket> copy = new Long2ObjectOpenHashMap<>(sources.size());
        copy.putAll(sources);
        return copy;
    }

    private static void addSourceToSections(
            Long2ObjectOpenHashMap<SectionBucket> sections,
            IndexedSource source,
            Bounds bounds
    ) {
        forEachCoveredSection(bounds, key -> {
            SectionBucket bucket = sections.get(key);
            if (bucket == null) {
                sections.put(key, new SectionBucket(
                        new IndexedSource[]{source}, nextSectionRevision()
                ));
                return;
            }
            IndexedSource[] expanded = Arrays.copyOf(bucket.sources, bucket.sources.length + 1);
            expanded[expanded.length - 1] = source;
            sections.put(key, new SectionBucket(expanded, nextSectionRevision()));
        });
    }

    private static void removeSourceFromSections(
            Long2ObjectOpenHashMap<SectionBucket> sections,
            IndexedSource source,
            Bounds bounds
    ) {
        forEachCoveredSection(bounds, key -> {
            SectionBucket bucket = sections.get(key);
            if (bucket == null) {
                return;
            }
            IndexedSource[] existing = bucket.sources;
            int found = -1;
            for (int i = 0; i < existing.length; i++) {
                if (existing[i] == source) {
                    found = i;
                    break;
                }
            }
            if (found < 0) {
                return;
            }
            if (existing.length == 1) {
                sections.remove(key);
                return;
            }
            IndexedSource[] reduced = new IndexedSource[existing.length - 1];
            System.arraycopy(existing, 0, reduced, 0, found);
            System.arraycopy(existing, found + 1, reduced, found, existing.length - found - 1);
            sections.put(key, new SectionBucket(reduced, nextSectionRevision()));
        });
    }

    private static void invalidateSectionRevisions(Bounds bounds) {
        SectionIndex index = sectionIndex;
        int minSectionX = SectionPos.blockToSectionCoord(bounds.minX());
        int minSectionY = SectionPos.blockToSectionCoord(bounds.minY());
        int minSectionZ = SectionPos.blockToSectionCoord(bounds.minZ());
        int maxSectionX = SectionPos.blockToSectionCoord(bounds.maxX());
        int maxSectionY = SectionPos.blockToSectionCoord(bounds.maxY());
        int maxSectionZ = SectionPos.blockToSectionCoord(bounds.maxZ());
        for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
            for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    SectionBucket bucket = index.sources.get(sectionKey(sectionX, sectionY, sectionZ));
                    if (bucket != null) {
                        bucket.revision = nextSectionRevision();
                    }
                }
            }
        }
    }

    private static void forEachCoveredSection(Bounds bounds, java.util.function.LongConsumer action) {
        int minSectionX = SectionPos.blockToSectionCoord(bounds.minX());
        int minSectionY = SectionPos.blockToSectionCoord(bounds.minY());
        int minSectionZ = SectionPos.blockToSectionCoord(bounds.minZ());
        int maxSectionX = SectionPos.blockToSectionCoord(bounds.maxX());
        int maxSectionY = SectionPos.blockToSectionCoord(bounds.maxY());
        int maxSectionZ = SectionPos.blockToSectionCoord(bounds.maxZ());
        for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
            for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    action.accept(sectionKey(sectionX, sectionY, sectionZ));
                }
            }
        }
    }

    private static long nextSectionRevision() {
        return SECTION_INDEX_REVISION.incrementAndGet();
    }

    private static boolean sameSectionCoverage(Bounds first, Bounds second) {
        return SectionPos.blockToSectionCoord(first.minX()) == SectionPos.blockToSectionCoord(second.minX())
                && SectionPos.blockToSectionCoord(first.minY()) == SectionPos.blockToSectionCoord(second.minY())
                && SectionPos.blockToSectionCoord(first.minZ()) == SectionPos.blockToSectionCoord(second.minZ())
                && SectionPos.blockToSectionCoord(first.maxX()) == SectionPos.blockToSectionCoord(second.maxX())
                && SectionPos.blockToSectionCoord(first.maxY()) == SectionPos.blockToSectionCoord(second.maxY())
                && SectionPos.blockToSectionCoord(first.maxZ()) == SectionPos.blockToSectionCoord(second.maxZ());
    }

    private static void schedule(Bounds bounds) {
        int minSectionX = SectionPos.blockToSectionCoord(bounds.minX() - 1);
        int minSectionY = SectionPos.blockToSectionCoord(bounds.minY() - 1);
        int minSectionZ = SectionPos.blockToSectionCoord(bounds.minZ() - 1);
        int maxSectionX = SectionPos.blockToSectionCoord(bounds.maxX() + 1);
        int maxSectionY = SectionPos.blockToSectionCoord(bounds.maxY() + 1);
        int maxSectionZ = SectionPos.blockToSectionCoord(bounds.maxZ() + 1);
        synchronized (DIRTY_LOCK) {
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
                    for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                        DIRTY_SECTIONS.add(sectionKey(sectionX, sectionY, sectionZ));
                    }
                }
            }
        }
    }

    private static void rebuildDirtySections(Minecraft minecraft) {
        long[] pending = new long[MAX_SECTION_DIRTY_MARKS_PER_TICK];
        boolean prioritize = minecraft.player != null;
        double cameraX = prioritize ? minecraft.player.getX() : 0.0;
        double cameraY = prioritize ? minecraft.player.getY() : 0.0;
        double cameraZ = prioritize ? minecraft.player.getZ() : 0.0;
        int pendingCount = 0;
        if (!prioritize) {
            synchronized (DIRTY_LOCK) {
                LongIterator iterator = DIRTY_SECTIONS.iterator();
                while (iterator.hasNext() && pendingCount < pending.length) {
                    long key = iterator.nextLong();
                    if (!isSectionLoaded(key)) {
                        // Unloaded sections are compiled from scratch when they
                        // enter the render distance, so retaining this marker
                        // only creates unbounded queue growth while travelling.
                        iterator.remove();
                        continue;
                    }
                    iterator.remove();
                    pending[pendingCount++] = key;
                }
            }
        } else {
            long[] fair = new long[FAIR_SECTION_DIRTY_MARKS_PER_TICK];
            long[] nearest = new long[MAX_SECTION_DIRTY_MARKS_PER_TICK - FAIR_SECTION_DIRTY_MARKS_PER_TICK];
            double[] distances = new double[nearest.length];
            int fairCount = 0;
            int nearestCount = 0;
            synchronized (DIRTY_LOCK) {
                LongIterator iterator = DIRTY_SECTIONS.iterator();
                while (iterator.hasNext()) {
                    long key = iterator.nextLong();
                    if (!isSectionLoaded(key)) {
                        iterator.remove();
                        continue;
                    }
                    if (fairCount < fair.length) {
                        fair[fairCount++] = key;
                        continue;
                    }

                    double distance = sectionDistanceSq(key, cameraX, cameraY, cameraZ);
                    int slot = -1;
                    if (nearestCount < nearest.length) {
                        slot = nearestCount++;
                    } else {
                        int farthestSlot = 0;
                        for (int i = 1; i < nearestCount; i++) {
                            if (distances[i] > distances[farthestSlot]) {
                                farthestSlot = i;
                            }
                        }
                        if (distance < distances[farthestSlot]) {
                            slot = farthestSlot;
                        }
                    }
                    if (slot >= 0) {
                        nearest[slot] = key;
                        distances[slot] = distance;
                    }
                }

                for (int i = 0; i < fairCount; i++) {
                    if (DIRTY_SECTIONS.remove(fair[i])) {
                        pending[pendingCount++] = fair[i];
                    }
                }
                for (int i = 0; i < nearestCount; i++) {
                    if (DIRTY_SECTIONS.remove(nearest[i])) {
                        pending[pendingCount++] = nearest[i];
                    }
                }
            }
        }

        LevelRenderer renderer = minecraft.levelRenderer;
        for (int i = 0; i < pendingCount; i++) {
            long key = pending[i];
            renderer.setSectionDirty(sectionX(key), sectionY(key), sectionZ(key));
        }
    }

    private static double sectionDistanceSq(long key, double x, double y, double z) {
        double centerX = (sectionX(key) << 4) + 8.0;
        double centerY = (sectionY(key) << 4) + 8.0;
        double centerZ = (sectionZ(key) << 4) + 8.0;
        double dx = centerX - x;
        double dy = centerY - y;
        double dz = centerZ - z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean isSectionLoaded(long key) {
        ClientLevel level = currentLevel;
        if (level == null) {
            return true;
        }
        return level.hasChunkAt(new BlockPos(
                sectionX(key) << 4,
                sectionY(key) << 4,
                sectionZ(key) << 4
        ));
    }

    private static SectionIndex emptySectionIndex() {
        return new SectionIndex(
                new Long2ObjectOpenHashMap<>(),
                new IdentityHashMap<>()
        );
    }

    /** The maps are fully built before volatile publication; buckets are immutable apart from revisions. */
    private record SectionIndex(
            Long2ObjectOpenHashMap<SectionBucket> sources,
            IdentityHashMap<DynamicLightBehavior, IndexedSource> byBehavior
    ) {
    }

    private record IndexSourceState(
            DynamicLightBehavior behavior,
            Bounds bounds,
            DynamicLightBehavior.BatchLightSnapshot batchSnapshot
    ) {
    }

    private static final class SectionBucket {
        private final IndexedSource[] sources;
        private volatile long revision;

        private SectionBucket(IndexedSource[] sources, long revision) {
            this.sources = sources;
            this.revision = revision;
        }
    }

    private record CoverageChange(
            DynamicLightBehavior behavior,
            Bounds before,
            Bounds after,
            IndexedSource previousSource
    ) {
    }

    private record BatchSource(
            DynamicLightBehavior.BatchLightSnapshot snapshot,
            Bounds bounds
    ) {
    }

    private static final class BatchEntry {
        private final long revision;
        private final AtomicInteger requests = new AtomicInteger();
        private volatile CompletableFuture<BatchLightValues> future;
        private volatile boolean disabled;

        private BatchEntry(long revision) {
            this.revision = revision;
        }
    }

    private record BatchLightValues(float[] values) {
        private float value(int blockX, int blockY, int blockZ) {
            int index = (blockZ & 15) * 16 * 16
                    + (blockX & 15) * 16
                    + (blockY & 15);
            return values[index];
        }
    }

    private static final class IndexedSource {
        private final DynamicLightBehavior behavior;
        private volatile Bounds bounds;
        private volatile BatchSource batchSource;

        private IndexedSource(
                DynamicLightBehavior behavior,
                Bounds bounds,
                DynamicLightBehavior.BatchLightSnapshot batchSnapshot
        ) {
            this.behavior = behavior;
            this.bounds = bounds;
            this.batchSource = batchSnapshot == null ? null : new BatchSource(batchSnapshot, bounds);
        }

        private void update(
                Bounds bounds,
                DynamicLightBehavior.BatchLightSnapshot batchSnapshot
        ) {
            this.bounds = bounds;
            this.batchSource = batchSnapshot == null ? null : new BatchSource(batchSnapshot, bounds);
        }

    }

    private static final class QueryCache {
        private static final int SLOT_MASK = QUERY_CACHE_SLOTS_PER_THREAD - 1;

        private final long[] keys = new long[QUERY_CACHE_SLOTS_PER_THREAD];
        private final long[] revisions = new long[QUERY_CACHE_SLOTS_PER_THREAD];
        private final float[] values = new float[QUERY_CACHE_SLOTS_PER_THREAD];

        private float get(long currentRevision, long key) {
            int slot = slot(key);
            if (revisions[slot] != currentRevision || keys[slot] != key) {
                return Float.NaN;
            }
            return values[slot];
        }

        private void put(long currentRevision, long key, float light) {
            int slot = slot(key);
            keys[slot] = key;
            values[slot] = light;
            revisions[slot] = currentRevision;
        }

        private static int slot(long key) {
            long mixed = key;
            mixed ^= mixed >>> 33;
            mixed *= 0xff51afd7ed558ccdL;
            mixed ^= mixed >>> 33;
            mixed *= 0xc4ceb9fe1a85ec53L;
            mixed ^= mixed >>> 33;
            return (int) mixed & SLOT_MASK;
        }
    }

    private static long sectionKey(int sectionX, int sectionY, int sectionZ) {
        return ((long) sectionX & 0x3FFFFFL) << 42
                | ((long) sectionZ & 0x3FFFFFL) << 20
                | ((long) sectionY & 0xFFFFFL);
    }

    private static int sectionX(long key) {
        return signExtend((int) ((key >>> 42) & 0x3FFFFFL), 22);
    }

    private static int sectionZ(long key) {
        return signExtend((int) ((key >>> 20) & 0x3FFFFFL), 22);
    }

    private static int sectionY(long key) {
        return signExtend((int) (key & 0xFFFFFL), 20);
    }

    private static int signExtend(int value, int bits) {
        int sign = 1 << (bits - 1);
        return (value ^ sign) - sign;
    }
}
