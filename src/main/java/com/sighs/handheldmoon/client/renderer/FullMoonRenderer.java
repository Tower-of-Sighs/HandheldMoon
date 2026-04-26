package com.sighs.handheldmoon.client.renderer;

import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.registry.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public class FullMoonRenderer extends EntityRenderer<FullMoonEntity> {
    private final ItemRenderer itemRenderer;

    public FullMoonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FullMoonEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isLampBound()) {
            return;
        }

        poseStack.pushPose();
        float scale = 1.0F;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        this.itemRenderer.renderStatic(
                new ItemStack(ModItems.FULL_MOON.get()),
                ItemDisplayContext.GROUND,
                15728880,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FullMoonEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}