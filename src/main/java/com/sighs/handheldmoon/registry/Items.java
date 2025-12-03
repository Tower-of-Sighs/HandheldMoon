package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.Item.FullMoonItem;
import com.sighs.handheldmoon.Item.MoonlightLampItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sighs.handheldmoon.HandheldMoon.MODID;

public class Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> MOONLIGHT_LAMP = ITEMS.register("moonlight_lamp", MoonlightLampItem::new);
    public static final RegistryObject<Item> FULL_MOON = ITEMS.register("full_moon", FullMoonItem::new);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    static {
        CREATIVE_MODE_TABS.register(MODID, () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.tab.handheldmoon")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> MOONLIGHT_LAMP.get().getDefaultInstance()).displayItems((parameters, output) -> {
            output.accept(FULL_MOON.get());
            output.accept(MOONLIGHT_LAMP.get());
        }).build());
    }
}
