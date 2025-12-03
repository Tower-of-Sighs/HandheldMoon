package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntities {
    public static BlockEntityType<MoonlightLampBlockEntity> MOONLIGHT_LAMP;

    public static void init() {
        MOONLIGHT_LAMP = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "moonlight_lamp"),
                FabricBlockEntityTypeBuilder.create(MoonlightLampBlockEntity::new, Blocks.MOONLIGHT_LAMP).build(null)
        );
    }
}
