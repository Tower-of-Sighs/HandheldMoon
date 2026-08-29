package cc.sighs.handheldmoon.neoforge.compat.curios;

import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class FlashlightRender implements ICurioRenderer {
    public static void register() {
        ICurioRenderer.register(ModItems.MOONLIGHT_LAMP.get(), FlashlightRender::new);
    }

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            S renderState,
            RenderLayerParent<S, M> renderLayerParent,
            EntityRendererProvider.Context context,
            float yRotation,
            float xRotation
    ) {
        LivingEntity livingEntity = slotContext.entity();
        ItemStackRenderState itemState = new ItemStackRenderState();
        context.getItemModelResolver().updateForTopItem(itemState, stack, ItemDisplayContext.NONE, livingEntity.level(), livingEntity, 0);

        poseStack.pushPose();
        if (livingEntity.isCrouching()) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }

        poseStack.translate(-0.32F, -0.05F, 0.0F);
        poseStack.mulPose(Direction.SOUTH.getRotation());
        poseStack.scale(0.7F, 0.7F, 0.7F);
        itemState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
