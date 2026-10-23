package cc.sighs.handheldmoon.registry;

/**
 * 1.20.1 variant: there are no data components, so {@link ModDataComponent} is
 * replaced by {@link cc.sighs.handheldmoon.item.ItemData} and item NBT.
 */
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void initialize() {
        ModBlocks.MOONLIGHT_LAMP.getClass();
        ModItems.MOONLIGHT_LAMP.getClass();
        ModItems.COLOR_LENS.getClass();
        ModItems.MOONLIGHT_WAND.getClass();
        ModEntities.MOONLIGHT.getClass();
        ModBlockEntities.MOONLIGHT_LAMP.getClass();
        ModCreativeModeTab.CATBURGER_TAB.getClass();
        ModMenus.FLASHLIGHT_CONFIG.getClass();
    }
}
