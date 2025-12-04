package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.item.FullMoonItem;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, HandheldMoon.MOD_ID);

    public static final DeferredHolder<Item, MoonlightLampItem> MOONLIGHT_LAMP =
            ITEMS.register("moonlight_lamp", MoonlightLampItem::new);

    public static final DeferredHolder<Item, FullMoonItem> FULL_MOON =
            ITEMS.register("full_moon", FullMoonItem::new);
}
