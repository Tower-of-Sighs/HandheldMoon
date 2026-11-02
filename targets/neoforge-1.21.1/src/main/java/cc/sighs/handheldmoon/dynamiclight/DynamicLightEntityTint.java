package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * 1.21.1 entity tint bridge: samples dynamic light at the entity body center
 * and returns a lightmap-aware ARGB coefficient, or -1 when unlit.
 */
public final class DynamicLightEntityTint {
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
}
