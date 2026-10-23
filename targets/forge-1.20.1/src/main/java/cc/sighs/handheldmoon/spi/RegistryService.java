package cc.sighs.handheldmoon.spi;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Minecraft 1.20.1 view of the loader registry. Data components (1.20.5+) are
 * replaced by {@link cc.sighs.handheldmoon.spi.ItemDataPort} on this version.
 */
public interface RegistryService {
    <T extends Block> RegistrySupplier<T> registerBlock(String id, Supplier<T> factory);

    RegistrySupplier<? extends Item> registerMoonlightLampItem(String id);

    RegistrySupplier<? extends Item> registerFullMoonItem(String id);

    RegistrySupplier<? extends Item> registerColorLensItem(String id);

    RegistrySupplier<? extends Item> registerMoonlightWandItem(String id);

    /** Registers a menu type with the loader's menu registry. */
    RegistrySupplier<MenuType<?>> registerMenu(String id, Supplier<MenuType<?>> factory);

    RegistrySupplier<? extends EntityType<?>> registerFullMoonEntity(
            String id, MobCategory category, float width, float height);

    RegistrySupplier<? extends BlockEntityType<?>> registerMoonlightLampBlockEntity(
            String id, RegistrySupplier<? extends Block> block);

    RegistrySupplier<? extends BlockEntityType<?>> registerFullMoonBlockEntity(
            String id, RegistrySupplier<? extends Block> block);

    RegistrySupplier<CreativeModeTab> registerCreativeModeTab(
            String id,
            RegistrySupplier<? extends Item> lamp,
            RegistrySupplier<? extends Item> fullMoon,
            RegistrySupplier<? extends Item> colorLens,
            RegistrySupplier<? extends Item> wand);

    BlockEntity createBlockEntity(String id, BlockPos pos, BlockState state);
}
