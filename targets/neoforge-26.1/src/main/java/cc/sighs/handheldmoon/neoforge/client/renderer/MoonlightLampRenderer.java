package cc.sighs.handheldmoon.neoforge.client.renderer;

import cc.sighs.handheldmoon.neoforge.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MoonlightLampRenderer implements BlockEntityRenderer<MoonlightLampBlockEntity, MoonlightLampRenderer.State> {
    private static final Identifier LAMP_BE_MODEL = Identifier.fromNamespaceAndPath("handheldmoon", "moonlight_lamp_be");
    private static final Identifier LAMP_BE_ON_MODEL = Identifier.fromNamespaceAndPath("handheldmoon", "moonlight_lamp_on_be");
    private final ItemModelResolver itemModelResolver;

    public MoonlightLampRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            MoonlightLampBlockEntity lamp,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(lamp, state, partialTicks, cameraPosition, breakProgress);
        state.xRot = lamp.getXRot();
        state.yRot = lamp.getYRot();

        ItemStack stack = new ItemStack(ModItems.MOONLIGHT_LAMP.get());
        stack.set(DataComponents.ITEM_MODEL, lamp.getPowered() ? LAMP_BE_ON_MODEL : LAMP_BE_MODEL);
        this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.NONE, lamp.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float xRot;
        public float yRot;
    }
}
