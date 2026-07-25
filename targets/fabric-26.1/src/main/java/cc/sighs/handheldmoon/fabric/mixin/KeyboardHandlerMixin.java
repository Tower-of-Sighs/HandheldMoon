package cc.sighs.handheldmoon.fabric.mixin;

import cc.sighs.handheldmoon.fabric.HandheldMoonFabricClientEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void handheldmoon$onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (handle == mc.getWindow().handle() && mc.screen == null && mc.getOverlay() == null) {
            HandheldMoonFabricClientEvents.onKeyInput(event.key(), action);
        }
    }
}
