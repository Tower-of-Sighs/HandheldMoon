package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.LineLightMath;
import cc.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerFlashlightLineLightBehavior implements DynamicLightBehavior {
    private final Player player;
    private float lastYaw;
    private float lastPitch;
    private Vec3 lastPos;
    private boolean lastPowered;
    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;
    private static final double LUMINANCE_THRESHOLD = 0.5;
    private static final double ACTIVE_MIN_PADDING = 1.5;
    private static final double ACTIVE_MAX_PADDING = 8.0;
    private static final long LIGHT_CACHE_LIMIT = 65_536L;
    private static final long OCCLUSION_CACHE_LIMIT = 32_768L;
    private static final double OCCLUSION_REFINE_LIGHT_THRESHOLD = 3.0;
    private double eyeX, eyeY, eyeZ;
    private double dirX, dirY, dirZ;
    private double range;
    private double luminance;
    private boolean lastOcclusionEnabled;
    private final ConcurrentHashMap<Long, Float> lightCache = new ConcurrentHashMap<>(4096);
    private final ConcurrentHashMap<Long, Boolean> occlusionCache = new ConcurrentHashMap<>(2048);
    private int lastCellStartX, lastCellStartY, lastCellStartZ, lastCellEndX, lastCellEndY, lastCellEndZ;

    public PlayerFlashlightLineLightBehavior(Player player) {
        this.player = player;
        this.lastYaw = player.getYRot();
        this.lastPitch = player.getXRot();
        this.lastPos = player.position();
        this.lastPowered = Utils.isUsingFlashlight(player);
        Vec3 eye = player.getEyePosition(1.0f);
        this.eyeX = eye.x;
        this.eyeY = eye.y;
        this.eyeZ = eye.z;
        double yawRad = this.lastYaw * Mth.DEG_TO_RAD;
        double pitchRad = this.lastPitch * Mth.DEG_TO_RAD;
        this.dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
        this.dirY = -Math.sin(pitchRad);
        this.dirZ = Math.cos(yawRad) * Math.cos(pitchRad);
        this.range = Config.LIGHT_RANGE.get();
        this.luminance = Config.REAL_LIGHT_LUMINANCE.get();
        this.lastOcclusionEnabled = Config.LIGHT_OCCLUSION.get();
        this.lastCellStartX = Integer.MIN_VALUE;
        this.lastCellStartY = Integer.MIN_VALUE;
        this.lastCellStartZ = Integer.MIN_VALUE;
        this.lastCellEndX = Integer.MIN_VALUE;
        this.lastCellEndY = Integer.MIN_VALUE;
        this.lastCellEndZ = Integer.MIN_VALUE;
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        if (!lastPowered) return 0.0;

        long key = query.asLong();
        Float cached = lightCache.get(key);
        if (cached != null) {
            return cached;
        }

        double computed = LineLightMath.computeLight(
                eyeX, eyeY, eyeZ,
                dirX, dirY, dirZ,
                luminance,
                query,
                range, INNER, OUTER
        );

        if (computed > 0.0 && Config.LIGHT_OCCLUSION.get()) {
            int shift = LineLightMath.chooseOcclusionBucketShift(computed);
            long occlusionKey = LineLightMath.occlusionBucketKey(query, shift);
            Boolean visible = occlusionCache.get(occlusionKey);
            if (visible == null) {
                Vec3 sample = LineLightMath.occlusionBucketCenter(query, shift);
                visible = LineLightMath.isRayVisible(player.level(), eyeX, eyeY, eyeZ, sample, query, shift);
                cacheOcclusion(occlusionKey, visible);
            }
            if (!visible) {
                if (computed >= OCCLUSION_REFINE_LIGHT_THRESHOLD) {
                    double factor = LineLightMath.preciseVisibilityFactor(player.level(), eyeX, eyeY, eyeZ, query);
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
        double sx = eyeX;
        double sy = eyeY;
        double sz = eyeZ;
        double eff = LineLightMath.effectiveRange(luminance, range, LUMINANCE_THRESHOLD);
        double ex = sx + dirX * eff;
        double ey = sy + dirY * eff;
        double ez = sz + dirZ * eff;
        // Active emission volume only: keep this compact and let LDL cell neighborhood cover falloff light.
        double r = LineLightMath.conePadding(eff, OUTER, ACTIVE_MIN_PADDING, ACTIVE_MAX_PADDING);
        int minX = Mth.floor(Math.min(sx, ex) - r);
        int minY = Mth.floor(Math.min(sy, ey) - r);
        int minZ = Mth.floor(Math.min(sz, ez) - r);
        int maxX = Mth.floor(Math.max(sx, ex) + r);
        int maxY = Mth.floor(Math.max(sy, ey) + r);
        int maxZ = Mth.floor(Math.max(sz, ez) + r);

        BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        return box;
    }

    @Override
    public boolean hasChanged() {
        boolean powered = Utils.isUsingFlashlight(player);
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        Vec3 pos = player.position();

        boolean rotChanged = Math.abs(yaw - lastYaw) > 0.05f || Math.abs(pitch - lastPitch) > 0.05f;
        boolean moved = pos.distanceToSqr(lastPos) > 0.0001;
        double configRange = Config.LIGHT_RANGE.get();
        boolean rangeChanged = Math.abs(configRange - range) > 0.001;
        boolean luminanceChanged = Math.abs(Config.REAL_LIGHT_LUMINANCE.get() - luminance) > 0.001;
        boolean occlusionChanged = Config.LIGHT_OCCLUSION.get() != lastOcclusionEnabled;

        Vec3 eye = player.getEyePosition(1.0f);
        double sx = eye.x;
        double sy = eye.y;
        double sz = eye.z;
        Vec3 d = LineLightMath.computeDirection(yaw, pitch, false);
        double nx = d.x;
        double ny = d.y;
        double nz = d.z;

        double eff = LineLightMath.effectiveRange(Config.REAL_LIGHT_LUMINANCE.get(), configRange, LUMINANCE_THRESHOLD);
        double ex = sx + nx * eff;
        double ey = sy + ny * eff;
        double ez = sz + nz * eff;
        double r = LineLightMath.conePadding(eff, OUTER, ACTIVE_MIN_PADDING, ACTIVE_MAX_PADDING);
        int minX = Mth.floor(Math.min(sx, ex) - r);
        int minY = Mth.floor(Math.min(sy, ey) - r);
        int minZ = Mth.floor(Math.min(sz, ez) - r);
        int maxX = Mth.floor(Math.max(sx, ex) + r);
        int maxY = Mth.floor(Math.max(sy, ey) + r);
        int maxZ = Mth.floor(Math.max(sz, ez) + r);

        int cellStartX = DynamicLightingEngine.positionToCell(minX);
        int cellStartY = DynamicLightingEngine.positionToCell(minY);
        int cellStartZ = DynamicLightingEngine.positionToCell(minZ);
        int cellEndX = DynamicLightingEngine.positionToCell(maxX);
        int cellEndY = DynamicLightingEngine.positionToCell(maxY);
        int cellEndZ = DynamicLightingEngine.positionToCell(maxZ);

        boolean cellChanged = cellStartX != lastCellStartX || cellStartY != lastCellStartY || cellStartZ != lastCellStartZ
                || cellEndX != lastCellEndX || cellEndY != lastCellEndY || cellEndZ != lastCellEndZ;

        boolean changed = powered != lastPowered || rotChanged || moved || cellChanged || rangeChanged || luminanceChanged || occlusionChanged;

        lastPowered = powered;

        if (changed) {
            eyeX = sx;
            eyeY = sy;
            eyeZ = sz;
            dirX = nx;
            dirY = ny;
            dirZ = nz;
            range = configRange;
            luminance = Config.REAL_LIGHT_LUMINANCE.get();
            lastOcclusionEnabled = Config.LIGHT_OCCLUSION.get();
            lightCache.clear();
            occlusionCache.clear();
            lastCellStartX = cellStartX;
            lastCellStartY = cellStartY;
            lastCellStartZ = cellStartZ;
            lastCellEndX = cellEndX;
            lastCellEndY = cellEndY;
            lastCellEndZ = cellEndZ;
        }

        lastYaw = yaw;
        lastPitch = pitch;
        lastPos = pos;

        return changed;
    }

    @Override
    public boolean isRemoved() {
        return !Utils.isUsingFlashlight(player);
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
