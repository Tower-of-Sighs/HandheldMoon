package com.sighs.handheldmoon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class FullMoonRenderer extends ThrownItemRenderer<FullMoonEntity> {
    public FullMoonRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true);
    }

    @Override
    public void render(FullMoonEntity entity, float f1, float f2, PoseStack poseStack, MultiBufferSource bufferSource, int i) {
        if (entity.level().getBlockEntity(entity.blockPosition()) instanceof MoonlightLampBlockEntity) return;
        super.render(entity, f1, f2, poseStack, bufferSource, i);
    }
}
