package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.FullMoonBlock;
import com.sighs.handheldmoon.block.MoonlightLampBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static MoonlightLampBlock MOONLIGHT_LAMP;
    public static FullMoonBlock FULL_MOON;

    public static void init() {
        MOONLIGHT_LAMP = register("moonlight_lamp", new MoonlightLampBlock());
        FULL_MOON = register("full_moon", new FullMoonBlock());
    }

    public static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK,
                new ResourceLocation(HandheldMoon.MOD_ID, name),
                block
        );
    }

}
