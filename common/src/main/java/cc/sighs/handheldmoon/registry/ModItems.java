package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.item.FullMoonItem;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.oelib.registry.DeferredRegister;
import cc.sighs.oelib.registry.RegisterSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, HandheldMoon.MOD_ID);

    public static final RegisterSupplier<MoonlightLampItem> MOONLIGHT_LAMP =
            ITEMS.register("moonlight_lamp", MoonlightLampItem::new);

    public static final RegisterSupplier<FullMoonItem> FULL_MOON =
            ITEMS.register("full_moon", FullMoonItem::new);
}
