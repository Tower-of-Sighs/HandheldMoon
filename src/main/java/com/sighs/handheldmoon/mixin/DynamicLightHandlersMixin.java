package com.sighs.handheldmoon.mixin;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.registry.BlockEntities;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DynamicLightHandlers.class, remap = false)
public abstract class DynamicLightHandlersMixin {
    @Shadow
    public static @Nullable <T extends BlockEntity> DynamicLightHandler<T> getDynamicLightHandler(BlockEntityType<BeaconBlockEntity> type) {
        return null;
    }

    @Shadow
    public static <T extends BlockEntity> void registerDynamicLightHandler(BlockEntityType<T> type, DynamicLightHandler<T> handler) {
    }

    @Inject(method = "getLuminanceFrom(Lnet/minecraft/world/level/block/entity/BlockEntity;)I", at = @At("HEAD"), cancellable = true)
    private static <T extends BlockEntity> void enforce1(T entity, CallbackInfoReturnable<Integer> cir) {
//        System.out.print(entity+"\n");
        if (entity instanceof MoonlightLampBlockEntity lamp) {
            cir.setReturnValue(15);
        }
    }
    @Inject(method = "getDynamicLightHandler(Lnet/minecraft/world/level/block/entity/BlockEntityType;)Ldev/lambdaurora/lambdynlights/api/DynamicLightHandler;", at = @At("HEAD"), cancellable = true)
    private static <T extends BlockEntity> void enforce2(BlockEntityType<T> type, CallbackInfoReturnable<DynamicLightHandler<T>> cir) {
        System.out.print(type+"111\n");
        if (type == BlockEntities.MOONLIGHT_LAMP.get()) {
            System.out.print(type+"111\n");
            cir.setReturnValue(getDynamicLightHandler(BlockEntityType.BEACON));
        }
    }
    @Inject(method = "registerDefaultHandlers", at = @At("HEAD"))
    private static void register(CallbackInfo ci) {
        registerDynamicLightHandler(BlockEntities.MOONLIGHT_LAMP.get(), (entity) -> 15);
    }
}
