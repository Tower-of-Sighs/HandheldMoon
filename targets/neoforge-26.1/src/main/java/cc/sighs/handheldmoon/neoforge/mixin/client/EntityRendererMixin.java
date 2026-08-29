package cc.sighs.handheldmoon.neoforge.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @WrapOperation(
            method = "getPackedLightCoords",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getBlockLightLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)I"
            )
    )
    private int handheldMoon$onForceEntityLitUp(EntityRenderer<?, ?> instance, Entity entity, BlockPos pos, Operation<Integer> original) {
        if (entity instanceof Player player) {
            if (Utils.isUsingFlashlight(player)) {
                return 15;
            }
        }
        return original.call(instance, entity, pos);
    }
}
