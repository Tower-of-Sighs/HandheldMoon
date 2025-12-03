package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.LineLightMath;
import com.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
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
    private static final double COS_INNER = Math.cos(0.5);
    private static final double COS_OUTER = Math.cos(0.7);
    private double eyeX, eyeY, eyeZ;
    private double dirX, dirY, dirZ;
    private double luminance;

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
        this.luminance = Config.LIGHT_INTENSITY.get() * 15.0;
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        if (!lastPowered) return 0.0;

        return LineLightMath.computeLight(
                eyeX, eyeY, eyeZ,
                dirX, dirY, dirZ,
                luminance,
                query,
                RANGE, COS_INNER, COS_OUTER
        );
    }

    @Override
    public BoundingBox getBoundingBox() {
        Vec3 s = player.getEyePosition(1.0f);
        Vec3 d = player.getViewVector(1.0f).normalize();
        double range = 32.0;
        Vec3 e = s.add(d.scale(range));
        double outer = 0.7;
        double rMax = range * Math.tan(outer);
        int minX = Mth.floor(Math.min(s.x, e.x) - rMax);
        int minY = Mth.floor(Math.min(s.y, e.y) - rMax);
        int minZ = Mth.floor(Math.min(s.z, e.z) - rMax);
        int maxX = Mth.floor(Math.max(s.x, e.x) + rMax);
        int maxY = Mth.floor(Math.max(s.y, e.y) + rMax);
        int maxZ = Mth.floor(Math.max(s.z, e.z) + rMax);
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean hasChanged() {
        boolean powered = Utils.isUsingFlashlight(player);
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        Vec3 pos = player.position();

        boolean rotChanged = Math.abs(yaw - lastYaw) > 0.5f || Math.abs(pitch - lastPitch) > 0.5f;
        boolean moved = pos.distanceTo(lastPos) > 0.25;
        boolean changed = powered != lastPowered || rotChanged || moved;

        lastPowered = powered;

        if (rotChanged || moved) {
            Vec3 eye = player.getEyePosition(1.0f);
            eyeX = eye.x;
            eyeY = eye.y;
            eyeZ = eye.z;

            Vec3 d = LineLightMath.computeDirection(yaw, pitch, false);
            dirX = d.x;
            dirY = d.y;
            dirZ = d.z;

            luminance = Config.LIGHT_INTENSITY.get() * 15.0;
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