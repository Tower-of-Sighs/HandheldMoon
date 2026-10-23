package cc.sighs.handheldmoon.fabric.mixin;

import cc.sighs.handheldmoon.fabric.HandheldMoonFabricClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Feeds raw 1.20.1 mouse input to the shared handlers and lets them cancel it. */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void handheldmoon$onPress(
            long window, int button, int action, int modifiers, CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (window == minecraft.getWindow().getWindow()
                && minecraft.screen == null
                && minecraft.getOverlay() == null) {
            if (HandheldMoonFabricClientEvents.onMouseButton(button, action)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void handheldmoon$onScroll(
            long window, double horizontal, double vertical, CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (window == minecraft.getWindow().getWindow()
                && minecraft.screen == null
                && minecraft.getOverlay() == null) {
            if (HandheldMoonFabricClientEvents.onMouseScroll(vertical)) {
                ci.cancel();
            }
        }
    }
}
