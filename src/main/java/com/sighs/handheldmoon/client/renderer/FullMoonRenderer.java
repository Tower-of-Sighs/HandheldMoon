package com.sighs.handheldmoon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;

public class FullMoonRenderer extends ThrownItemRenderer<FullMoonEntity> {
    public FullMoonRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true);
    }


    @Override
    public void render(ThrownItemRenderState thrownItemRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(thrownItemRenderState, poseStack, multiBufferSource, i);
    }
}
