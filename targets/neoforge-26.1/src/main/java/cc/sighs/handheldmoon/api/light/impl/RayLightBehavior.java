package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.IRayLightConfig;
import cc.sighs.handheldmoon.api.light.LightCache;
import cc.sighs.handheldmoon.util.LineLightMath;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Generic {@link DynamicLightBehavior} implementation driven by
 * {@link Supplier}s for position, direction, and active state.
 * <p>
 * Supports both cone lights (flashlight, lamp) and point lights
 * (full moon block). Light values are computed using inverse-square
 * falloff with angular attenuation for cones. Occlusion may optionally
 * be checked via world raycasts.
 * <p>
 * Create instances via {@link cc.sighs.handheldmoon.api.light.DynamicLightBuilder}.
 *
 * <pre>{@code
 * RayLightBehavior light = DynamicLightBuilder.cone()
 *     .range(24.0).angle(0.5, 0.7).luminance(12.0)
 *     .build(
 *         () -> player.getEyePosition(1.0f),
 *         () -> player.getViewVector(1.0f),
 *         () -> Utils.isUsingFlashlight(player)
 *     );
 * // register with LDL manager
 * manager.add(light);
 * }</pre>
 */
@SuppressWarnings("unused")
public class RayLightBehavior implements DynamicLightBehavior {
    // ---- config & suppliers ----
    private final IRayLightConfig config;
    private final Supplier<Vec3> positionSupplier;
    private final Supplier<Vec3> directionSupplier;
    private final BooleanSupplier activeSupplier;

    // ---- cached state for change detection ----
    private boolean lastActive;
    private double lastRange;
    private double lastLuminance;
    private boolean lastOcclusion;
    private Vec3 lastPos = Vec3.ZERO;
    private Vec3 lastDir = Vec3.ZERO;

    // ---- caches ----
    private LightCache lightCache;
    private final ConcurrentHashMap<Long, Boolean> occlusionCache = new ConcurrentHashMap<>(2048);
    private long occlusionCacheMax = 16_384L;

    private static final double LUMINANCE_THRESHOLD = 0.5;
    private static final double ACTIVE_MIN_PADDING = 1.5;
    private static final double ACTIVE_MAX_PADDING = 8.0;
    private static final double OCCLUSION_REFINE_THRESHOLD = 3.0;

    public RayLightBehavior(
            IRayLightConfig config,
            Supplier<Vec3> positionSupplier,
            Supplier<Vec3> directionSupplier,
            BooleanSupplier activeSupplier
    ) {
        this.config = config;
        this.positionSupplier = positionSupplier;
        this.directionSupplier = directionSupplier;
        this.activeSupplier = activeSupplier;
        this.lastActive = activeSupplier.getAsBoolean();
        this.lastPos = positionSupplier.get();
        this.lastDir = config.type() == IRayLightConfig.LightType.CONE
                ? directionSupplier.get() : Vec3.ZERO;
        this.lastRange = config.range();
        this.lastLuminance = config.luminance();
        this.lastOcclusion = config.occlusionEnabled();

        long size = config.type() == IRayLightConfig.LightType.CONE ? 65_536L : 32_768L;
        this.lightCache = new LightCacheImpl(size);
    }

    // ---- cache override ----

    /** Returns the current light value cache. */
    public LightCache getLightCache() {
        return lightCache;
    }

    /** Replace the light value cache strategy. */
    public void setLightCache(LightCache cache) {
        this.lightCache = cache != null ? cache : new LightCacheImpl(65536);
    }

    /** Replace the occlusion cache max size. */
    public void setOcclusionCacheMax(long max) {
        this.occlusionCacheMax = max;
    }

    // ---- DynamicLightBehavior ----

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        if (!lastActive) return 0.0;

        long key = query.asLong();

