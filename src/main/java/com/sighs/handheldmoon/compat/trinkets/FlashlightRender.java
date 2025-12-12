package com.sighs.handheldmoon.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sighs.handheldmoon.registry.ModItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FlashlightRender implements TrinketRenderer {

    private static final Minecraft MC = Minecraft.getInstance();

    public static void register() {
        TrinketRendererRegistry.registerRenderer(ModItems.MOONLIGHT_LAMP, new FlashlightRender());
    }

    @Override
    public void render(
            ItemStack itemStack,
            SlotReference slotReference,
            EntityModel<? extends LivingEntityRenderState> entityModel,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int light,
            LivingEntityRenderState state,
            float v, float v1
    ) {
        MC.getItemModelResolver().updateForTopItem(
                state.headItem,
                TrinketsCompat.getFirstFlashlight(MC.player),
                ItemDisplayContext.NONE,
                MC.level,
                MC.player,
                1

        );
        poseStack.pushPose();

        if (state.hasPose(Pose.CROUCHING)) {
            poseStack.translate(0.0F, 0.2F, -0.0F);
        }

        poseStack.translate(-0.32, -0.05, 0.0);
        poseStack.mulPose(Direction.SOUTH.getRotation());
        poseStack.scale(0.7F, 0.7F, 0.7F);

        state.headItem.render(
                poseStack,
                multiBufferSource,
                light,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
