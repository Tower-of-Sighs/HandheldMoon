package com.sighs.handheldmoon.compat.tacz;

import com.mojang.serialization.Codec;
import com.sighs.handheldmoon.HandheldMoon;
import com.tacz.guns.api.item.IGun;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaczCompat {
    public static DataComponentType<Boolean> POWERED__TACZ;
    private static final String MOD_ID = "tacz";
    private static boolean INSTALLED = false;

    public static void init() {
        INSTALLED = FabricLoader.getInstance().isModLoaded(MOD_ID);
        if (INSTALLED) {
            POWERED__TACZ = Registry.register(
                    BuiltInRegistries.DATA_COMPONENT_TYPE,
                    ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "poweredMoonlightLamp"),
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );
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
