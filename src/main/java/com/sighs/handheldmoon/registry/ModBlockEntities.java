package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static BlockEntityType<MoonlightLampBlockEntity> MOONLIGHT_LAMP;

    public static void init() {
        MOONLIGHT_LAMP = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "moonlight_lamp"),
                BlockEntityType.Builder.of(MoonlightLampBlockEntity::new, ModBlocks.MOONLIGHT_LAMP).build(null)
        );
    }
}
