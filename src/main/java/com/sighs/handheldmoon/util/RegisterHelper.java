package com.sighs.handheldmoon.util;

import com.sighs.handheldmoon.HandheldMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class RegisterHelper {
    public static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, id(name));
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, name);
    }
}
