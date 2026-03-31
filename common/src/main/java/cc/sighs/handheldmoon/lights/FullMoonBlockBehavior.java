package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.LineLightMath;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import java.util.concurrent.ConcurrentHashMap;

public class FullMoonBlockBehavior implements DynamicLightBehavior {
    private final BlockPos pos;
    private static final double RANGE = 18.0;
    private static final long LIGHT_CACHE_LIMIT = 32_768L;
    private static final long OCCLUSION_CACHE_LIMIT = 16_384L;
    private static final double OCCLUSION_REFINE_LIGHT_THRESHOLD = 3.0;
    private static final long WORLD_BLOCK_CHANGE_REFRESH_TICKS = 6L;
    private boolean lastRealLightEnabled;
    private double lastLuminance;
    private boolean lastOcclusionEnabled;
    private long cacheEpoch = Long.MIN_VALUE;
    private final ConcurrentHashMap<Long, Float> lightCache = new ConcurrentHashMap<>(2048);
    private final ConcurrentHashMap<Long, Boolean> occlusionCache = new ConcurrentHashMap<>(1024);

    public FullMoonBlockBehavior(BlockPos pos) {
        this.pos = pos;
        FullMoonDeviceConfig cfg = configFromLevel();
        this.lastRealLightEnabled = cfg.realLight();
        this.lastLuminance = cfg.realLightLuminance();
        this.lastOcclusionEnabled = cfg.lightOcclusion();
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        FullMoonDeviceConfig cfg = configFromLevel();
        if (!cfg.realLight()) {
            return 0.0;
        }

        refreshWorldDependentCaches(Minecraft.getInstance().level);

        long key = query.asLong();
        Float cached = lightCache.get(key);
        if (cached != null) {
            return cached;
        }

        double computed;
        if (cfg.lightOcclusion()) {
            var level = Minecraft.getInstance().level;
            double sx = pos.getX() + 0.5;
            double sy = pos.getY() + 0.5;
            double sz = pos.getZ() + 0.5;
            double raw = computePointLight(sx, sy, sz, query, RANGE, cfg.realLightLuminance());
            if (raw <= 0.0 || level == null) {
                computed = raw;
            } else {
                int shift = LineLightMath.chooseOcclusionBucketShift(raw);
                long occlusionKey = LineLightMath.occlusionBucketKey(query, shift);
                Boolean visible = occlusionCache.get(occlusionKey);
                if (visible == null) {
                    var sample = LineLightMath.occlusionBucketCenter(query, shift);
                    visible = LineLightMath.isRayVisible(level, sx, sy, sz, sample, query, shift);
                    cacheOcclusion(occlusionKey, visible);
                }
                if (visible) {
                    computed = raw;
                } else if (raw >= OCCLUSION_REFINE_LIGHT_THRESHOLD) {
                    double factor = LineLightMath.preciseVisibilityFactor(level, sx, sy, sz, query);
                    computed = raw * factor;
                } else {
                    computed = 0.0;
                }
            }
        } else {
            computed = computePointLight(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, query, RANGE, cfg.realLightLuminance());
        }

        cacheLight(key, computed);
        return computed;
    }

    @Override
    public BoundingBox getBoundingBox() {
        int r = (int) Math.ceil(RANGE);
        return new BoundingBox(
                pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                pos.getX() + r, pos.getY() + r, pos.getZ() + r
        );
    }

    @Override
    public boolean hasChanged() {
        FullMoonDeviceConfig cfg = configFromLevel();
        boolean realLightChanged = cfg.realLight() != lastRealLightEnabled;
        boolean luminanceChanged = Math.abs(cfg.realLightLuminance() - lastLuminance) > 0.001;
        boolean occlusionChanged = cfg.lightOcclusion() != lastOcclusionEnabled;
        if (realLightChanged || luminanceChanged || occlusionChanged) {
            lastRealLightEnabled = cfg.realLight();
            lastLuminance = cfg.realLightLuminance();
            lastOcclusionEnabled = cfg.lightOcclusion();
            lightCache.clear();
            occlusionCache.clear();
            return true;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) return true;
        var be = getExistingBlockEntity(level);
        if (!(be instanceof FullMoonBlockEntity)) return true;
        return !(level.getBlockState(pos).getBlock() instanceof FullMoonBlock);
    }

    @Override
    public boolean isRemoved() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return true;

        if (!level.hasChunkAt(pos)) {
            return true;
        }

        BlockEntity be = getExistingBlockEntity(level);
        if (!(be instanceof FullMoonBlockEntity)) return true;
        return !(level.getBlockState(pos).getBlock() instanceof FullMoonBlock);
    }

    private static double computePointLight(double sx, double sy, double sz, BlockPos query, double range, double luminance) {
        double dx = query.getX() + 0.5 - sx;
        double dy = query.getY() + 0.5 - sy;
        double dz = query.getZ() + 0.5 - sz;
        double distSq = dx * dx + dy * dy + dz * dz;
        double rangeSq = range * range;
        if (distSq > rangeSq) {
            return 0.0;
        }
        if (distSq < 1.0e-8) {
            return luminance;
        }
        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        double t = Mth.clamp(dist / range, 0.0, 1.0);
        double distanceMultiplier = 1.0 - (t * t * (3.0 - 2.0 * t));
        return luminance * distanceMultiplier;
    }

    private void cacheLight(long key, double value) {
        if (lightCache.mappingCount() >= LIGHT_CACHE_LIMIT) {
            lightCache.clear();
        }
        lightCache.put(key, (float) value);
    }

    private void cacheOcclusion(long key, boolean visible) {
        if (occlusionCache.mappingCount() >= OCCLUSION_CACHE_LIMIT) {
            occlusionCache.clear();
        }
        occlusionCache.put(key, visible);
    }

    private FullMoonDeviceConfig configFromLevel() {
        Level level = Minecraft.getInstance().level;
        if (level != null && getExistingBlockEntity(level) instanceof FullMoonBlockEntity moon) {
            return moon.getFullMoonConfig();
        }
        return new FullMoonDeviceConfig(Config.REAL_LIGHT.get(), Config.REAL_LIGHT_LUMINANCE.get(), Config.LIGHT_OCCLUSION.get());
    }

    private void refreshWorldDependentCaches(Level level) {
        if (level == null) {
            return;
        }
        long epoch = level.getGameTime() / WORLD_BLOCK_CHANGE_REFRESH_TICKS;
        if (epoch != cacheEpoch) {
            cacheEpoch = epoch;
            lightCache.clear();
            occlusionCache.clear();
        }
    }

    private BlockEntity getExistingBlockEntity(Level level) {
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        LevelChunk chunk = level.getChunkAt(pos);
        return chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    }
}
