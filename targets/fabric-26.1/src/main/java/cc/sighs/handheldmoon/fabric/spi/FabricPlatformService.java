package cc.sighs.handheldmoon.fabric.spi;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.fabric.registry.ModKeyBindings;
import cc.sighs.handheldmoon.fabric.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.fabric.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.fabric.entity.FullMoonEntity;
import cc.sighs.handheldmoon.fabric.item.MoonlightLampItem;
import cc.sighs.handheldmoon.item.FullMoonItem;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.event.handler.BlockEntityLampConeSources;
import cc.sighs.handheldmoon.event.handler.LampConeSourceHooks;
import cc.sighs.handheldmoon.event.handler.LampInteractionHooks;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.compat.accessory.AccessoryCompat;
import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.registry.ModRegistries;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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
        @Override public RegistrySupplier<CreativeModeTab> registerCreativeModeTab(String id, RegistrySupplier<? extends Item> lamp, RegistrySupplier<? extends Item> fullMoon) {
            CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.tab.handheldmoon"))
                    .icon(() -> lamp.get().getDefaultInstance())
                    .displayItems((parameters, output) -> { output.accept(lamp.get().getDefaultInstance()); output.accept(fullMoon.get()); })
                    .build();
            return supplier(Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, identifier(id), tab));
        }

        private static Identifier identifier(String id) {
            return Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, id);
        }
    }

    private static final class FabricClientPlatformService implements ClientPlatformService {
        @Override public void initializeClient() {
            RayConeRenderer.installBackend(cc.sighs.handheldmoon.fabric.api.raycone.impl.RayConeRendererImpl::render);
            LampConeSourceHooks.install((minecraft, sources) -> BlockEntityLampConeSources.append(
                    minecraft, HandheldMoonDynamicLightsInitializer.getActiveLampPositions(), sources));
            LampInteractionHooks.install(HandheldMoonDynamicLightsInitializer::syncLampBehavior);
            registerKeyBindings();
            AccessoryCompat.init();
        }
        @Override public void tickClient() {
            cc.sighs.handheldmoon.client.ClientRuntime.tick();
            cc.sighs.handheldmoon.fabric.event.handler.ShaderEventHandler.onClientTick();
        }
        @Override public void registerKeyBindings() {
            KeyMappingHelper.registerKeyMapping(ModKeyBindings.FLASHLIGHT_SWITCH);
            KeyMappingHelper.registerKeyMapping(ModKeyBindings.OPEN_DEVICE_CONFIG);
        }
        @Override public int flashlightKeyCode() { return ModKeyBindings.FLASHLIGHT_SWITCH.getDefaultKey().getValue(); }
        @Override public int deviceConfigKeyCode() { return ModKeyBindings.OPEN_DEVICE_CONFIG.getDefaultKey().getValue(); }
        @Override public boolean isFlashlightKeyDown() { return ModKeyBindings.FLASHLIGHT_SWITCH.isDown(); }
        @Override public void openLampConfig(ConfigTarget<cc.sighs.handheldmoon.config.LampDeviceConfig> target) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new cc.sighs.handheldmoon.fabric.client.screen.MoonlightLampDeviceConfigScreen(target));
        }
        @Override public void openFullMoonConfig(ConfigTarget<cc.sighs.handheldmoon.config.FullMoonDeviceConfig> target) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new cc.sighs.handheldmoon.fabric.client.screen.FullMoonDeviceConfigScreen(target));
        }
        @Override public void sendLampState(net.minecraft.core.BlockPos pos, float xRot, float yRot, boolean powered) { ClientPlayNetworking.send(new ServerMoonLightLampSyncPacket(pos, xRot, yRot, powered)); }
        @Override public void sendLampConfig(net.minecraft.core.BlockPos pos, LampDeviceConfig config) { ClientPlayNetworking.send(new ServerMoonlightLampConfigSyncPacket(pos, config)); }
        @Override public void sendFullMoonConfig(net.minecraft.core.BlockPos pos, FullMoonDeviceConfig config) { ClientPlayNetworking.send(new ServerFullMoonConfigSyncPacket(pos, config)); }
        @Override public void sendHeldLampConfig(InteractionHand hand, LampDeviceConfig config) { ClientPlayNetworking.send(new ServerHeldMoonlightLampConfigSyncPacket(hand == InteractionHand.OFF_HAND ? 1 : 0, config)); }
        @Override public void sendHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config) { ClientPlayNetworking.send(new ServerHeldFullMoonConfigSyncPacket(hand == InteractionHand.OFF_HAND ? 1 : 0, config)); }
    }
}
