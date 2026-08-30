package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.IRayLightConfig;
import cc.sighs.handheldmoon.api.light.AttenuationCurve;
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
    private AttenuationCurve lastAttenuation;
    private boolean lastOcclusion;
    private Vec3 lastPos = Vec3.ZERO;
    private Vec3 lastDir = Vec3.ZERO;
    private Bounds boundsCache;

    // ---- caches ----
    private static final LightCache NO_LIGHT_CACHE = LightCache.none();
    private LightCache lightCache = NO_LIGHT_CACHE;
    private final ConcurrentHashMap<Long, Boolean> occlusionCache = new ConcurrentHashMap<>(2048);
    private long occlusionCacheMax = 4_096L;

    private static final double LUMINANCE_THRESHOLD = 0.5;
    private static final double OCCLUSION_REFINE_THRESHOLD = 3.0;
    private static final double POSITION_CHANGE_SQ = 1.0E-4;
    private static final double DIRECTION_CHANGE_SQ = 1.0E-5;
    private static final double BOUNDS_EPSILON = 1.0E-6;
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
        this.lastAttenuation = config.attenuationCurve();
        this.lastOcclusion = config.occlusionEnabled();
        this.cosInner = Mth.cos((float) config.innerAngle());
        this.cosOuter = Mth.cos((float) config.outerAngle());
        this.cosOuterSq = cosOuter * cosOuter;
    }

    // ---- cache override ----

    /** Returns the current light value cache. */
    public LightCache getLightCache() {
        return lightCache;
    }

    /** Replace the light value cache strategy. */
    public void setLightCache(LightCache cache) {
        this.lightCache = cache != null ? cache : NO_LIGHT_CACHE;
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
        LightCache cache = lightCache;
        if (cache != NO_LIGHT_CACHE) {
            double cached = cache.get(key);
            if (!Double.isNaN(cached)) {
                return cached;
            }
        }

        Vec3 pos = lastPos;
        Vec3 dir = lastDir;

        double computed;
        if (config.type() == IRayLightConfig.LightType.CONE) {
            computed = LineLightMath.computeLightWithCos(
                    pos.x, pos.y, pos.z,
                    dir.x, dir.y, dir.z,
                    lastLuminance, blockX, blockY, blockZ, lastRange,
                    cosInner, cosOuter, cosOuterSq, lastAttenuation
            );
        } else {
            computed = computePointLight(pos, blockX, blockY, blockZ, lastRange,
                    lastLuminance, lastAttenuation);
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

        if (cache != NO_LIGHT_CACHE) {
            cache.put(key, computed);
        }
        return computed;
    }

    public double lightAtPos(BlockPos query, double falloffRatio) {
        return lightAt(query.getX(), query.getY(), query.getZ(), falloffRatio);
    }

    @Override
    public Bounds getBounds() {
        Bounds cached = boundsCache;
        if (cached != null) {
            return cached;
        }

        Vec3 pos = lastPos;
        Vec3 dir = lastDir;
        double eff = effectiveRange();

        if (config.type() == IRayLightConfig.LightType.CONE) {
            // The light query is a spherical sector, not a padded axis-aligned
            // prism. Account for non-normalised directions and fall back to a
            // sphere only when the support cone is degenerate.
            double directionLengthSq = dir.lengthSqr();
            double directionLength = Math.sqrt(directionLengthSq);
            // computeLightWithCos uses the supplied vector directly. Account
            // for its length when deriving the support cone instead of
            // treating harmless float sin/cos drift as malformed input.
            double supportCos = Math.abs(cosOuter) / directionLength;
            if (eff <= 0.0
                    || !Double.isFinite(directionLength)
                    || directionLength <= 1.0E-8
                    || !Double.isFinite(supportCos)
                    || supportCos >= 1.0) {
                cached = sphereBounds(pos, Math.max(eff, 1.0));
            } else {
                SharedLightMath.Aabb aabb = SharedLightMath.sphericalConeBounds(
                        pos.x, pos.y, pos.z,
                        dir.x, dir.y, dir.z,
                        eff, Math.acos(Math.max(0.0, supportCos))
                );
                cached = voxelBounds(aabb);
            }
        } else {
            cached = sphereBounds(pos, lastRange);
        }
        boundsCache = cached;
        return cached;
    }

    private static Bounds voxelBounds(SharedLightMath.Aabb aabb) {
        return new Bounds(
                Mth.floor(aabb.minX() - BOUNDS_EPSILON),
                Mth.floor(aabb.minY() - BOUNDS_EPSILON),
                Mth.floor(aabb.minZ() - BOUNDS_EPSILON),
                Mth.floor(aabb.maxX() + BOUNDS_EPSILON),
                Mth.floor(aabb.maxY() + BOUNDS_EPSILON),
                Mth.floor(aabb.maxZ() + BOUNDS_EPSILON)
        );
    }

    private static Bounds sphereBounds(Vec3 pos, double radius) {
        int r = (int) Math.ceil(Math.max(radius, 0.0));
        return new Bounds(
                Mth.floor(pos.x - r), Mth.floor(pos.y - r), Mth.floor(pos.z - r),
                Mth.floor(pos.x + r), Mth.floor(pos.y + r), Mth.floor(pos.z + r)
        );
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
        boolean attenuationChanged = config.attenuationCurve() != lastAttenuation;
        boolean occChanged = config.occlusionEnabled() != lastOcclusion;

        boolean changed = dirChanged || posChanged || activeChanged
                || rangeChanged || lumChanged || attenuationChanged || occChanged;

        if (changed) {
            lastPos = pos;
            lastDir = dir;
            lastActive = active;
            lastRange = config.range();
            lastLuminance = config.luminance();
            lastAttenuation = config.attenuationCurve();
            lastOcclusion = config.occlusionEnabled();
            boundsCache = null;
            lightCache.clear();
            occlusionCache.clear();
        }

        return changed;
    }

    @Override
    public boolean isRemoved() {
        return !lastActive;
    }

    @Override
    public BatchLightSnapshot getBatchLightSnapshot() {
        if (!lastActive || lastOcclusion || lightCache != NO_LIGHT_CACHE) {
            return null;
        }
        Vec3 pos = lastPos;
        Vec3 dir = lastDir;
        return new BatchLightSnapshot(
                config.type() == IRayLightConfig.LightType.CONE,
                pos.x, pos.y, pos.z,
                dir.x, dir.y, dir.z,
                lastRange, lastLuminance,
                cosInner, cosOuter, cosOuterSq, lastAttenuation
        );
    }

    // ---- internal helpers ----

    private double effectiveRange() {
        double lum = lastLuminance;
        return LineLightMath.effectiveRange(lum, lastRange, LUMINANCE_THRESHOLD, lastAttenuation);
    }

    private void cacheOcclusion(long key, boolean visible) {
        if (occlusionCache.mappingCount() >= occlusionCacheMax) {
            occlusionCache.clear();
        }
        occlusionCache.put(key, visible);
    }

    private static double computePointLight(Vec3 origin, int blockX, int blockY, int blockZ,
                                            double range, double luminance,
                                            AttenuationCurve attenuationCurve) {
        double dx = blockX + 0.5 - origin.x;
        double dy = blockY + 0.5 - origin.y;
        double dz = blockZ + 0.5 - origin.z;
        double distSq = dx * dx + dy * dy + dz * dz;
        double rangeSq = range * range;
        if (distSq >= rangeSq) return 0.0;
        if (distSq < 1.0e-8) return Math.max(luminance, 0.0);
        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        return SharedLightMath.attenuatedIntensity(luminance, dist, range, attenuationCurve);
    }
}
