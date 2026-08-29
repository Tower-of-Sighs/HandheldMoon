package cc.sighs.handheldmoon.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior.Bounds;
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

import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Minimal client dynamic-light registry and section invalidation engine. */
public final class DynamicLightManager {
    private static final int MAX_SECTION_REBUILDS_PER_TICK = 64;
    private static final int QUERY_CACHE_SLOTS_PER_THREAD = 16_384;
    private static final Set<DynamicLightBehavior> SOURCES = ConcurrentHashMap.newKeySet();
    private static final Object DIRTY_LOCK = new Object();
    private static final LongLinkedOpenHashSet DIRTY_SECTIONS = new LongLinkedOpenHashSet();
    private static final AtomicLong SECTION_INDEX_REVISION = new AtomicLong();
    private static final ThreadLocal<QueryCache> QUERY_CACHES = ThreadLocal.withInitial(QueryCache::new);

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
            refreshSnapshot();
            rebuildSectionIndex();
            schedule(behavior.getBounds());
        }
    }

    public static void remove(DynamicLightBehavior behavior) {
        if (SOURCES.remove(behavior)) {
            schedule(behavior.getBounds());
            refreshSnapshot();
            rebuildSectionIndex();
        }
    }

    public static void tick(Minecraft minecraft) {
        DynamicLightBehavior[] current = snapshot;
        boolean removedAny = false;
        boolean changedAny = false;
        boolean indexChanged = false;
        for (DynamicLightBehavior behavior : current) {
            Bounds before = behavior.getBounds();
            boolean changed = behavior.hasChanged();
            if (behavior.isRemoved()) {
                if (SOURCES.remove(behavior)) {
                    schedule(before);
                    removedAny = true;
                }
                continue;
            }
            if (changed) {
                schedule(before);
                Bounds after = behavior.getBounds();
                schedule(after);
                if (sameSectionCoverage(before, after)) {
                    // Keep the candidate section membership and refresh the
                    // exact block bounds in place for sub-section movement.
                    SectionIndex index = sectionIndex;
                    IndexedSource indexed = index.byBehavior.get(behavior);
                    if (indexed != null) {
                        indexed.updateBounds(after);
                    } else {
                        indexChanged = true;
                    }
                } else {
                    indexChanged = true;
                }
                changedAny = true;
            }
        }
        if (removedAny) {
            refreshSnapshot();
        }
        if (removedAny || indexChanged) {
            rebuildSectionIndex();
        } else if (changedAny) {
            advanceSectionIndexRevision();
        }
        rebuildDirtySections(minecraft.levelRenderer);
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
        IndexedSource[] candidates = index.sources.get(sectionKey);
        if (candidates == null) {
            return 0.0;
        }

        long blockKey = BlockPos.asLong(blockX, blockY, blockZ);
        QueryCache cache = QUERY_CACHES.get();
        float cached = cache.get(index.revision, blockKey);
        if (!Float.isNaN(cached)) {
            return cached;
        }

        double light = 0.0;
        for (IndexedSource source : candidates) {
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
        if (index == sectionIndex) {
            cache.put(index.revision, blockKey, (float) light);
        }
        return light;
    }

    private static void refreshSnapshot() {
        snapshot = SOURCES.toArray(DynamicLightBehavior[]::new);
    }

    private static void rebuildSectionIndex() {
        DynamicLightBehavior[] current = snapshot;
        IndexedSource[] indexed = new IndexedSource[current.length];
        Long2IntOpenHashMap counts = new Long2IntOpenHashMap();
        counts.defaultReturnValue(0);
        for (int sourceIndex = 0; sourceIndex < current.length; sourceIndex++) {
            DynamicLightBehavior behavior = current[sourceIndex];
            Bounds bounds = behavior.getBounds();
            IndexedSource source = new IndexedSource(behavior, bounds);
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

        Long2ObjectOpenHashMap<IndexedSource[]> rebuilt = new Long2ObjectOpenHashMap<>(counts.size());
        IdentityHashMap<DynamicLightBehavior, IndexedSource> byBehavior = new IdentityHashMap<>(current.length);
        for (IndexedSource source : indexed) {
            byBehavior.put(source.behavior, source);
        }
        for (Long2IntMap.Entry entry : counts.long2IntEntrySet()) {
            rebuilt.put(entry.getLongKey(), new IndexedSource[entry.getIntValue()]);
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
                        IndexedSource[] sources = rebuilt.get(key);
                        int offset = offsets.get(key);
                        sources[offset] = source;
                        offsets.put(key, offset + 1);
                    }
                }
            }
        }
        rebuilt.trim();
        sectionIndex = new SectionIndex(
                SECTION_INDEX_REVISION.incrementAndGet(), rebuilt, byBehavior
        );
    }

    private static void advanceSectionIndexRevision() {
        SectionIndex current = sectionIndex;
        sectionIndex = new SectionIndex(
                SECTION_INDEX_REVISION.incrementAndGet(), current.sources, current.byBehavior
        );
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

    private static void rebuildDirtySections(LevelRenderer renderer) {
        synchronized (DIRTY_LOCK) {
            LongIterator iterator = DIRTY_SECTIONS.iterator();
            int rebuilt = 0;
            while (iterator.hasNext() && rebuilt < MAX_SECTION_REBUILDS_PER_TICK) {
                long key = iterator.nextLong();
                iterator.remove();
                renderer.setSectionDirty(sectionX(key), sectionY(key), sectionZ(key));
                rebuilt++;
            }
        }
    }

    private static SectionIndex emptySectionIndex() {
        return new SectionIndex(
                SECTION_INDEX_REVISION.incrementAndGet(),
                new Long2ObjectOpenHashMap<>(),
                new IdentityHashMap<>()
        );
    }

    /** The maps are fully built before volatile publication; source bounds update in place. */
    private record SectionIndex(
            long revision,
            Long2ObjectOpenHashMap<IndexedSource[]> sources,
            IdentityHashMap<DynamicLightBehavior, IndexedSource> byBehavior
    ) {
    }

    private static final class IndexedSource {
        private final DynamicLightBehavior behavior;
        private volatile Bounds bounds;

        private IndexedSource(DynamicLightBehavior behavior, Bounds bounds) {
            this.behavior = behavior;
            this.bounds = bounds;
        }

        private void updateBounds(Bounds bounds) {
            this.bounds = bounds;
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
