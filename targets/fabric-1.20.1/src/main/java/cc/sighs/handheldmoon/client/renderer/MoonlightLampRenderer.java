package cc.sighs.handheldmoon.client.renderer;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.20.1 variant of the placed-lamp renderer. Item models are still looked up
 * through {@link ModelResourceLocation} with the {@code inventory} variant, and
 * the quad pipeline is the fixed-function {@link RenderType#solid()} one.
 */
public class MoonlightLampRenderer implements BlockEntityRenderer<MoonlightLampBlockEntity> {

    public MoonlightLampRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            MoonlightLampBlockEntity lamp, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay
    ) {
        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel modelOn = modelManager.getModel(new ModelResourceLocation(
                new ResourceLocation(HandheldMoon.MOD_ID, "moonlight_lamp_on"), "inventory"));
        BakedModel model = modelManager.getModel(new ModelResourceLocation(
                new ResourceLocation(HandheldMoon.MOD_ID, "moonlight_lamp"), "inventory"));

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(lamp.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(lamp.getXRot()));

        poseStack.translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()),
                null,
                lamp.getPowered() ? modelOn : model,
                1.0f, 1.0f, 1.0f,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }
}
