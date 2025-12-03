package com.sighs.handheldmoon.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MoonlightLampRenderer implements BlockEntityRenderer<MoonlightLampBlockEntity> {
    
    public MoonlightLampRenderer(BlockEntityRendererProvider.Context context) { }
    
    @Override
    public void render(MoonlightLampBlockEntity lamp, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel model_on = modelManager.getModel(new ModelResourceLocation(new ResourceLocation("handheldmoon", "moonlight_lamp_on"), "inventory"));
        BakedModel model = modelManager.getModel(new ModelResourceLocation(new ResourceLocation("handheldmoon", "moonlight_lamp"), "inventory"));;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(lamp.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(lamp.getXRot()));

        poseStack.translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.solid()),
                null,
                lamp.getPowered() ? model_on : model,
                1.0f, 1.0f, 1.0f,
                packedLight,
                packedOverlay
        );
        
        poseStack.popPose();
    }
}