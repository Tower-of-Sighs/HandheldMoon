package cc.sighs.handheldmoon.spi;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public interface RegistryService {
    <T extends Block> RegistrySupplier<T> registerBlock(String id, Supplier<T> factory);

    RegistrySupplier<? extends Item> registerMoonlightLampItem(String id);

    RegistrySupplier<? extends Item> registerFullMoonItem(String id);

    RegistrySupplier<? extends EntityType<?>> registerFullMoonEntity(
            String id, MobCategory category, float width, float height);

    RegistrySupplier<? extends BlockEntityType<?>> registerMoonlightLampBlockEntity(
            String id, RegistrySupplier<? extends Block> block);

    RegistrySupplier<? extends BlockEntityType<?>> registerFullMoonBlockEntity(
            String id, RegistrySupplier<? extends Block> block);

    <T> RegistrySupplier<DataComponentType<T>> registerDataComponent(String id, Supplier<DataComponentType<T>> factory);

    RegistrySupplier<CreativeModeTab> registerCreativeModeTab(
            String id, RegistrySupplier<? extends Item> lamp, RegistrySupplier<? extends Item> fullMoon);

    BlockEntity createBlockEntity(String id, BlockPos pos, BlockState state);
}
