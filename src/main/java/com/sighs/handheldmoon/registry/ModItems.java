package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.item.FullMoonItem;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    public static MoonlightLampItem MOONLIGHT_LAMP;
    public static FullMoonItem FULL_MOON;

    public static void init() {
        MOONLIGHT_LAMP = register(
                "moonlight_lamp",
                new MoonlightLampItem()
        );
        FULL_MOON = register(
                "full_moon",
                new FullMoonItem()
        );
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, name),
                item
        );
    }
}
