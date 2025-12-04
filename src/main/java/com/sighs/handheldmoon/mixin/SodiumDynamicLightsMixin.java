package com.sighs.handheldmoon.mixin;

import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.event.Cache;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Items;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

import java.lang.reflect.Field;

@Mixin(value = SodiumDynamicLights.class, remap = false)
public class SodiumDynamicLightsMixin {
    @Inject(method = "updateTracking", at = @At("HEAD"))
    private static void enforce(DynamicLightSource lightSource, CallbackInfo ci) {
        if (Cache.getSelfLightSourceList().contains(lightSource)) {
            try {
                Field field = Entity.class.getDeclaredField("sodiumdynamiclights$luminance");
                field.setAccessible(true);
                field.setInt(lightSource, 15);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
