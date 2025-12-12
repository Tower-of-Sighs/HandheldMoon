package com.sighs.handheldmoon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class MoonlightLampRenderer implements BlockEntityRenderer<MoonlightLampBlockEntity> {

    public MoonlightLampRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MoonlightLampBlockEntity lamp, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel model_on = modelManager.getModel(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp_on"));
        BakedModel model = modelManager.getModel(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp"));

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(lamp.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(lamp.getXRot()));

        poseStack.translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()),
                null,
                lamp.getPowered() ? model_on : model,
                1.0f, 1.0f, 1.0f,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }
}