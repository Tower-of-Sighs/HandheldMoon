package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.IRayLightConfig;
import cc.sighs.handheldmoon.api.light.LightCache;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.util.LineLightMath;
import cc.sighs.handheldmoon.util.SharedLightMath;
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
 * (full moon block). Light values are computed using quadratic distance
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
 * // register with DynamicLightManager
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
    private long occlusionCacheMax = 4_096L;

    private static final double LUMINANCE_THRESHOLD = 0.5;
    private static final double ACTIVE_MIN_PADDING = 1.0;
    private static final double ACTIVE_MAX_PADDING = 12.0;
    private static final double OCCLUSION_REFINE_THRESHOLD = 3.0;
    private static final double POSITION_CHANGE_SQ = 1.0E-4;
    private static final double DIRECTION_CHANGE_SQ = 1.0E-5;
    private final double cosInner;
    private final double cosOuter;
    private final double cosOuterSq;

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
        this.cosInner = Mth.cos((float) config.innerAngle());
        this.cosOuter = Mth.cos((float) config.outerAngle());
        this.cosOuterSq = cosOuter * cosOuter;

        long size = config.type() == IRayLightConfig.LightType.CONE ? 8_192L : 4_096L;
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
    public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
        if (!lastActive) return 0.0;

        long key = BlockPos.asLong(blockX, blockY, blockZ);
        return lightCache.getOrCompute(key, () -> {
            Vec3 pos = lastPos;
            Vec3 dir = lastDir;

            double computed;
            if (config.type() == IRayLightConfig.LightType.CONE) {
                computed = LineLightMath.computeLightWithCos(
                        pos.x, pos.y, pos.z,
                        dir.x, dir.y, dir.z,
                        lastLuminance, blockX, blockY, blockZ, lastRange,
                        cosInner, cosOuter, cosOuterSq
                );
            } else {
                computed = computePointLight(pos, blockX, blockY, blockZ, lastRange, lastLuminance);
            }

            if (computed > 0.0 && lastOcclusion) {
                BlockPos query = new BlockPos(blockX, blockY, blockZ);
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

    public double lightAtPos(BlockPos query, double falloffRatio) {
        return lightAt(query.getX(), query.getY(), query.getZ(), falloffRatio);
    }

    @Override
    public Bounds getBounds() {
        Vec3 pos = lastPos;
        Vec3 dir = lastDir;
        double eff = effectiveRange();

        if (config.type() == IRayLightConfig.LightType.CONE) {
            double ex = pos.x + dir.x * eff;
            double ey = pos.y + dir.y * eff;
            double ez = pos.z + dir.z * eff;
            double pad = LineLightMath.conePadding(eff, config.outerAngle(),
                    ACTIVE_MIN_PADDING, ACTIVE_MAX_PADDING);
            return new Bounds(
                    Mth.floor(Math.min(pos.x, ex) - pad),
                    Mth.floor(Math.min(pos.y, ey) - pad),
                    Mth.floor(Math.min(pos.z, ez) - pad),
                    Mth.floor(Math.max(pos.x, ex) + pad),
                    Mth.floor(Math.max(pos.y, ey) + pad),
                    Mth.floor(Math.max(pos.z, ez) + pad)
            );
        } else {
            int r = (int) Math.ceil(lastRange);
            return new Bounds(
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
                && dir.distanceToSqr(lastDir) > DIRECTION_CHANGE_SQ;
        boolean posChanged = pos.distanceToSqr(lastPos) > POSITION_CHANGE_SQ;
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

    private static double computePointLight(Vec3 origin, int blockX, int blockY, int blockZ,
                                            double range, double luminance) {
        double dx = blockX + 0.5 - origin.x;
        double dy = blockY + 0.5 - origin.y;
        double dz = blockZ + 0.5 - origin.z;
        double distSq = dx * dx + dy * dy + dz * dz;
        double rangeSq = range * range;
        if (distSq > rangeSq) return 0.0;
        if (distSq < 1.0e-8) return luminance;
        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        double distMul = SharedLightMath.distanceAttenuation(dist, range);
        return luminance * distMul;
    }
}
