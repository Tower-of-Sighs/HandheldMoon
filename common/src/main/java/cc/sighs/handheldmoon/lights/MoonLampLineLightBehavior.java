package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import cc.sighs.handheldmoon.util.LineLightMath;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Moonlight lamp block light behavior.
 * <p>
 * Delegates to {@link RayLightBehavior} internally via
 * the {@link cc.sighs.handheldmoon.api.light} API.
 * Lazily initialises from the block entity on the first light query.
 */
public class MoonLampLineLightBehavior implements DynamicLightBehavior {
    private final BlockPos pos;
    private RayLightBehavior delegate;
    private boolean initialized = false;
    private double lastRange;
    private double lastLuminance;
    private boolean lastOcclusion;

    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;

    public MoonLampLineLightBehavior(BlockPos pos) {
        this.pos = pos;
    }

    private void ensureInitialized() {
        if (initialized) return;
        MoonlightLampBlockEntityAccess lamp = lamp();
        if (lamp == null) return;
        LampDeviceConfig cfg = lamp.getLampConfig();

        Vec3 center = pos.getCenter();
        float adjustedPitch = lamp.getXRot() - 90.0f;
        Vec3 dir = LineLightMath.computeDirection(lamp.getYRot(), adjustedPitch, true).scale(-1);

        this.delegate = new RayLightBehavior(
                DynamicLightBuilder.cone()
                        .range(DynamicLightDefaults.FLASHLIGHT_RANGE)
                        .angle(INNER, OUTER)
                        .luminance(cfg.realLightLuminance())
                        .occlusion(cfg.lightOcclusion())
                        .buildConfig(),
                        () -> pos.getCenter(),
                        () -> {
                            MoonlightLampBlockEntityAccess l = lamp();
                            if (l == null) return dir;
                            float ap = l.getXRot() - 90.0f;
                            return LineLightMath.computeDirection(l.getYRot(), ap, true).scale(-1);
                        },
                        () -> {
                            MoonlightLampBlockEntityAccess l = lamp();
                            return l != null && l.getPowered() && l.getLampConfig().realLight();
                        }
                );
        this.lastRange = cfg.lightRange();
        this.lastLuminance = cfg.realLightLuminance();
        this.lastOcclusion = cfg.lightOcclusion();
        this.initialized = true;
    }

    @Override
    public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
        ensureInitialized();
        if (delegate == null) return 0.0;
        return delegate.lightAt(blockX, blockY, blockZ, falloffRatio);
    }

    @Override
    public Bounds getBounds() {
        ensureInitialized();
        if (delegate == null) return Bounds.empty();
        return delegate.getBounds();
    }

    @Override
    public boolean hasChanged() {
        MoonlightLampBlockEntityAccess lamp = lamp();
        if (lamp == null) {
            initialized = false;
            return true;
        }

        if (!initialized) return true;

        LampDeviceConfig cfg = lamp.getLampConfig();
        boolean cfgChanged = Math.abs(cfg.lightRange() - lastRange) > 0.001
                || Math.abs(cfg.realLightLuminance() - lastLuminance) > 0.001
                || cfg.lightOcclusion() != lastOcclusion;
        if (cfgChanged) {
            lastRange = cfg.lightRange();
            lastLuminance = cfg.realLightLuminance();
            lastOcclusion = cfg.lightOcclusion();
            delegate = null;
            initialized = false;
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        MoonlightLampBlockEntityAccess lamp = lamp();
        return lamp == null || !lamp.getPowered() || !lamp.getLampConfig().realLight();
    }

    @Override
    public BatchLightSnapshot getBatchLightSnapshot() {
        return delegate == null ? null : delegate.getBatchLightSnapshot();
    }

    // ---- internal ----

    private MoonlightLampBlockEntityAccess lamp() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;
        var be = level.getBlockEntity(pos);
        return be instanceof MoonlightLampBlockEntityAccess lamp ? lamp : null;
    }
}
