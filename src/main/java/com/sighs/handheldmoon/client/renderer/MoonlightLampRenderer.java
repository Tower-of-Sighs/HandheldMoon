package com.sighs.handheldmoon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sighs.handheldmoon.client.HandheldMoonClient;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class MoonlightLampRenderer implements BlockEntityRenderer<MoonlightLampBlockEntity> {

    private final BlockStateModel lampModel;
    private final BlockStateModel lampOnModel;

    public MoonlightLampRenderer(BlockEntityRendererProvider.Context context) {
        FabricBakedModelManager modelManager = Minecraft.getInstance().getModelManager();
        this.lampModel = modelManager.getModel(HandheldMoonClient.MOONLIGHT_LAMP_MODEL_KEY);
        this.lampOnModel = modelManager.getModel(HandheldMoonClient.MOONLIGHT_LAMP_ON_MODEL_KEY);
    }

    @Override
    public void render(MoonlightLampBlockEntity lamp, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 vec3) {

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(lamp.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(lamp.getXRot()));

        poseStack.translate(-0.5, -0.5, -0.5);

        var model = lamp.getPowered() ? lampOnModel : lampModel;
        if (model != null) {
            ModelBlockRenderer.renderModel(
                    poseStack.last(),
                    bufferSource.getBuffer(RenderType.solid()),
                    model,
                    1.0f, 1.0f, 1.0f,
                    packedLight,
                    packedOverlay
            );
        }
        poseStack.popPose();
    }
}