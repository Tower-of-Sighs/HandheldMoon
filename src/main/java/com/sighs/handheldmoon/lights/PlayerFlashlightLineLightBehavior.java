package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.LineLightMath;
import com.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.minecraft.client.Minecraft;
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
    private static final double BB_OUTER = Math.atan(12.0 / RANGE);
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
        this.luminance = 15.0;
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
        double ex = sx + dirX * RANGE;
        double ey = sy + dirY * RANGE;
        double ez = sz + dirZ * RANGE;
        double rMax = RANGE * Math.tan(BB_OUTER);
        double rX = rMax * Math.sqrt(1.0 - dirX * dirX);
        double rY = rMax * Math.sqrt(1.0 - dirY * dirY);
        double rZ = rMax * Math.sqrt(1.0 - dirZ * dirZ);
        Vec3 cam = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getViewVector(1.0f).normalize() : new Vec3(0, 0, 1);
        double cx = cam.x;
        double cy = cam.y;
        double cz = cam.z;
        double pad = DynamicLightingEngine.CELL_SIZE / 3.0;
        int minX = Mth.floor(Math.min(sx, ex) - ((ex < sx && cx < 0.0) ? rX : 0.0) - pad);
        int minY = Mth.floor(Math.min(sy, ey) - ((ey < sy && cy < 0.0) ? rY : 0.0) - pad);
        int minZ = Mth.floor(Math.min(sz, ez) - ((ez < sz && cz < 0.0) ? rZ : 0.0) - pad);
        int maxX = Mth.floor(Math.max(sx, ex) + ((ex > sx && cx > 0.0) ? rX : 0.0) + pad);
        int maxY = Mth.floor(Math.max(sy, ey) + ((ey > sy && cy > 0.0) ? rY : 0.0) + pad);
        int maxZ = Mth.floor(Math.max(sz, ez) + ((ez > sz && cz > 0.0) ? rZ : 0.0) + pad);

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
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

        double ex = sx + nx * RANGE;
        double ey = sy + ny * RANGE;
        double ez = sz + nz * RANGE;
        double rMax = RANGE * Math.tan(BB_OUTER);
        double rX = rMax * Math.sqrt(1.0 - nx * nx);
        double rY = rMax * Math.sqrt(1.0 - ny * ny);
        double rZ = rMax * Math.sqrt(1.0 - nz * nz);
        int minX = Mth.floor(Math.min(sx, ex) - (ex < sx ? rX : 0.0));
        int minY = Mth.floor(Math.min(sy, ey) - (ey < sy ? rY : 0.0));
        int minZ = Mth.floor(Math.min(sz, ez) - (ez < sz ? rZ : 0.0));
        int maxX = Mth.floor(Math.max(sx, ex) + (ex > sx ? rX : 0.0));
        int maxY = Mth.floor(Math.max(sy, ey) + (ey > sy ? rY : 0.0));
        int maxZ = Mth.floor(Math.max(sz, ez) + (ez > sz ? rZ : 0.0));

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
            luminance = 15.0;
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