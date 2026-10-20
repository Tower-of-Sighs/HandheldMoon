package cc.sighs.handheldmoon.neoforge.spi;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerOpenLampBlockConfigPacket;
import cc.sighs.handheldmoon.network.ServerSetLightProfilePacket;
import cc.sighs.handheldmoon.neoforge.registry.ModKeyBindings;
import cc.sighs.handheldmoon.registry.ModRegistries;
import cc.sighs.handheldmoon.neoforge.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.neoforge.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.neoforge.entity.FullMoonEntity;
import cc.sighs.handheldmoon.neoforge.item.MoonlightLampItem;
import cc.sighs.handheldmoon.item.ColorLensItem;
import cc.sighs.handheldmoon.item.FullMoonItem;
import cc.sighs.handheldmoon.item.MoonlightWandItem;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import cc.sighs.handheldmoon.network.ServerOpenFlashlightConfigPacket;
import cc.sighs.handheldmoon.spi.ClientPlatformService;
import cc.sighs.handheldmoon.spi.PlatformService;
import cc.sighs.handheldmoon.spi.RegistryService;
import cc.sighs.handheldmoon.spi.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.event.handler.PlacedLampConeSources;
import cc.sighs.handheldmoon.event.handler.LampConeSourceHooks;
import cc.sighs.handheldmoon.event.handler.LampInteractionHooks;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.compat.accessory.AccessoryCompat;

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public final class NeoForgePlatformService implements PlatformService {
    private final NeoForgeRegistryService registry = new NeoForgeRegistryService();
    private final NeoForgeClientPlatformService client = new NeoForgeClientPlatformService();

    @Override
    public net.minecraft.resources.Identifier id(String path) {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, path);
    }

    @Override
    public void initialize(Object loaderContext) {
        IEventBus eventBus = (IEventBus) loaderContext;
        registry.register(eventBus);
        client.register(eventBus);
        eventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            var registrar = event.registrar(HandheldMoon.MOD_ID);
            registrar.playToServer(ServerMoonLightLampSyncPacket.TYPE, ServerMoonLightLampSyncPacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
            registrar.playToServer(ServerMoonlightLampConfigSyncPacket.TYPE, ServerMoonlightLampConfigSyncPacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
            registrar.playToServer(ServerFullMoonConfigSyncPacket.TYPE, ServerFullMoonConfigSyncPacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
            registrar.playToServer(ServerHeldMoonlightLampConfigSyncPacket.TYPE, ServerHeldMoonlightLampConfigSyncPacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
            registrar.playToServer(ServerHeldFullMoonConfigSyncPacket.TYPE, ServerHeldFullMoonConfigSyncPacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
            registrar.playToServer(ServerOpenFlashlightConfigPacket.TYPE, ServerOpenFlashlightConfigPacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
            registrar.playToServer(ServerOpenLampBlockConfigPacket.TYPE, ServerOpenLampBlockConfigPacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
            registrar.playToServer(ServerSetLightProfilePacket.TYPE, ServerSetLightProfilePacket.STREAM_CODEC,
                    (message, context) -> context.enqueueWork(() -> message.apply((net.minecraft.server.level.ServerPlayer) context.player())));
        });
        ModRegistries.initialize();
    }

    @Override public RegistryService registry() { return registry; }
    @Override public ClientPlatformService client() { return client; }

    @Override
    public java.nio.file.Path configDirectory() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    private static final class NeoForgeRegistryService implements RegistryService {
        private final DeferredRegister<Block> blocks = DeferredRegister.create(Registries.BLOCK, HandheldMoon.MOD_ID);
        private final DeferredRegister<Item> items = DeferredRegister.create(Registries.ITEM, HandheldMoon.MOD_ID);
        private final DeferredRegister<EntityType<?>> entities = DeferredRegister.create(Registries.ENTITY_TYPE, HandheldMoon.MOD_ID);
        private final DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HandheldMoon.MOD_ID);
        private final DeferredRegister<DataComponentType<?>> dataComponents = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HandheldMoon.MOD_ID);
        private final DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HandheldMoon.MOD_ID);
        private final DeferredRegister<MenuType<?>> menus = DeferredRegister.create(Registries.MENU, HandheldMoon.MOD_ID);

        void register(IEventBus eventBus) {
            blocks.register(eventBus); items.register(eventBus); entities.register(eventBus);
            blockEntities.register(eventBus); dataComponents.register(eventBus); creativeTabs.register(eventBus);
            menus.register(eventBus);
        }

        private static <T> RegistrySupplier<T> supplier(Supplier<T> source) { return source::get; }
        private static <T> RegistrySupplier<T> supplierWildcard(Supplier<? extends T> source) { return source::get; }
        @Override public <T extends Block> RegistrySupplier<T> registerBlock(String id, Supplier<T> factory) { return supplier(blocks.register(id, factory)); }
        @Override public RegistrySupplier<? extends Item> registerMoonlightLampItem(String id) { return supplier(items.register(id, MoonlightLampItem::new)); }
        @Override public RegistrySupplier<? extends Item> registerFullMoonItem(String id) { return supplier(items.register(id, FullMoonItem::new)); }
        @Override public RegistrySupplier<? extends Item> registerColorLensItem(String id) { return supplier(items.register(id, ColorLensItem::new)); }
        @Override public RegistrySupplier<? extends Item> registerMoonlightWandItem(String id) { return supplier(items.register(id, MoonlightWandItem::new)); }
        @Override public RegistrySupplier<MenuType<?>> registerMenu(String id, Supplier<MenuType<?>> factory) { return supplierWildcard(menus.register(id, factory)); }
        @Override public RegistrySupplier<? extends EntityType<?>> registerFullMoonEntity(String id, MobCategory category, float width, float height) {
            EntityType.EntityFactory<FullMoonEntity> factory = FullMoonEntity::new;
            return supplier(entities.register(id, () -> EntityType.Builder.of(factory, category).sized(width, height).build(
                    net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, HandheldMoon.id(id)))));
        }
        @Override public RegistrySupplier<? extends BlockEntityType<?>> registerMoonlightLampBlockEntity(String id, RegistrySupplier<? extends Block> block) {
            return supplier(blockEntities.register(id, () -> new BlockEntityType<>(MoonlightLampBlockEntity::new, Set.of(block.get()))));
        }
        @Override public RegistrySupplier<? extends BlockEntityType<?>> registerFullMoonBlockEntity(String id, RegistrySupplier<? extends Block> block) {
            return supplier(blockEntities.register(id, () -> new BlockEntityType<>(FullMoonBlockEntity::new, Set.of(block.get()))));
        }
        @Override public BlockEntity createBlockEntity(String id, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
            if (id.equals("moonlight_lamp")) return new MoonlightLampBlockEntity(pos, state);
            if (id.equals("full_moon")) return new FullMoonBlockEntity(pos, state);
            return null;
        }
        @Override public <T> RegistrySupplier<DataComponentType<T>> registerDataComponent(String id, Supplier<DataComponentType<T>> factory) { return supplier(dataComponents.register(id, factory)); }
        @Override public RegistrySupplier<CreativeModeTab> registerCreativeModeTab(String id, RegistrySupplier<? extends Item> lamp, RegistrySupplier<? extends Item> fullMoon, RegistrySupplier<? extends Item> colorLens, RegistrySupplier<? extends Item> wand) {
            return supplier(creativeTabs.register(id, () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.tab.handheldmoon"))
                    .icon(() -> lamp.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(lamp.get().getDefaultInstance());
                        output.accept(fullMoon.get());
                        output.accept(colorLens.get().getDefaultInstance());
                        output.accept(wand.get().getDefaultInstance());
                    })
                    .build()));
        }
    }

    private static final class NeoForgeClientPlatformService implements ClientPlatformService {
        @Override public void initializeClient() {
            RayConeRenderer.installBackend(cc.sighs.handheldmoon.neoforge.api.raycone.impl.RayConeRendererImpl::render);
            LampConeSourceHooks.install((minecraft, sources) -> PlacedLampConeSources.append(
                    minecraft, java.util.List.of(), sources));
            LampInteractionHooks.install(HandheldMoonDynamicLightsInitializer::syncLampBehavior);
            registerKeyBindings();
            AccessoryCompat.init();
        }

        @Override public void tickClient() {
            cc.sighs.handheldmoon.client.ClientRuntime.tick();
            cc.sighs.handheldmoon.neoforge.event.handler.ShaderEventHandler.onClientTick();
        }

        void register(IEventBus eventBus) {
            eventBus.addListener((RegisterKeyMappingsEvent event) -> {
                event.register(ModKeyBindings.FLASHLIGHT_SWITCH);
                event.register(ModKeyBindings.CONE_SWITCH);
                event.register(ModKeyBindings.OPEN_MOD_CONFIG);
            });
            eventBus.addListener((RegisterMenuScreensEvent event) -> event.register(
                    (MenuType<FlashlightConfigMenu>) (MenuType<?>) cc.sighs.handheldmoon.registry.ModMenus.FLASHLIGHT_CONFIG.get(),
                    cc.sighs.handheldmoon.neoforge.client.screen.FlashlightItemConfigScreen::new));
        }
        @Override public void registerKeyBindings() { }
        @Override public int flashlightKeyCode() { return ModKeyBindings.FLASHLIGHT_SWITCH.getDefaultKey().getValue(); }
        @Override public int coneSwitchKeyCode() { return ModKeyBindings.CONE_SWITCH.getDefaultKey().getValue(); }
        @Override public int modConfigKeyCode() { return ModKeyBindings.OPEN_MOD_CONFIG.getDefaultKey().getValue(); }
        @Override public boolean isFlashlightKeyDown() { return ModKeyBindings.FLASHLIGHT_SWITCH.isDown(); }
        @Override public void requestOpenFlashlightConfig() {
            FlashlightConfigMenu.clearPendingBlockTarget();
            send(new ServerOpenFlashlightConfigPacket());
        }
        @Override public void requestOpenLampConfig(net.minecraft.core.BlockPos pos) {
            FlashlightConfigMenu.expectBlockTarget(pos);
            send(new ServerOpenLampBlockConfigPacket(pos));
        }
        @Override public void openModConfig() {
            net.minecraft.client.Minecraft.getInstance().setScreen(new cc.sighs.handheldmoon.neoforge.client.screen.HandheldMoonConfigScreen());
        }
        @Override public void openLightSourceConfig(cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource source, int entityId, java.util.UUID uuid, String sourceId) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new cc.sighs.handheldmoon.neoforge.client.screen.LightSourceConfigScreen(source, entityId, uuid, sourceId));
        }
        @Override public void sendLightProfile(int entityId, cc.sighs.handheldmoon.api.light.EntityLightProfile profile) {
            send(new ServerSetLightProfilePacket(entityId, profile));
        }
        @Override public void sendLampState(net.minecraft.core.BlockPos pos, float xRot, float yRot, boolean powered) { send(new ServerMoonLightLampSyncPacket(pos, xRot, yRot, powered)); }
        @Override public void sendLampConfig(net.minecraft.core.BlockPos pos, LampDeviceConfig config) { send(new ServerMoonlightLampConfigSyncPacket(pos, config)); }
        @Override public void sendFullMoonConfig(net.minecraft.core.BlockPos pos, FullMoonDeviceConfig config) { send(new ServerFullMoonConfigSyncPacket(pos, config)); }
        @Override public void sendHeldLampConfig(InteractionHand hand, LampDeviceConfig config) { send(new ServerHeldMoonlightLampConfigSyncPacket(hand == InteractionHand.OFF_HAND ? 1 : 0, config)); }
        @Override public void sendHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config) { send(new ServerHeldFullMoonConfigSyncPacket(hand == InteractionHand.OFF_HAND ? 1 : 0, config)); }
        private void send(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) { net.minecraft.client.Minecraft.getInstance().getConnection().send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(payload)); }
    }
}
