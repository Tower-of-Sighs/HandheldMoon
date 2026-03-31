package cc.sighs.handheldmoon.fabric.mixin;

import cc.sighs.handheldmoon.fabric.HandheldMoonFabricClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void handheldmoon$onButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (handle == mc.getWindow().handle() && mc.screen == null && mc.getOverlay() == null) {
            if (HandheldMoonFabricClientEvents.onMouseButton(rawButtonInfo.button(), action)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void handheldmoon$onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (handle == mc.getWindow().handle() && mc.screen == null && mc.getOverlay() == null) {
            if (HandheldMoonFabricClientEvents.onMouseScroll(yoffset)) {
                ci.cancel();
            }
        }
    }
}
