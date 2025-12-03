package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class ModCreativeModeTab {
    public static final ResourceKey<CreativeModeTab> CATBURGER_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            new ResourceLocation(HandheldMoon.MOD_ID, "catburger_tab")
    );
    public static CreativeModeTab CATBURGER_TAB;

    public static void init() {
        CATBURGER_TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CATBURGER_TAB_KEY,
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.tab.handheldmoon"))
                        .icon(() -> ModItems.MOONLIGHT_LAMP.getDefaultInstance())
                        .displayItems((ctx, entries) -> {
                            entries.accept(ModItems.MOONLIGHT_LAMP);
                        })
                        .build()
        );
    }

}
