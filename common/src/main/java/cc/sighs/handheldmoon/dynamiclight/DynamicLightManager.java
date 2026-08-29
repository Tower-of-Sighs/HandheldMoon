package cc.sighs.handheldmoon.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior.Bounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Minimal client dynamic-light registry and section invalidation engine. */
public final class DynamicLightManager {
    private static final int MAX_SECTION_REBUILDS_PER_TICK = 64;
    private static final int MAX_QUERY_CACHE_ENTRIES = 65_536;
    private static final Set<DynamicLightBehavior> SOURCES = ConcurrentHashMap.newKeySet();
    private static final Object DIRTY_LOCK = new Object();
    private static final LinkedHashSet<SectionKey> DIRTY_SECTIONS = new LinkedHashSet<>();
    private static final ConcurrentHashMap<Long, CachedLight> QUERY_CACHE = new ConcurrentHashMap<>(32_768);

    private static volatile DynamicLightBehavior[] snapshot = new DynamicLightBehavior[0];
    private static volatile Map<Long, IndexedSource[]> sectionIndex = Map.of();
    private static volatile long lightRevision;
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
        sectionIndex = Map.of();
        invalidateQueryCache();
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
        long sectionKey = sectionKey(
                SectionPos.blockToSectionCoord(blockX),
                SectionPos.blockToSectionCoord(blockY),
                SectionPos.blockToSectionCoord(blockZ)
        );
        IndexedSource[] candidates = sectionIndex.get(sectionKey);
        if (candidates == null) {
            return 0.0;
        }

        long revision = lightRevision;
        long blockKey = BlockPos.asLong(blockX, blockY, blockZ);
        CachedLight cached = QUERY_CACHE.get(blockKey);
        if (cached != null && cached.revision == revision) {
            return cached.light;
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
        if (revision == lightRevision) {
            if (QUERY_CACHE.mappingCount() >= MAX_QUERY_CACHE_ENTRIES) {
                QUERY_CACHE.clear();
            }
            QUERY_CACHE.put(blockKey, new CachedLight(revision, (float) light));
        }
        return light;
    }

    private static void refreshSnapshot() {
        snapshot = SOURCES.toArray(DynamicLightBehavior[]::new);
    }

    private static void rebuildSectionIndex() {
        Map<Long, List<IndexedSource>> building = new HashMap<>();
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
                        building.computeIfAbsent(sectionKey(sectionX, sectionY, sectionZ), ignored -> new ArrayList<>())
                                .add(source);
                    }
                }
            }
        }

        Map<Long, IndexedSource[]> rebuilt = new HashMap<>(building.size());
        building.forEach((key, sources) -> rebuilt.put(key, sources.toArray(IndexedSource[]::new)));
        sectionIndex = Collections.unmodifiableMap(rebuilt);
        invalidateQueryCache();
    }

    private static void invalidateQueryCache() {
        lightRevision++;
        if (QUERY_CACHE.mappingCount() >= MAX_QUERY_CACHE_ENTRIES) {
            QUERY_CACHE.clear();
        }
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

    private record CachedLight(long revision, float light) {
    }

    private record IndexedSource(
            DynamicLightBehavior behavior,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    ) {
    }

    private static long sectionKey(int sectionX, int sectionY, int sectionZ) {
        return ((long) sectionX & 0x3FFFFFL) << 42
                | ((long) sectionZ & 0x3FFFFFL) << 20
                | ((long) sectionY & 0xFFFFFL);
    }
}
