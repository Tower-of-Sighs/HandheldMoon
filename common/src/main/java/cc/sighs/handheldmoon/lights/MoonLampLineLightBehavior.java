package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.LineLightMath;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ConcurrentHashMap;

public class MoonLampLineLightBehavior implements DynamicLightBehavior {
    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;
    private static final double LUMINANCE_THRESHOLD = 0.5;
    private static final double ACTIVE_MIN_PADDING = 1.5;
    private static final double ACTIVE_MAX_PADDING = 8.0;
    private static final long LIGHT_CACHE_LIMIT = 32_768L;
    private static final long OCCLUSION_CACHE_LIMIT = 16_384L;
    private static final double OCCLUSION_REFINE_LIGHT_THRESHOLD = 3.0;

    private final BlockPos pos;
    private float lastXRot;
    private float lastYRot;
    private boolean lastPowered;
    private double sX;
    private double sY;
    private double sZ;
    private double dX;
    private double dY;
    private double dZ;
    private double range;
    private double luminance;
    private boolean lastRealLightEnabled;
    private boolean lastOcclusionEnabled;

    private final ConcurrentHashMap<Long, Float> lightCache = new ConcurrentHashMap<>(2048);
    private final ConcurrentHashMap<Long, Boolean> occlusionCache = new ConcurrentHashMap<>(1024);

    public MoonLampLineLightBehavior(BlockPos pos) {
        this.pos = pos;
        this.range = Config.LIGHT_RANGE.get();
        this.lastRealLightEnabled = Config.REAL_LIGHT.get();
        this.lastOcclusionEnabled = Config.LIGHT_OCCLUSION.get();
    }

    private MoonlightLampBlockEntity lamp() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        var be = level.getBlockEntity(pos);
        return be instanceof MoonlightLampBlockEntity lamp ? lamp : null;
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        if (!lastPowered || luminance == 0.0) {
            MoonlightLampBlockEntity lamp = lamp();
            if (lamp == null || !lamp.getPowered()) {
                return 0.0;
            }

            LampDeviceConfig cfg = lamp.getLampConfig();
            if (!cfg.realLight()) {
                lastPowered = lamp.getPowered();
                return 0.0;
            }

            lastPowered = true;
            sX = pos.getX() + 0.5;
            sY = pos.getY() + 0.5;
            sZ = pos.getZ() + 0.5;

            float yaw = lamp.getYRot();
            float pitch = lamp.getXRot() - 90.0f;
            Vec3 direction = LineLightMath.computeDirection(yaw, pitch, true);
            dX = -direction.x;
            dY = -direction.y;
            dZ = -direction.z;

            range = cfg.lightRange();
            luminance = cfg.realLightLuminance();
            lastXRot = lamp.getXRot();
            lastYRot = lamp.getYRot();
            lastRealLightEnabled = cfg.realLight();
            lastOcclusionEnabled = cfg.lightOcclusion();
            lightCache.clear();
            occlusionCache.clear();
        }

        long key = query.asLong();
        Float cached = lightCache.get(key);
        if (cached != null) {
            return cached;
        }

        double computed = LineLightMath.computeLight(
                sX, sY, sZ,
                dX, dY, dZ,
                luminance,
                query,
                range, INNER, OUTER
        );

        if (computed > 0.0 && lastOcclusionEnabled) {
            int shift = LineLightMath.chooseOcclusionBucketShift(computed);
            long occlusionKey = LineLightMath.occlusionBucketKey(query, shift);
            Boolean visible = occlusionCache.get(occlusionKey);
            if (visible == null) {
                Vec3 sample = LineLightMath.occlusionBucketCenter(query, shift);
                visible = LineLightMath.isRayVisible(Minecraft.getInstance().level, sX, sY, sZ, sample, query, shift);
                cacheOcclusion(occlusionKey, visible);
            }
            if (!visible) {
                if (computed >= OCCLUSION_REFINE_LIGHT_THRESHOLD) {
                    double factor = LineLightMath.preciseVisibilityFactor(Minecraft.getInstance().level, sX, sY, sZ, query);
                    computed *= factor;
                } else {
                    computed = 0.0;
                }
            }
        }

        cacheLight(key, computed);
        return computed;
    }

    @Override
    public BoundingBox getBoundingBox() {
        double eff = LineLightMath.effectiveRange(luminance, range, LUMINANCE_THRESHOLD);
        double ex = sX + dX * eff;
        double ey = sY + dY * eff;
        double ez = sZ + dZ * eff;
        // Active emission volume only: compact bounds reduce LDL rebuild pressure.
        double pad = LineLightMath.conePadding(eff, OUTER, ACTIVE_MIN_PADDING, ACTIVE_MAX_PADDING);

        int minX = Mth.floor(Math.min(sX, ex) - pad);
        int minY = Mth.floor(Math.min(sY, ey) - pad);
        int minZ = Mth.floor(Math.min(sZ, ez) - pad);
        int maxX = Mth.floor(Math.max(sX, ex) + pad);
        int maxY = Mth.floor(Math.max(sY, ey) + pad);
        int maxZ = Mth.floor(Math.max(sZ, ez) + pad);
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean hasChanged() {
        MoonlightLampBlockEntity lamp = lamp();
        if (lamp == null) {
            return true;
        }

        boolean powered = lamp.getPowered();
        float xr = lamp.getXRot();
        float yr = lamp.getYRot();
        LampDeviceConfig cfg = lamp.getLampConfig();

        double configRange = cfg.lightRange();
        boolean rangeChanged = Math.abs(configRange - range) > 0.001;
        boolean luminanceChanged = Math.abs(cfg.realLightLuminance() - luminance) > 0.001;
        boolean realLightChanged = cfg.realLight() != lastRealLightEnabled;
        boolean occlusionChanged = cfg.lightOcclusion() != lastOcclusionEnabled;

        boolean changed = powered != lastPowered
                || Math.abs(xr - lastXRot) > 0.01f
                || Math.abs(yr - lastYRot) > 0.01f
                || rangeChanged
                || luminanceChanged
                || realLightChanged
                || occlusionChanged;
        lastPowered = powered;

        if (changed) {
            sX = pos.getX() + 0.5;
            sY = pos.getY() + 0.5;
            sZ = pos.getZ() + 0.5;
            float adjustedPitch = xr - 90.0f;
            Vec3 direction = LineLightMath.computeDirection(yr, adjustedPitch, true);
            dX = -direction.x;
            dY = -direction.y;
            dZ = -direction.z;
            range = configRange;
            luminance = cfg.realLightLuminance();
            lastRealLightEnabled = cfg.realLight();
            lastOcclusionEnabled = cfg.lightOcclusion();
            lightCache.clear();
            occlusionCache.clear();
        }

        lastXRot = xr;
        lastYRot = yr;
        return changed;
    }

    @Override
    public boolean isRemoved() {
        MoonlightLampBlockEntity lamp = lamp();
        return lamp == null || !lamp.getPowered() || !lamp.getLampConfig().realLight();
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
}
