package com.sighs.handheldmoon.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FlashlightRender implements TrinketRenderer {

    private static final Minecraft MC = Minecraft.getInstance();
    private final ItemStackRenderState itemState = new ItemStackRenderState();

    @Override
    public void render(
            ItemStack stack,
            SlotReference slotRef,
            EntityModel<? extends LivingEntityRenderState> entityModel,
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            int light,
            LivingEntityRenderState state,
            float limbSwing,
            float limbSwingAmount
    ) {

        MC.getItemModelResolver().updateForTopItem(
                this.itemState,
                stack,
                ItemDisplayContext.NONE,
                MC.level,
                null,
                0
        );

        poseStack.pushPose();

        if (state.hasPose(Pose.CROUCHING)) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
        poseStack.translate(-0.32, -0.05, 0.0);
        poseStack.mulPose(Direction.SOUTH.getRotation());
        poseStack.scale(0.7f, 0.7f, 0.7f);

        // === 渲染物品 ===
        this.itemState.submit(
                poseStack,
                nodes,
                light,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );

        poseStack.popPose();
    }
}
