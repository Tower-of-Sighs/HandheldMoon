package cc.sighs.handheldmoon;

import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.registry.ModBlocks;
import cc.sighs.handheldmoon.registry.ModCreativeModeTab;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public final class HandheldMoon {
    public static final String MOD_ID = "handheldmoon";
    public static final Logger LOGGER = LogUtils.getLogger();

    private HandheldMoon() {
    }

    public static void init() {
        ModBlocks.BLOCKS.register();
        ModCreativeModeTab.CREATIVE_MODE_TABS.register();
        ModDataComponent.DATA_COMPONENT_TYPES.register();
        ModEntities.ENTITY_TYPES.register();
        ModItems.ITEMS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();
        Config.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String formattedMod(String path) {
        return path.formatted(MOD_ID);
    }
}
