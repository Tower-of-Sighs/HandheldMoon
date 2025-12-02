package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sighs.handheldmoon.HandheldMoon.MODID;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<BlockEntityType<MoonlightLampBlockEntity>> MOONLIGHT_LAMP =
            BLOCK_ENTITIES.register("moonlight_lamp",
                    () -> BlockEntityType.Builder.of(MoonlightLampBlockEntity::new, Blocks.MOONLIGHT_LAMP.get()).build(null));
}
