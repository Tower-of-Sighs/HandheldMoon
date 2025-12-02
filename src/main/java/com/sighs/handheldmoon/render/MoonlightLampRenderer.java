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
    
    public MoonlightLampRenderer(BlockEntityRendererProvider.Context context) {
        // 可以在这里初始化一些渲染相关的资源
    }
    
    @Override
    public void render(MoonlightLampBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(new ResourceLocation("handheldmoon", "moonlight_lamp"), "inventory"));;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(blockEntity.getXRot()));

        poseStack.translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.solid()),
                null,
                model,
                1.0f, 1.0f, 1.0f,
                packedLight,
                packedOverlay
        );
        
        poseStack.popPose();
    }
}