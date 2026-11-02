package cc.sighs.handheldmoon.fabric.mixin;

import cc.sighs.handheldmoon.fabric.HandheldMoonFabricClientEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Feeds raw 1.20.1 key presses to the shared input handler before vanilla routes them. */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void handheldmoon$onKeyPress(
            long window, int key, int scancode, int action, int modifiers, CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (window == minecraft.getWindow().getWindow()
                && minecraft.screen == null
                && minecraft.getOverlay() == null) {
            HandheldMoonFabricClientEvents.onKeyInput(key, action);
        }
    }
}
