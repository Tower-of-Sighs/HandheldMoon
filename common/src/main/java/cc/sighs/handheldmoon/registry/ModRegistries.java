package cc.sighs.handheldmoon.registry;

/** Initializes common registry declarations after the platform service is available. */
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void initialize() {
        ModBlocks.MOONLIGHT_LAMP.getClass();
        ModItems.MOONLIGHT_LAMP.getClass();
        ModDataComponent.POWERED.getClass();
        ModEntities.MOONLIGHT.getClass();
        ModBlockEntities.MOONLIGHT_LAMP.getClass();
        ModCreativeModeTab.CATBURGER_TAB.getClass();
    }
}
