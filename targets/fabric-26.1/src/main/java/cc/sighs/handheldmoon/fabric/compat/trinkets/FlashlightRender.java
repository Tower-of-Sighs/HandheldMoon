package cc.sighs.handheldmoon.fabric.compat.trinkets;

import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FlashlightRender implements TrinketRenderer {
    public static void register() {
        TrinketRendererRegistry.registerRenderer(ModItems.MOONLIGHT_LAMP.get(), new FlashlightRender());
    }

    @Override
    public void submit(
            ItemStack stack,
            TrinketSlotAccess slotReference,
            EntityModel<? extends LivingEntityRenderState> contextModel,
            PoseStack poseStack,
            SubmitNodeCollector submit,
            int light,
            LivingEntityRenderState state,
            float limbAngle,
            float limbDistance
    ) {
        LivingEntity entity = slotReference.inventory().getAttachment().getEntity();
        if (entity == null) return;

        ItemStackRenderState itemState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver()
                .updateForTopItem(itemState, stack, ItemDisplayContext.NONE, entity.level(), entity, 0);

        poseStack.pushPose();
        if (entity.isCrouching()) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }

        poseStack.translate(-0.32F, -0.05F, 0.0F);
        poseStack.mulPose(Direction.SOUTH.getRotation());
        poseStack.scale(0.7F, 0.7F, 0.7F);
        itemState.submit(poseStack, submit, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
