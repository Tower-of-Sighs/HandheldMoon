package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.registry.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

/**
 * Full moon block point-light behavior.
 * <p>
 * Delegates to {@link RayLightBehavior} internally via
 * the {@link cc.sighs.handheldmoon.api.light} API.
 */
public class FullMoonBlockBehavior implements DynamicLightBehavior {
    private final BlockPos pos;
    private RayLightBehavior delegate;
    private double lastLuminance;
    private boolean lastRealLight;
    private boolean lastOcclusion;
    private static final long WORLD_CHANGE_REFRESH_TICKS = 6L;
    private long cacheEpoch = Long.MIN_VALUE;

    public FullMoonBlockBehavior(BlockPos pos) {
        this.pos = pos;
        FullMoonDeviceConfig cfg = configFromLevel();
        this.lastLuminance = cfg.realLightLuminance();
        this.lastRealLight = cfg.realLight();
        this.lastOcclusion = cfg.lightOcclusion();
        this.delegate = createDelegate(cfg);
    }

    private RayLightBehavior createDelegate(FullMoonDeviceConfig cfg) {
        return new RayLightBehavior(
                DynamicLightBuilder.point()
                        .range(18.0)
                        .luminance(cfg.realLightLuminance())
                        .occlusion(cfg.lightOcclusion())
                        .buildConfig(),
                        () -> pos.getCenter(),
                        () -> Vec3.ZERO,
                        () -> configFromLevel().realLight()
                );
    }

    @Override
    public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
        refreshEpoch();
        return delegate.lightAt(blockX, blockY, blockZ, falloffRatio);
    }

    @Override
    public Bounds getBounds() {
        return delegate.getBounds();
    }

    @Override
    public boolean hasChanged() {
        FullMoonDeviceConfig cfg = configFromLevel();
        boolean cfgChanged = Math.abs(cfg.realLightLuminance() - lastLuminance) > 0.001
                || cfg.realLight() != lastRealLight
                || cfg.lightOcclusion() != lastOcclusion;
        if (cfgChanged) {
            lastLuminance = cfg.realLightLuminance();
            lastRealLight = cfg.realLight();
            lastOcclusion = cfg.lightOcclusion();
            delegate = createDelegate(cfg);
            return true;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) return true;
        var be = getExistingBlockEntity(level);
        if (!(be instanceof FullMoonBlockEntity)) return true;
        return !(level.getBlockState(pos).getBlock() instanceof FullMoonBlock);
    }

    @Override
    public boolean isRemoved() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return true;
        if (!level.hasChunkAt(pos)) return true;
        BlockEntity be = getExistingBlockEntity(level);
        if (!(be instanceof FullMoonBlockEntity)) return true;
        return !(level.getBlockState(pos).getBlock() instanceof FullMoonBlock);
    }

    // ---- internal ----

    private void refreshEpoch() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        long epoch = level.getGameTime() / WORLD_CHANGE_REFRESH_TICKS;
        if (epoch != cacheEpoch) {
            cacheEpoch = epoch;
            delegate.getLightCache().clear();
        }
    }

    private FullMoonDeviceConfig configFromLevel() {
        Level level = Minecraft.getInstance().level;
        if (level != null && getExistingBlockEntity(level) instanceof FullMoonBlockEntity moon) {
            return moon.getFullMoonConfig();
        }
        return new FullMoonDeviceConfig(
                Config.REAL_LIGHT.get(),
                Config.REAL_LIGHT_LUMINANCE.get(),
                Config.LIGHT_OCCLUSION.get()
        );
    }

    private BlockEntity getExistingBlockEntity(Level level) {
        if (!level.hasChunkAt(pos)) return null;
        LevelChunk chunk = level.getChunkAt(pos);
        return chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    }
}
