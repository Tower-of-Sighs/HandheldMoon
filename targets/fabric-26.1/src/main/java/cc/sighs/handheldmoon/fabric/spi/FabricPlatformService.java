package cc.sighs.handheldmoon.fabric.spi;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.registry.ModKeyBindings;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.item.ColorLensItem;
import cc.sighs.handheldmoon.item.FullMoonItem;
import cc.sighs.handheldmoon.item.MoonlightWandItem;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import cc.sighs.handheldmoon.network.ServerOpenFlashlightConfigPacket;
import cc.sighs.handheldmoon.network.ServerOpenLampBlockConfigPacket;
import cc.sighs.handheldmoon.network.ServerSetLightProfilePacket;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.event.handler.PlacedLampConeSources;
import cc.sighs.handheldmoon.event.handler.LampConeSourceHooks;
import cc.sighs.handheldmoon.event.handler.LampInteractionHooks;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.compat.accessory.AccessoryCompat;
import cc.sighs.handheldmoon.registry.ModRegistries;
import cc.sighs.handheldmoon.spi.AbstractClientPlatformService;
import cc.sighs.handheldmoon.spi.ClientPlatformService;
import cc.sighs.handheldmoon.spi.PlatformService;
import cc.sighs.handheldmoon.spi.RegistryService;
import cc.sighs.handheldmoon.spi.RegistrySupplier;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.function.Supplier;

public final class FabricPlatformService implements PlatformService {
    private final FabricRegistryService registry = new FabricRegistryService();
    private final ClientPlatformService client = new FabricClientPlatformService();

