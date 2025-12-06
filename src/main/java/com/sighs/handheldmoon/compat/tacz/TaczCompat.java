package com.sighs.handheldmoon.compat.tacz;

import com.mojang.serialization.Codec;
import com.sighs.handheldmoon.HandheldMoon;
import com.tacz.guns.api.item.IGun;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TaczCompat {
    public static DeferredRegister<DataComponentType<?>> DATACOMPOENT__TACZ =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HandheldMoon.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> POWERED__TACZ = DATACOMPOENT__TACZ.register(
            "powered_moonlight_lamp", () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
    );
    private static final String MOD_ID = "tacz";
    private static boolean INSTALLED = false;

    public static void init(IEventBus bus) {
        INSTALLED = ModList.get().isLoaded(MOD_ID);
        if (INSTALLED) {
            DATACOMPOENT__TACZ.register(bus);
        }
    }

    public static boolean isUsingAttachmentFlashlight(Player player) {
        if (INSTALLED) {
            return TaczCompatInner.isUsingAttachmentFlashlight(player);
        }
        return false;
    }

    public static void toggleAttachmentFlashlight(Player player) {
        if (INSTALLED) {
            TaczCompatInner.toggleAttachmentFlashlight(player);
        }
    }

    public static boolean isLampAttachment(ItemStack itemStack) {
        if (INSTALLED) {
            return TaczCompatInner.isLampAttachment(itemStack);
        }
        return false;
    }

    public static boolean hasMoonlightAttachment(ItemStack itemStack) {
        if (INSTALLED) {
            var iGun = IGun.getIGunOrNull(itemStack);
            if (iGun == null) return false;
            return TaczCompatInner.hasMoonlightAttachment(itemStack, iGun);
        }
        return false;
    }
}
