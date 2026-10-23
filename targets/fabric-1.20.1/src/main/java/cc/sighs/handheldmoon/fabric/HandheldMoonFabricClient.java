package cc.sighs.handheldmoon.fabric;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.api.raycone.impl.RayConeRendererImpl;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.client.renderer.FullMoonRenderer;
import cc.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import cc.sighs.handheldmoon.client.screen.FlashlightItemConfigScreen;
import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.handheldmoon.event.handler.EffectManager;
import cc.sighs.handheldmoon.event.handler.LampConeSourceHooks;
import cc.sighs.handheldmoon.fabric.compat.tacz.TaczCompat;
import cc.sighs.handheldmoon.fabric.event.handler.LampConeSourceProvider;
import cc.sighs.handheldmoon.fabric.network.FabricNetwork;
import cc.sighs.handheldmoon.fabric.network.ServerToggleAttachmentLampPacket;
import cc.sighs.handheldmoon.item.ColorLensItem;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.registry.ModKeyBindings;
import cc.sighs.handheldmoon.registry.ModMenus;
import cc.sighs.handheldmoon.spi.PlatformServices;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Fabric 1.20.1 client bootstrap: models, renderers, key bindings and menus. */
public final class HandheldMoonFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RayConeRenderer.installBackend(RayConeRendererImpl::render);
        LampConeSourceHooks.install(LampConeSourceProvider::append);

        PlatformServices.require().client().initializeClient();

        ItemProperties.register(
                ModItems.MOONLIGHT_LAMP.get(),
                new ResourceLocation(HandheldMoon.MOD_ID, "powered"),
                (stack, level, entity, seed) -> MoonlightLampItem.getPowered(stack)
        );

        registerKeyMappings();
        registerMenuScreens();
        registerRenderers();
        registerItemColors();
        registerExtraModels();
        registerReloadListener();

        TaczCompat.setStateSync(player -> FabricNetwork.send(new ServerToggleAttachmentLampPacket()));

        HandheldMoonFabricClientEvents.register();
    }

    private static void registerKeyMappings() {
        KeyBindingHelper.registerKeyBinding(ModKeyBindings.FLASHLIGHT_SWITCH);
        KeyBindingHelper.registerKeyBinding(ModKeyBindings.OPEN_MOD_CONFIG);
        KeyBindingHelper.registerKeyBinding(ModKeyBindings.CONE_SWITCH);
    }

    @SuppressWarnings("unchecked")
    private static void registerMenuScreens() {
        MenuType<FlashlightConfigMenu> menuType =
                (MenuType<FlashlightConfigMenu>) (MenuType<?>) ModMenus.FLASHLIGHT_CONFIG.get();
        MenuScreens.register(menuType, FlashlightItemConfigScreen::new);
    }

    @SuppressWarnings("unchecked")
    private static void registerRenderers() {
        EntityRendererRegistry.register(
                (EntityType<FullMoonEntity>) (EntityType<?>) ModEntities.MOONLIGHT.get(),
                FullMoonRenderer::new);
        BlockEntityRenderers.register(
                (BlockEntityType<MoonlightLampBlockEntity>) (BlockEntityType<?>) ModBlockEntities.MOONLIGHT_LAMP.get(),
                MoonlightLampRenderer::new);
    }

    private static void registerItemColors() {
        // ItemColors expects ARGB; lens colors are stored as 0xRRGGBB.
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> 0xFF000000 | ColorLensItem.getColor(stack),
                ModItems.COLOR_LENS.get());
    }

    private static void registerExtraModels() {
        ModelLoadingPlugin.register(context -> context.addModels(
                new ModelResourceLocation(
                        new ResourceLocation(HandheldMoon.MOD_ID, "moonlight_lamp_on"), "inventory")));
    }

    private static void registerReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(HandheldMoon.MOD_ID, "shader_effects");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        EffectManager.onResourceReload();
                    }
                });
    }
}
