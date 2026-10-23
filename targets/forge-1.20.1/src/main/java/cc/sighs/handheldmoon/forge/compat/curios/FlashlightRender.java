package cc.sighs.handheldmoon.forge.compat.curios;

import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Renders the lamp on the shoulder Curios slot. */
public final class FlashlightRender implements ICurioRenderer {
    private static final Minecraft MC = Minecraft.getInstance();

    public static void register() {
        CuriosRendererRegistry.register(ModItems.MOONLIGHT_LAMP.get(), FlashlightRender::new);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        ItemRenderer itemRenderer = MC.getItemRenderer();
        LivingEntity livingEntity = slotContext.entity();
        BakedModel model = itemRenderer.getModel(CuriosCompat.getFirstFlashlight(MC.player), MC.level, MC.player, 1);

        poseStack.pushPose();
        if (livingEntity.isCrouching()) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
        poseStack.translate(-0.32, -0.05, 0.0);
        poseStack.mulPose(Direction.SOUTH.getRotation());
        poseStack.scale(0.7f, 0.7f, 0.7f);
        itemRenderer.render(stack, ItemDisplayContext.NONE, true, poseStack, renderTypeBuffer,
                light, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
    }
}
