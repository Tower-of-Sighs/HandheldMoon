package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.oelib.registry.DeferredRegister;
import cc.sighs.oelib.registry.RegisterSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HandheldMoon.MOD_ID);

    public static final RegisterSupplier<CreativeModeTab> CATBURGER_TAB =
            CREATIVE_MODE_TABS.register("catburger_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.tab.handheldmoon"))
                    .icon(() -> ModItems.MOONLIGHT_LAMP.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MOONLIGHT_LAMP.get().getDefaultInstance());
                        output.accept(ModItems.FULL_MOON.get());
                    })
                    .build());
}
