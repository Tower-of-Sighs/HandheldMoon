package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * 1.20.1 entity tint bridge: samples dynamic light at the entity body center
 * and returns a lightmap-aware ARGB coefficient, or -1 when unlit.
 *
 * <p>1.20.1 has no {@code EntityRenderState} on the model compile path, so the
 * entity being rendered is remembered from
 * {@code EntityRenderer#getPackedLightCoords}, which vanilla calls right before
 * every entity render call.</p>
 */
public final class DynamicLightEntityTint {
    private static int currentCoefficient = -1;

    private DynamicLightEntityTint() {
    }

    public static int entityTint(Entity entity) {
        Level level = entity.level();
        if (level == null) {
            return -1;
        }
        double y = entity.getY() + Math.max(0.5, entity.getBbHeight() * 0.5);
        BlockPos pos = BlockPos.containing(entity.getX(), y, entity.getZ());
        double dynamic = DynamicLightManager.getLightLevel(pos);
        int vanilla = level.getBrightness(LightLayer.BLOCK, pos);
        int sky = level.getBrightness(LightLayer.SKY, pos);
        int color = DynamicLightManager.getLightColor(pos);
        return LightmapColorBridge.coefficient(dynamic, vanilla, sky, color);
    }

    /** Remembers the entity vanilla is about to render, for {@link #coefficient()}. */
    public static void begin(Entity entity) {
        currentCoefficient = entity instanceof LivingEntity ? entityTint(entity) : -1;
    }

    /** Drops the remembered tint once the entity has been drawn. */
    public static void end() {
        currentCoefficient = -1;
    }

    /** Tint coefficient of the entity currently being rendered, or -1. */
    public static int coefficient() {
        return currentCoefficient;
    }
}
