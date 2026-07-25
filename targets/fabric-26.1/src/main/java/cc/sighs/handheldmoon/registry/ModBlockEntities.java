package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.oelib.registry.DeferredRegister;
import cc.sighs.oelib.registry.RegisterSupplier;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HandheldMoon.MOD_ID);

    public static final RegisterSupplier<BlockEntityType<MoonlightLampBlockEntity>> MOONLIGHT_LAMP =
            BLOCK_ENTITIES.register("moonlight_lamp", () ->
                    FabricBlockEntityTypeBuilder.create(
                            MoonlightLampBlockEntity::new,
                            ModBlocks.MOONLIGHT_LAMP.get()
                    ).build()
            );

    public static final RegisterSupplier<BlockEntityType<FullMoonBlockEntity>> FULL_MOON =
            BLOCK_ENTITIES.register("full_moon", () ->
                    FabricBlockEntityTypeBuilder.create(
                            FullMoonBlockEntity::new,
                            ModBlocks.FULL_MOON.get()
                    ).build()
            );
}
