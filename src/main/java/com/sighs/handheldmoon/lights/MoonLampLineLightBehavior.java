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
    private static final double COS_INNER = Math.cos(0.5);
    private static final double COS_OUTER = Math.cos(0.7);
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

            float adjustedPitch = l.getXRot() + 90.0f;
            var d = LineLightMath.computeDirection(l.getYRot(), adjustedPitch, true);
            dX = d.x;
            dY = d.y;
            dZ = d.z;

            luminance = Config.LIGHT_INTENSITY.get() * 15.0;
            lastXRot = l.getXRot();
            lastYRot = l.getYRot();
        }

        return LineLightMath.computeLight(
                sX, sY, sZ,
                dX, dY, dZ,
                luminance,
                query,
                RANGE, COS_INNER, COS_OUTER
        );
    }

    @Override
    public BoundingBox getBoundingBox() {
        double ex = sX + dX * RANGE;
        double ey = sY + dY * RANGE;
        double ez = sZ + dZ * RANGE;
        double rMax = RANGE * Math.tan(0.7);
        int minX = Mth.floor(Math.min(sX, ex) - rMax);
        int minY = Mth.floor(Math.min(sY, ey) - rMax);
        int minZ = Mth.floor(Math.min(sZ, ez) - rMax);
        int maxX = Mth.floor(Math.max(sX, ex) + rMax);
        int maxY = Mth.floor(Math.max(sY, ey) + rMax);
        int maxZ = Mth.floor(Math.max(sZ, ez) + rMax);
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
            float adjustedPitch = xr + 90.0f;
            double yawRad = yr * Mth.DEG_TO_RAD;
            double pitchRad = adjustedPitch * Mth.DEG_TO_RAD;
            dX = Math.sin(yawRad) * Math.cos(pitchRad);
            dY = -Math.sin(pitchRad);
            dZ = Math.cos(yawRad) * Math.cos(pitchRad);
            luminance = Config.LIGHT_INTENSITY.get() * 15.0;
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