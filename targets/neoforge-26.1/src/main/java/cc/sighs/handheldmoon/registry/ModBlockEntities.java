package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.oelib.registry.DeferredRegister;
import cc.sighs.oelib.registry.RegisterSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HandheldMoon.MOD_ID);

    public static final RegisterSupplier<BlockEntityType<MoonlightLampBlockEntity>> MOONLIGHT_LAMP =
            BLOCK_ENTITIES.register("moonlight_lamp", () ->
                    createType(
                            MoonlightLampBlockEntity::new,
                            ModBlocks.MOONLIGHT_LAMP.get()
                    )
            );

    public static final RegisterSupplier<BlockEntityType<FullMoonBlockEntity>> FULL_MOON =
            BLOCK_ENTITIES.register("full_moon", () ->
                    createType(
                            FullMoonBlockEntity::new,
                            ModBlocks.FULL_MOON.get()
                    )
            );

    private static <T extends BlockEntity> BlockEntityType<T> createType(BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... validBlocks) {
        return new BlockEntityType<>(factory, Set.of(validBlocks));
    }
}
