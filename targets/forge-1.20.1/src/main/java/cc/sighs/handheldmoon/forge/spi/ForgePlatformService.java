package cc.sighs.handheldmoon.forge.spi;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.compat.FlashlightCompatHooks;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.forge.compat.curios.CuriosCompat;
import cc.sighs.handheldmoon.forge.compat.tacz.TaczCompat;
import cc.sighs.handheldmoon.forge.network.ForgeNetwork;
import cc.sighs.handheldmoon.forge.network.ServerToggleAttachmentLampPacket;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.handheldmoon.item.ColorLensItem;
import cc.sighs.handheldmoon.item.FullMoonItem;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.item.MoonlightWandItem;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerOpenFlashlightConfigPacket;
import cc.sighs.handheldmoon.network.ServerOpenLampBlockConfigPacket;
import cc.sighs.handheldmoon.network.ServerPacket;
import cc.sighs.handheldmoon.network.ServerSetLightProfilePacket;
import cc.sighs.handheldmoon.registry.ModRegistries;
import cc.sighs.handheldmoon.spi.AbstractClientPlatformService;
import cc.sighs.handheldmoon.spi.ClientPlatformService;
import cc.sighs.handheldmoon.spi.PlatformService;
import cc.sighs.handheldmoon.spi.RegistryService;
import cc.sighs.handheldmoon.spi.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.function.Supplier;

/** Forge 1.20.1 adapter. Shared registry declarations stay in common. */
public final class ForgePlatformService implements PlatformService {
    private final ForgeRegistryService registry = new ForgeRegistryService();
    private final ClientPlatformService client = new ForgeClientPlatformService();

    @Override
    public Object id(String path) {
        return new net.minecraft.resources.ResourceLocation(HandheldMoon.MOD_ID, path);
    }

