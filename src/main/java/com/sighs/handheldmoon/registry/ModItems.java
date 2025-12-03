package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    public static MoonlightLampItem MOONLIGHT_LAMP;

    public static void init() {
        MOONLIGHT_LAMP = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "moonlight_lamp"),
                new MoonlightLampItem()
        );
    }
}
