package com.sighs.handheldmoon.mixin;

import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.compat.TaczCompat;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.event.Cache;
import com.sighs.handheldmoon.init.LightHandler;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.registry.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

@Mixin(value = SodiumDynamicLights.class, remap = false)
public abstract class DirectionalLightMixin {
    @Inject(method = "maxDynamicLightLevel", at = @At("HEAD"), cancellable = true)
    private static void onMaxDynamicLightLevel(BlockPos pos, DynamicLightSource lightSource, double currentLightLevel, CallbackInfoReturnable<Double> cir) {
        LightHandler.entityLight(pos, lightSource, currentLightLevel, cir);
    }

    @Inject(method = "getLivingEntityLuminanceFromItems", at = @At("HEAD"), cancellable = true)
    private static void enforce(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        LightHandler.selfLight(entity, cir);
    }
}
