package cc.sighs.handheldmoon.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior.Bounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Minimal client dynamic-light registry and section invalidation engine. */
public final class DynamicLightManager {
    private static final int MAX_SECTION_REBUILDS_PER_TICK = 64;
    private static final int QUERY_CACHE_SLOTS_PER_THREAD = 16_384;
    private static final Set<DynamicLightBehavior> SOURCES = ConcurrentHashMap.newKeySet();
    private static final Object DIRTY_LOCK = new Object();
    private static final LinkedHashSet<SectionKey> DIRTY_SECTIONS = new LinkedHashSet<>();
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
                schedule(behavior.getBounds());
                changedAny = true;
            }
        }
        if (removedAny) {
            refreshSnapshot();
        }
        if (removedAny || changedAny) {
            rebuildSectionIndex();
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
            if (blockX < source.minX || blockX > source.maxX
                    || blockY < source.minY || blockY > source.maxY
                    || blockZ < source.minZ || blockZ > source.maxZ) {
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
        Long2ObjectOpenHashMap<List<IndexedSource>> building = new Long2ObjectOpenHashMap<>();
        for (DynamicLightBehavior behavior : snapshot) {
            Bounds bounds = behavior.getBounds();
            IndexedSource source = new IndexedSource(
                    behavior,
                    bounds.minX(), bounds.minY(), bounds.minZ(),
                    bounds.maxX(), bounds.maxY(), bounds.maxZ()
            );
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
                        List<IndexedSource> sources = building.get(key);
                        if (sources == null) {
                            sources = new ArrayList<>();
                            building.put(key, sources);
                        }
                        sources.add(source);
                    }
                }
            }
        }

        Long2ObjectOpenHashMap<IndexedSource[]> rebuilt = new Long2ObjectOpenHashMap<>(building.size());
        for (Long2ObjectMap.Entry<List<IndexedSource>> entry : building.long2ObjectEntrySet()) {
            rebuilt.put(entry.getLongKey(), entry.getValue().toArray(IndexedSource[]::new));
        }
        rebuilt.trim();
        sectionIndex = new SectionIndex(SECTION_INDEX_REVISION.incrementAndGet(), rebuilt);
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
                        DIRTY_SECTIONS.add(new SectionKey(sectionX, sectionY, sectionZ));
                    }
                }
            }
        }
    }

    private static void rebuildDirtySections(LevelRenderer renderer) {
        synchronized (DIRTY_LOCK) {
            Iterator<SectionKey> iterator = DIRTY_SECTIONS.iterator();
            int rebuilt = 0;
            while (iterator.hasNext() && rebuilt < MAX_SECTION_REBUILDS_PER_TICK) {
                SectionKey section = iterator.next();
                iterator.remove();
                renderer.setSectionDirty(section.x, section.y, section.z);
                rebuilt++;
            }
        }
    }

    private record SectionKey(int x, int y, int z) {
    }

    private static SectionIndex emptySectionIndex() {
        return new SectionIndex(
                SECTION_INDEX_REVISION.incrementAndGet(),
                new Long2ObjectOpenHashMap<>()
        );
    }

    /** The map is fully built before volatile publication and never mutated afterwards. */
    private record SectionIndex(long revision, Long2ObjectOpenHashMap<IndexedSource[]> sources) {
    }

    private record IndexedSource(
            DynamicLightBehavior behavior,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    ) {
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
}
