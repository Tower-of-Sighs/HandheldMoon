package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.block.MoonlightLampBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sighs.handheldmoon.HandheldMoon.MODID;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> MOONLIGHT_LAMP = BLOCKS.register("moonlight_lamp", () -> new MoonlightLampBlock(BlockBehaviour.Properties.of().noCollission()));
}
