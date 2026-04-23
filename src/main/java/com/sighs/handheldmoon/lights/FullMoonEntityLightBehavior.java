package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.LineLightMath;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FullMoonEntityLightBehavior implements DynamicLightBehavior {
    private final FullMoonEntity entity;
    private static final double RANGE = 32.0;
    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;
    private static final double LUMINANCE_THRESHOLD = 0.5;
    private Vec3 lastPos;
    private int lastLuminance;
    private float lastXRot;
    private float lastYRot;
    private double sX, sY, sZ;
    private double dX, dY, dZ;

    public FullMoonEntityLightBehavior(FullMoonEntity entity) {
        this.entity = entity;
        this.lastPos = entity.position();
        this.lastLuminance = entity.getLampLuminance();
        this.lastXRot = entity.getLampXRot();
        this.lastYRot = entity.getLampYRot();
        refreshState();
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        int luminance = entity.getLampLuminance();
        if (luminance <= 0) return 0.0;
        if (Config.LIGHT_OCCLUSION.get()) {
            return LineLightMath.computeLightOccluded(
                    entity.level(),
                    sX, sY, sZ,
                    dX, dY, dZ,
                    luminance,
                    query,
                    RANGE, INNER, OUTER
            );
        }
        return LineLightMath.computeLight(
                sX, sY, sZ,
                dX, dY, dZ,
                luminance,
                query,
                RANGE, INNER, OUTER
        );
    }

    @Override
    public BoundingBox getBoundingBox() {
        double eff = LineLightMath.effectiveRange(entity.getLampLuminance(), RANGE, LUMINANCE_THRESHOLD);
        double ex = sX + dX * eff;
        double ey = sY + dY * eff;
        double ez = sZ + dZ * eff;
        double r = LineLightMath.conePadding(eff, OUTER, 1.0, 12.0);
        int minX = Mth.floor(Math.min(sX, ex) - r);
        int minY = Mth.floor(Math.min(sY, ey) - r);
        int minZ = Mth.floor(Math.min(sZ, ez) - r);
        int maxX = Mth.floor(Math.max(sX, ex) + r);
        int maxY = Mth.floor(Math.max(sY, ey) + r);
        int maxZ = Mth.floor(Math.max(sZ, ez) + r);
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean hasChanged() {
        Vec3 pos = entity.position();
        int luminance = entity.getLampLuminance();
        float xRot = entity.getLampXRot();
        float yRot = entity.getLampYRot();
        boolean changed = entity.isRemoved()
                || pos.distanceToSqr(lastPos) > 1.0E-4
                || luminance != lastLuminance
                || Math.abs(xRot - lastXRot) > 0.01f
                || Math.abs(yRot - lastYRot) > 0.01f;
        if (changed) {
            refreshState();
        }
        lastPos = pos;
        lastLuminance = luminance;
        lastXRot = xRot;
        lastYRot = yRot;
        return changed;
    }

    @Override
    public boolean isRemoved() {
        return entity.isRemoved() || !entity.isLampBound();
    }

    private void refreshState() {
        Vec3 pos = entity.position();
        sX = pos.x;
        sY = pos.y;
        sZ = pos.z;

        float yaw = entity.getLampYRot();
        float pitch = entity.getLampXRot() - 90.0f;
        Vec3 direction = LineLightMath.computeDirection(yaw, pitch, true);
        dX = -direction.x;
        dY = -direction.y;
        dZ = -direction.z;
    }
}
