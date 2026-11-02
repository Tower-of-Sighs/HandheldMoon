package cc.sighs.handheldmoon.fabric.spi;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.compat.FlashlightCompatHooks;
import cc.sighs.handheldmoon.compat.accessory.AccessoryCompat;
import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.handheldmoon.fabric.compat.tacz.TaczCompat;
import cc.sighs.handheldmoon.fabric.network.FabricNetwork;
import cc.sighs.handheldmoon.fabric.network.ServerToggleAttachmentLampPacket;
import cc.sighs.handheldmoon.item.ColorLensItem;
import cc.sighs.handheldmoon.item.FullMoonItem;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.item.MoonlightWandItem;
import cc.sighs.handheldmoon.network.ServerPacket;
import cc.sighs.handheldmoon.registry.ModRegistries;
import cc.sighs.handheldmoon.spi.AbstractClientPlatformService;
import cc.sighs.handheldmoon.spi.ClientPlatformService;
import cc.sighs.handheldmoon.spi.PlatformService;
import cc.sighs.handheldmoon.spi.RegistryService;
import cc.sighs.handheldmoon.spi.RegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.function.Supplier;

/** Fabric 1.20.1 adapter. Shared registry declarations stay in common. */
public final class FabricPlatformService implements PlatformService {
    private final FabricRegistryService registry = new FabricRegistryService();
    private final ClientPlatformService client = new FabricClientPlatformService();

    @Override
    public Object id(String path) {
        return new ResourceLocation(HandheldMoon.MOD_ID, path);
    }

    @Override
    public void initialize(Object loaderContext) {
        FlashlightCompatHooks.install(
                player -> AccessoryCompat.isUsingAccessoryFlashlight(player)
                        || TaczCompat.isUsingAttachmentFlashlight(player),
                player -> {
                    AccessoryCompat.toggleAccessoryFlashlight(player);
                    TaczCompat.toggleAttachmentFlashlight(player);
                }
        );
        FlashlightCompatHooks.installItemLuminance(player ->
                (TaczCompat.hasMoonlightAttachment(player.getMainHandItem())
                        || TaczCompat.hasMoonlightAttachment(player.getOffhandItem()))
                        && !TaczCompat.isUsingAttachmentFlashlight(player) ? 15 : 0);
        TaczCompat.init();
        TaczCompat.setStateSync(player -> FabricNetwork.send(new ServerToggleAttachmentLampPacket()));
        ModRegistries.initialize();
    }

    @Override
    public RegistryService registry() {
        return registry;
    }

    @Override
    public ClientPlatformService client() {
        return client;
    }

    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    private static final class FabricRegistryService implements RegistryService {
        private static <T> RegistrySupplier<T> supplier(T value) {
            return () -> value;
        }

        private static ResourceLocation identifier(String id) {
            return new ResourceLocation(HandheldMoon.MOD_ID, id);
        }

        @Override
        public <T extends Block> RegistrySupplier<T> registerBlock(String id, Supplier<T> factory) {
            return supplier(Registry.register(BuiltInRegistries.BLOCK, identifier(id), factory.get()));
        }

        @Override
        public RegistrySupplier<? extends Item> registerMoonlightLampItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new MoonlightLampItem()));
        }

        @Override
        public RegistrySupplier<? extends Item> registerFullMoonItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new FullMoonItem()));
        }

        @Override
        public RegistrySupplier<? extends Item> registerColorLensItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new ColorLensItem()));
        }

        @Override
        public RegistrySupplier<? extends Item> registerMoonlightWandItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new MoonlightWandItem()));
        }

        @Override
        public RegistrySupplier<MenuType<?>> registerMenu(String id, Supplier<MenuType<?>> factory) {
            return supplier(Registry.register(BuiltInRegistries.MENU, identifier(id), factory.get()));
        }

        @Override
        public RegistrySupplier<? extends EntityType<?>> registerFullMoonEntity(
                String id, MobCategory category, float width, float height) {
            EntityType.EntityFactory<FullMoonEntity> factory = FullMoonEntity::new;
            return supplier(Registry.register(BuiltInRegistries.ENTITY_TYPE, identifier(id),
                    EntityType.Builder.of(factory, category)
                            .sized(width, height)
                            .build(identifier(id).toString())));
        }

        @Override
        public RegistrySupplier<? extends BlockEntityType<?>> registerMoonlightLampBlockEntity(
                String id, RegistrySupplier<? extends Block> block) {
            return supplier(Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier(id),
                    FabricBlockEntityTypeBuilder.create(MoonlightLampBlockEntity::new, block.get()).build()));
        }

        @Override
        public RegistrySupplier<? extends BlockEntityType<?>> registerFullMoonBlockEntity(
                String id, RegistrySupplier<? extends Block> block) {
            return supplier(Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier(id),
                    FabricBlockEntityTypeBuilder.create(FullMoonBlockEntity::new, block.get()).build()));
        }

        @Override
        public BlockEntity createBlockEntity(String id, BlockPos pos, BlockState state) {
            if (id.equals("moonlight_lamp")) return new MoonlightLampBlockEntity(pos, state);
            if (id.equals("full_moon")) return new FullMoonBlockEntity(pos, state);
            return null;
        }

        @Override
        public RegistrySupplier<CreativeModeTab> registerCreativeModeTab(
                String id,
                RegistrySupplier<? extends Item> lamp,
                RegistrySupplier<? extends Item> fullMoon,
                RegistrySupplier<? extends Item> colorLens,
                RegistrySupplier<? extends Item> wand) {
            CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.tab.handheldmoon"))
                    .icon(() -> lamp.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(lamp.get().getDefaultInstance());
                        output.accept(fullMoon.get().getDefaultInstance());
                        output.accept(colorLens.get().getDefaultInstance());
                        output.accept(wand.get().getDefaultInstance());
                    })
                    .build();
            return supplier(Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, identifier(id), tab));
        }
    }

    private static final class FabricClientPlatformService extends AbstractClientPlatformService {
        @Override
        protected void send(ServerPacket packet) {
            FabricNetwork.send(packet);
        }

        @Override
        public void initializeClient() {
            AccessoryCompat.init();
        }

        @Override
        public void registerKeyBindings() {
            // Key mappings are registered by HandheldMoonFabricClient.
        }

        @Override
        public boolean isAccessoryPlatformAvailable() {
            return FabricLoader.getInstance().isModLoaded("trinkets");
        }
    }
}