    @Override
    public void initialize(Object loaderContext) {
        IEventBus eventBus = (IEventBus) loaderContext;
        registry.register(eventBus);
        registerPackets();
        // The mod-construction phase runs before the registry events, so item
        // lookups (Curios renderer registration in particular) belong to the
        // common setup phase.
        eventBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(ForgePlatformService::initializeCompat));
        ModRegistries.initialize();
    }

    private static void initializeCompat() {
        FlashlightCompatHooks.install(
                player -> CuriosCompat.isUsingCuriosFlashlight(player) || TaczCompat.isUsingAttachmentFlashlight(player),
                player -> {
                    CuriosCompat.toggleCuriosFlashlight(player);
                    TaczCompat.toggleAttachmentFlashlight(player);
                }
        );
        FlashlightCompatHooks.installItemLuminance(player ->
                (TaczCompat.hasMoonlightAttachment(player.getMainHandItem())
                        || TaczCompat.hasMoonlightAttachment(player.getOffhandItem()))
                        && !TaczCompat.isUsingAttachmentFlashlight(player) ? 15 : 0);
        TaczCompat.init();
        TaczCompat.setStateSync(player ->
                ForgeNetwork.CHANNEL.sendToServer(new ServerToggleAttachmentLampPacket()));
        CuriosCompat.init();
    }

    private static void registerPackets() {
        int id = 0;
        ForgeNetwork.CHANNEL.messageBuilder(ServerMoonLightLampSyncPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerMoonLightLampSyncPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerMoonlightLampConfigSyncPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerMoonlightLampConfigSyncPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerFullMoonConfigSyncPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerFullMoonConfigSyncPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerHeldMoonlightLampConfigSyncPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerHeldMoonlightLampConfigSyncPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerHeldFullMoonConfigSyncPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerHeldFullMoonConfigSyncPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerOpenFlashlightConfigPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerOpenFlashlightConfigPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerOpenLampBlockConfigPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerOpenLampBlockConfigPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerSetLightProfilePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerSetLightProfilePacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
        ForgeNetwork.CHANNEL.messageBuilder(ServerToggleAttachmentLampPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacket::encode)
                .decoder(ServerToggleAttachmentLampPacket::new)
                .consumerMainThread((message, context) -> message.apply(context.get().getSender()))
                .add();
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
        return FMLPaths.CONFIGDIR.get();
    }

    private static final class ForgeRegistryService implements RegistryService {
        private final DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, HandheldMoon.MOD_ID);
        private final DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, HandheldMoon.MOD_ID);
        private final DeferredRegister<EntityType<?>> entities =
                DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HandheldMoon.MOD_ID);
        private final DeferredRegister<BlockEntityType<?>> blockEntities =
                DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HandheldMoon.MOD_ID);
        private final DeferredRegister<CreativeModeTab> creativeTabs =
                DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HandheldMoon.MOD_ID);
        private final DeferredRegister<MenuType<?>> menus =
                DeferredRegister.create(ForgeRegistries.MENU_TYPES, HandheldMoon.MOD_ID);

        void register(IEventBus eventBus) {
            blocks.register(eventBus);
            items.register(eventBus);
            entities.register(eventBus);
            blockEntities.register(eventBus);
            creativeTabs.register(eventBus);
            menus.register(eventBus);
        }

        private static <T> RegistrySupplier<T> supplier(Supplier<T> source) {
            return source::get;
        }

        @Override
        public <T extends Block> RegistrySupplier<T> registerBlock(String id, Supplier<T> factory) {
            return supplier(blocks.register(id, factory));
        }

        @Override
        public RegistrySupplier<? extends Item> registerMoonlightLampItem(String id) {
            return supplier(items.register(id, MoonlightLampItem::new));
        }

        @Override
        public RegistrySupplier<? extends Item> registerFullMoonItem(String id) {
            return supplier(items.register(id, FullMoonItem::new));
        }

        @Override
        public RegistrySupplier<? extends Item> registerColorLensItem(String id) {
            return supplier(items.register(id, ColorLensItem::new));
        }

        @Override
        public RegistrySupplier<? extends Item> registerMoonlightWandItem(String id) {
            return supplier(items.register(id, MoonlightWandItem::new));
        }

        @Override
        public RegistrySupplier<MenuType<?>> registerMenu(String id, Supplier<MenuType<?>> factory) {
            return supplier(menus.register(id, factory));
        }

        @Override
        public RegistrySupplier<? extends EntityType<?>> registerFullMoonEntity(
                String id, MobCategory category, float width, float height) {
            EntityType.EntityFactory<FullMoonEntity> factory = FullMoonEntity::new;
            return supplier(entities.register(id, () -> EntityType.Builder.of(factory, category)
                    .sized(width, height)
                    .build(HandheldMoon.id(id).toString())));
        }

        @Override
        public RegistrySupplier<? extends BlockEntityType<?>> registerMoonlightLampBlockEntity(
                String id, RegistrySupplier<? extends Block> block) {
            return supplier(blockEntities.register(id, () -> BlockEntityType.Builder.of(
                    MoonlightLampBlockEntity::new, block.get()).build(null)));
        }

        @Override
        public RegistrySupplier<? extends BlockEntityType<?>> registerFullMoonBlockEntity(
                String id, RegistrySupplier<? extends Block> block) {
            return supplier(blockEntities.register(id, () -> BlockEntityType.Builder.of(
                    FullMoonBlockEntity::new, block.get()).build(null)));
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
            return supplier(creativeTabs.register(id, () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.tab.handheldmoon"))
                    .icon(() -> lamp.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(lamp.get().getDefaultInstance());
                        output.accept(fullMoon.get().getDefaultInstance());
                        output.accept(colorLens.get().getDefaultInstance());
                        output.accept(wand.get().getDefaultInstance());
                    })
                    .build()));
        }
    }

    private static final class ForgeClientPlatformService extends AbstractClientPlatformService {
        @Override
        protected void send(ServerPacket packet) {
            ForgeNetwork.CHANNEL.sendToServer(packet);
        }

        @Override
        public void initializeClient() {
        }

        @Override
        public void registerKeyBindings() {
        }

        @Override
        public boolean isAccessoryPlatformAvailable() {
            return ModList.get().isLoaded("curios");
        }
    }
}