        return lightCache.getOrCompute(key, () -> {
            Vec3 pos = lastPos;
            Vec3 dir = lastDir;

            double computed;
            if (config.type() == IRayLightConfig.LightType.CONE) {
                computed = LineLightMath.computeLight(
                        pos.x, pos.y, pos.z,
                        dir.x, dir.y, dir.z,
                        lastLuminance, query, lastRange,
                        config.innerAngle(), config.outerAngle()
                );
            } else {
                // Point light falloff
                computed = computePointLight(pos, query, lastRange, lastLuminance);
            }

            if (computed > 0.0 && lastOcclusion) {
                int shift = LineLightMath.chooseOcclusionBucketShift(computed);
                long occKey = LineLightMath.occlusionBucketKey(query, shift);
                Boolean visible = occlusionCache.get(occKey);
                if (visible == null) {
                    Vec3 sample = LineLightMath.occlusionBucketCenter(query, shift);
                    visible = LineLightMath.isRayVisible(
                            Minecraft.getInstance().level,
                            pos.x, pos.y, pos.z, sample, query, shift
                    );
                    cacheOcclusion(occKey, visible);
                }
                if (!visible) {
                    if (computed >= OCCLUSION_REFINE_THRESHOLD) {
                        computed *= LineLightMath.preciseVisibilityFactor(
                                Minecraft.getInstance().level,
                                pos.x, pos.y, pos.z, query
                        );
                    } else {
                        computed = 0.0;
                    }
                }
            }

            return computed;
        });
    }

    @Override
    public DynamicLightBehavior.BoundingBox getBoundingBox() {
        Vec3 pos = lastPos;
        Vec3 dir = lastDir;
        double eff = effectiveRange();

        if (config.type() == IRayLightConfig.LightType.CONE) {
            double ex = pos.x + dir.x * eff;
            double ey = pos.y + dir.y * eff;
            double ez = pos.z + dir.z * eff;
            double pad = LineLightMath.conePadding(eff, config.outerAngle(),
                    ACTIVE_MIN_PADDING, ACTIVE_MAX_PADDING);
            return new BoundingBox(
                    Mth.floor(Math.min(pos.x, ex) - pad),
                    Mth.floor(Math.min(pos.y, ey) - pad),
                    Mth.floor(Math.min(pos.z, ez) - pad),
                    Mth.floor(Math.max(pos.x, ex) + pad),
                    Mth.floor(Math.max(pos.y, ey) + pad),
                    Mth.floor(Math.max(pos.z, ez) + pad)
            );
        } else {
            int r = (int) Math.ceil(lastRange);
            return new BoundingBox(
                    Mth.floor(pos.x - r), Mth.floor(pos.y - r), Mth.floor(pos.z - r),
                    Mth.floor(pos.x + r), Mth.floor(pos.y + r), Mth.floor(pos.z + r)
            );
        }
    }

    @Override
    public boolean hasChanged() {
        Vec3 pos = positionSupplier.get();
        Vec3 dir = config.type() == IRayLightConfig.LightType.CONE
                ? directionSupplier.get() : Vec3.ZERO;
        boolean active = activeSupplier.getAsBoolean();

        boolean dirChanged = config.type() == IRayLightConfig.LightType.CONE
                && dir.distanceToSqr(lastDir) > 1e-6;
        boolean posChanged = pos.distanceToSqr(lastPos) > 1e-6;
        boolean activeChanged = active != lastActive;
        boolean rangeChanged = Math.abs(config.range() - lastRange) > 0.001;
        boolean lumChanged = Math.abs(config.luminance() - lastLuminance) > 0.001;
        boolean occChanged = config.occlusionEnabled() != lastOcclusion;

        boolean changed = dirChanged || posChanged || activeChanged
                || rangeChanged || lumChanged || occChanged;

        if (changed) {
            lastPos = pos;
            lastDir = dir;
            lastActive = active;
            lastRange = config.range();
            lastLuminance = config.luminance();
            lastOcclusion = config.occlusionEnabled();
            lightCache.clear();
            occlusionCache.clear();
        }

        return changed;
    }

    @Override
    public boolean isRemoved() {
        return !lastActive;
    }

    // ---- internal helpers ----

    private double effectiveRange() {
        double lum = lastLuminance;
        return LineLightMath.effectiveRange(lum, lastRange, LUMINANCE_THRESHOLD);
    }

    private void cacheOcclusion(long key, boolean visible) {
        if (occlusionCache.mappingCount() >= occlusionCacheMax) {
            occlusionCache.clear();
        }
        occlusionCache.put(key, visible);
    }

    private static double computePointLight(Vec3 origin, BlockPos query, double range, double luminance) {
        double dx = query.getX() + 0.5 - origin.x;
        double dy = query.getY() + 0.5 - origin.y;
        double dz = query.getZ() + 0.5 - origin.z;
        double distSq = dx * dx + dy * dy + dz * dz;
        double rangeSq = range * range;
        if (distSq > rangeSq) return 0.0;
        if (distSq < 1.0e-8) return luminance;
        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        double t = Mth.clamp(dist / range, 0.0, 1.0);
        double distMul = 1.0 - (t * t * (3.0 - 2.0 * t));
        return luminance * distMul;
    }
}
