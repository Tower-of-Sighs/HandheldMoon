package com.sighs.handheldmoon.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sighs.handheldmoon.registry.ModItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
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
            EntityModel<? extends LivingEntity> entityModel,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int light,
            LivingEntity livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        var itemRenderer = MC.getItemRenderer();
        var spyglassModel = itemRenderer.getModel(
                TrinketsCompat.getFirstFlashlight(MC.player),
                MC.level,
                MC.player,
                1
        );
        poseStack.pushPose();

        if (livingEntity.isCrouching()) {
            poseStack.translate(0.0F, 0.2F, -0.0);
        }

        poseStack.translate(-0.32, -0.05, 0.0);
        poseStack.mulPose(Direction.SOUTH.getRotation());
        poseStack.scale(0.7F, 0.7F, 0.7F);

        itemRenderer.render(
                itemStack,
                ItemDisplayContext.NONE,
                true,
                poseStack,
                multiBufferSource,
                light,
                OverlayTexture.NO_OVERLAY,
                spyglassModel
        );

        poseStack.popPose();
    }
}
