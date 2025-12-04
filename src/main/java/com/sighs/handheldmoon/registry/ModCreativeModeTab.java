package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HandheldMoon.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CATBURGER_TAB =
            CREATIVE_MODE_TABS.register("catburger_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tab.handheldmoon")) // 标题
                    .icon(() -> ModItems.MOONLIGHT_LAMP.get().getDefaultInstance()) // 图标
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MOONLIGHT_LAMP.get());
                        output.accept(ModItems.FULL_MOON.get());
                    })
                    .build());
}
