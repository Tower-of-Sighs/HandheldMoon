package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static BlockEntityType<MoonlightLampBlockEntity> MOONLIGHT_LAMP;
    public static BlockEntityType<FullMoonBlockEntity> FULL_MOON;

    public static void init() {
        MOONLIGHT_LAMP = register(
                "moonlight_lamp",
                BlockEntityType.Builder.of(MoonlightLampBlockEntity::new, ModBlocks.MOONLIGHT_LAMP)
        );
        FULL_MOON = register(
                "full_moon",
                BlockEntityType.Builder.of(FullMoonBlockEntity::new, ModBlocks.FULL_MOON)
        );
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, name),
                builder.build(null)
        );
    }
}
