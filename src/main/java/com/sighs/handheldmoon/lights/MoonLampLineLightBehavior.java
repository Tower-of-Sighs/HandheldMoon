package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.LineLightMath;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class MoonLampLineLightBehavior implements DynamicLightBehavior {
    private final BlockPos pos;
    private float lastXRot;
    private float lastYRot;
    private boolean lastPowered;
    private static final double RANGE = 32.0;
    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;
    private double sX, sY, sZ;
    private double dX, dY, dZ;
    private double luminance;

    public MoonLampLineLightBehavior(BlockPos pos) {
        this.pos = pos;
    }

    private MoonlightLampBlockEntity lamp() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;
        var be = level.getBlockEntity(pos);
        return be instanceof MoonlightLampBlockEntity m ? m : null;
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        if (!lastPowered || luminance == 0.0) {
            MoonlightLampBlockEntity l = lamp();
            if (l == null || !l.getPowered()) return 0.0;

            lastPowered = true;

            sX = pos.getX() + 0.5;
            sY = pos.getY() + 0.5;
            sZ = pos.getZ() + 0.5;

            float yaw = l.getYRot();
            float pitch = l.getXRot() - 90.0f;

            var d = LineLightMath.computeDirection(yaw, pitch, true);
            dX = -d.x;
            dY = -d.y;
            dZ = -d.z;

            luminance = 15.0;
            lastXRot = l.getXRot();
            lastYRot = l.getYRot();
        }

        if (Config.LIGHT_OCCLUSION.get()) return LineLightMath.computeLightOccluded(
                Minecraft.getInstance().level,
                sX, sY, sZ,
                dX, dY, dZ,
                luminance,
                query,
                RANGE, INNER, OUTER
        );
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
        double sx = sX;
        double sy = sY;
        double sz = sZ;
        double ex = sx + dX * RANGE;
        double ey = sy + dY * RANGE;
        double ez = sz + dZ * RANGE;
        double r = 10.0;
        int minX = Mth.floor(Math.min(sx, ex) - r);
        int minY = Mth.floor(Math.min(sy, ey) - r);
        int minZ = Mth.floor(Math.min(sz, ez) - r);
        int maxX = Mth.floor(Math.max(sx, ex) + r);
        int maxY = Mth.floor(Math.max(sy, ey) + r);
        int maxZ = Mth.floor(Math.max(sz, ez) + r);
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean hasChanged() {
        MoonlightLampBlockEntity lamp = lamp();
        if (lamp == null) return true;
        boolean powered = lamp.getPowered();
        float xr = lamp.getXRot();
        float yr = lamp.getYRot();
        boolean changed = powered != lastPowered || Math.abs(xr - lastXRot) > 0.01f || Math.abs(yr - lastYRot) > 0.01f;
        lastPowered = powered;
        if (changed) {
            sX = pos.getX() + 0.5;
            sY = pos.getY() + 0.5;
            sZ = pos.getZ() + 0.5;
            float adjustedPitch = xr - 90.0f;
            var d = LineLightMath.computeDirection(yr, adjustedPitch, true);
            dX = -d.x;
            dY = -d.y;
            dZ = -d.z;
            luminance = 15.0;
        }
        lastXRot = xr;
        lastYRot = yr;
        return changed;
    }

    @Override
    public boolean isRemoved() {
        MoonlightLampBlockEntity lamp = lamp();
        return lamp == null || !lamp.getPowered();
    }
}