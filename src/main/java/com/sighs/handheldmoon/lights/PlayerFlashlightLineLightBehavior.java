package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.LineLightMath;
import com.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerFlashlightLineLightBehavior implements DynamicLightBehavior {
    private static final Logger LOGGER = LogManager.getLogger(HandheldMoon.MOD_ID);
    private final Player player;
    private float lastYaw;
    private float lastPitch;
    private Vec3 lastPos;
    private boolean lastPowered;
    private static final double RANGE = 32.0;
    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;
    private static final double LUMINANCE_THRESHOLD = 0.5;
    private double eyeX, eyeY, eyeZ;
    private double dirX, dirY, dirZ;
    private double luminance;
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
        this.luminance = Config.REAL_LIGHT_LUMINANCE.get();
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
        if (Config.LIGHT_OCCLUSION.get()) return LineLightMath.computeLightOccluded(
                player.level(),
                eyeX, eyeY, eyeZ,
                dirX, dirY, dirZ,
                luminance,
                query,
                RANGE, INNER, OUTER
        );
        return LineLightMath.computeLight(
                eyeX, eyeY, eyeZ,
                dirX, dirY, dirZ,
                luminance,
                query,
                RANGE, INNER, OUTER);
    }

    @Override
    public BoundingBox getBoundingBox() {
        double sx = eyeX;
        double sy = eyeY;
        double sz = eyeZ;
        double eff = LineLightMath.effectiveRange(luminance, RANGE, LUMINANCE_THRESHOLD);
        double ex = sx + dirX * eff;
        double ey = sy + dirY * eff;
        double ez = sz + dirZ * eff;
        double r = LineLightMath.conePadding(eff, OUTER, 1.0, 12.0);
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

        boolean rotChanged = Math.abs(yaw - lastYaw) > 0.2f || Math.abs(pitch - lastPitch) > 0.2f;
        boolean moved = pos.distanceTo(lastPos) > 0.05;

        Vec3 eye = player.getEyePosition(1.0f);
        double sx = eye.x;
        double sy = eye.y;
        double sz = eye.z;
        Vec3 d = LineLightMath.computeDirection(yaw, pitch, false);
        double nx = d.x;
        double ny = d.y;
        double nz = d.z;

        double eff = LineLightMath.effectiveRange(Config.REAL_LIGHT_LUMINANCE.get(), RANGE, LUMINANCE_THRESHOLD);
        double ex = sx + nx * eff;
        double ey = sy + ny * eff;
        double ez = sz + nz * eff;
        double r = LineLightMath.conePadding(eff, OUTER, 1.0, 12.0);
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

        boolean changed = powered != lastPowered || rotChanged || moved || cellChanged;

        lastPowered = powered;

        if (changed) {
            eyeX = sx;
            eyeY = sy;
            eyeZ = sz;
            dirX = nx;
            dirY = ny;
            dirZ = nz;
            luminance = Config.REAL_LIGHT_LUMINANCE.get();
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
}
