package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HandheldMoon.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MoonlightLampBlockEntity>> MOONLIGHT_LAMP =
            BLOCK_ENTITIES.register("moonlight_lamp", () ->
                    BlockEntityType.Builder.of(
                            MoonlightLampBlockEntity::new,
                            ModBlocks.MOONLIGHT_LAMP.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FullMoonBlockEntity>> FULL_MOON =
            BLOCK_ENTITIES.register("full_moon", () ->
                    BlockEntityType.Builder.of(
                            FullMoonBlockEntity::new,
                            ModBlocks.FULL_MOON.get()
                    ).build(null)
            );
}
