package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.FullMoonBlock;
import com.sighs.handheldmoon.block.MoonlightLampBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, HandheldMoon.MOD_ID);

    public static final DeferredHolder<Block, MoonlightLampBlock> MOONLIGHT_LAMP =
            BLOCKS.register("moonlight_lamp", MoonlightLampBlock::new);

    public static final DeferredHolder<Block, FullMoonBlock> FULL_MOON =
            BLOCKS.register("full_moon", FullMoonBlock::new);
}
