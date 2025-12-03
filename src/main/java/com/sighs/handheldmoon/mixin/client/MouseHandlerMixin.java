package com.sighs.handheldmoon.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.sighs.handheldmoon.event.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Shadow
    public abstract boolean isLeftPressed();

    @Shadow
    public abstract boolean isMiddlePressed();

    @Shadow
    public abstract boolean isRightPressed();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "onButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void sophisticatedSorter$onMouseButtonPre(long l, MouseButtonInfo mouseButtonInfo, int action, CallbackInfo ci) {
        boolean cancel = InputEvent.MOUSE_BUTTON_PRE.invoker().onMouseButtonPre(mouseButtonInfo, action);
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "onButton", at = @At("TAIL"))
    private void sophisticatedSorter$onMouseButtonPost(long l, MouseButtonInfo mouseButtonInfo, int action, CallbackInfo ci) {
        var window = this.minecraft.getWindow();
        if (l != window.handle()) return;
        InputEvent.MOUSE_BUTTON_POST.invoker().onMouseButtonPost(mouseButtonInfo, action);
    }

    @Inject(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void diligentstalker$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci, @Local(ordinal = 0, argsOnly = true) double g, @Local(ordinal = 1, argsOnly = true) double h) {
        if (InputEvent.MOUSE_SCROLL.invoker().onMouseScroll(
                g, h, xpos, ypos,
                isLeftPressed(), isMiddlePressed(), isRightPressed()
        )) {
            ci.cancel();
        }
    }
}