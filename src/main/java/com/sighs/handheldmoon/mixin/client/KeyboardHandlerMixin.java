package com.sighs.handheldmoon.mixin.client;

import com.sighs.handheldmoon.event.InputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void sophisticatedSorter$onKey(long l, int i, KeyEvent keyEvent, CallbackInfo ci) {
        var window = this.minecraft.getWindow();
        if (l != window.handle()) return;
        InputEvent.KEY.invoker().onKey(keyEvent);
    }
}