    @Override
    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, path);
    }

    @Override
    public java.nio.file.Path configDirectory() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void initialize(Object loaderContext) {
        registerPayloads();
        forceCommonRegistrations();
    }

    private static void forceCommonRegistrations() {
        ModRegistries.initialize();
    }

    private static void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(ServerMoonLightLampSyncPacket.TYPE, ServerMoonLightLampSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerMoonlightLampConfigSyncPacket.TYPE, ServerMoonlightLampConfigSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerFullMoonConfigSyncPacket.TYPE, ServerFullMoonConfigSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerHeldMoonlightLampConfigSyncPacket.TYPE, ServerHeldMoonlightLampConfigSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerHeldFullMoonConfigSyncPacket.TYPE, ServerHeldFullMoonConfigSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerOpenFlashlightConfigPacket.TYPE, ServerOpenFlashlightConfigPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerOpenLampBlockConfigPacket.TYPE, ServerOpenLampBlockConfigPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerSetLightProfilePacket.TYPE, ServerSetLightProfilePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerMoonLightLampSyncPacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerMoonlightLampConfigSyncPacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerFullMoonConfigSyncPacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerHeldMoonlightLampConfigSyncPacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerHeldFullMoonConfigSyncPacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerOpenFlashlightConfigPacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerOpenLampBlockConfigPacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerSetLightProfilePacket.TYPE,
                (message, context) -> context.server().execute(() -> message.apply(context.player())));
    }

    @Override public RegistryService registry() { return registry; }
    @Override public ClientPlatformService client() { return client; }

    private static final class FabricRegistryService implements RegistryService {
        private <T> RegistrySupplier<T> supplier(T value) { return () -> value; }
        @Override public <T extends Block> RegistrySupplier<T> registerBlock(String id, Supplier<T> factory) {
            return supplier(Registry.register(BuiltInRegistries.BLOCK, identifier(id), factory.get()));
        }
        @Override public RegistrySupplier<? extends Item> registerMoonlightLampItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new MoonlightLampItem()));
        }
        @Override public RegistrySupplier<? extends Item> registerFullMoonItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new FullMoonItem()));
        }
        @Override public RegistrySupplier<? extends Item> registerColorLensItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new ColorLensItem()));
        }
        @Override public RegistrySupplier<? extends Item> registerMoonlightWandItem(String id) {
            return supplier(Registry.register(BuiltInRegistries.ITEM, identifier(id), new MoonlightWandItem()));
        }
        @Override public RegistrySupplier<MenuType<?>> registerMenu(String id, Supplier<MenuType<?>> factory) {
            return supplier(Registry.register(BuiltInRegistries.MENU, identifier(id), factory.get()));
        }
        @Override public RegistrySupplier<? extends EntityType<?>> registerFullMoonEntity(String id, MobCategory category, float width, float height) {
            EntityType.EntityFactory<FullMoonEntity> factory = FullMoonEntity::new;
            Identifier identifier = identifier(id);
            return supplier(Registry.register(BuiltInRegistries.ENTITY_TYPE, identifier, EntityType.Builder.of(factory, category).sized(width, height).build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), identifier))));
        }
        @Override public RegistrySupplier<? extends BlockEntityType<?>> registerMoonlightLampBlockEntity(String id, RegistrySupplier<? extends Block> block) {
            return supplier(Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier(id), FabricBlockEntityTypeBuilder.create(MoonlightLampBlockEntity::new, block.get()).build()));
        }
        @Override public RegistrySupplier<? extends BlockEntityType<?>> registerFullMoonBlockEntity(String id, RegistrySupplier<? extends Block> block) {
            return supplier(Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier(id), FabricBlockEntityTypeBuilder.create(FullMoonBlockEntity::new, block.get()).build()));
        }
        @Override public BlockEntity createBlockEntity(String id, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
            if (id.equals("moonlight_lamp")) return new MoonlightLampBlockEntity(pos, state);
            if (id.equals("full_moon")) return new FullMoonBlockEntity(pos, state);
            return null;
        }
        @Override public <T> RegistrySupplier<DataComponentType<T>> registerDataComponent(String id, Supplier<DataComponentType<T>> factory) {
            return supplier(Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, identifier(id), factory.get()));
        }
        @Override public RegistrySupplier<CreativeModeTab> registerCreativeModeTab(String id, RegistrySupplier<? extends Item> lamp, RegistrySupplier<? extends Item> fullMoon, RegistrySupplier<? extends Item> colorLens, RegistrySupplier<? extends Item> wand) {
            CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.tab.handheldmoon"))
                    .icon(() -> lamp.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(lamp.get().getDefaultInstance());
                        output.accept(fullMoon.get());
                        output.accept(colorLens.get().getDefaultInstance());
                        output.accept(wand.get().getDefaultInstance());
                    })
                    .build();
            return supplier(Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, identifier(id), tab));
        }

        private static Identifier identifier(String id) {
            return Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, id);
        }
    }

    private static final class FabricClientPlatformService extends AbstractClientPlatformService {
        @Override public void initializeClient() {
            RayConeRenderer.installBackend(cc.sighs.handheldmoon.api.raycone.impl.RayConeRendererImpl::render);
            LampConeSourceHooks.install((minecraft, sources) -> PlacedLampConeSources.append(
                    minecraft, java.util.List.of(), sources));
            LampInteractionHooks.install(HandheldMoonDynamicLightsInitializer::syncLampBehavior);
            registerKeyBindings();
            @SuppressWarnings("unchecked")
            MenuType<FlashlightConfigMenu> menuType =
                    (MenuType<FlashlightConfigMenu>) (MenuType<?>) cc.sighs.handheldmoon.registry.ModMenus.FLASHLIGHT_CONFIG.get();
            MenuScreens.register(menuType, cc.sighs.handheldmoon.client.screen.FlashlightItemConfigScreen::new);
            AccessoryCompat.init();
        }

        @Override public void registerKeyBindings() {
            KeyMappingHelper.registerKeyMapping(ModKeyBindings.FLASHLIGHT_SWITCH);
            KeyMappingHelper.registerKeyMapping(ModKeyBindings.CONE_SWITCH);
            KeyMappingHelper.registerKeyMapping(ModKeyBindings.OPEN_MOD_CONFIG);
        }

        @Override
        protected void send(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
            ClientPlayNetworking.send(payload);
        }
    }
}
