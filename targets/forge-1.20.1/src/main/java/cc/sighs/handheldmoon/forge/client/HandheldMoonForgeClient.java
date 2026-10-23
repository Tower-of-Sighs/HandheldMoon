package cc.sighs.handheldmoon.forge.client;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.api.raycone.impl.RayConeRendererImpl;
import cc.sighs.handheldmoon.client.renderer.FullMoonRenderer;
import cc.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import cc.sighs.handheldmoon.event.handler.LampConeSourceHooks;
import cc.sighs.handheldmoon.forge.event.handler.LampConeSourceProvider;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.inventory.MenuType;
import cc.sighs.handheldmoon.client.screen.FlashlightItemConfigScreen;
import cc.sighs.handheldmoon.item.ColorLensItem;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import cc.sighs.handheldmoon.registry.ModKeyBindings;
import cc.sighs.handheldmoon.registry.ModMenus;

/** Forge 1.20.1 client bootstrap: models, renderers, key bindings and menus. */
public final class HandheldMoonForgeClient {
    private HandheldMoonForgeClient() {
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RayConeRenderer.installBackend(RayConeRendererImpl::render);
            LampConeSourceHooks.install(LampConeSourceProvider::append);
            ItemProperties.register(
                    ModItems.MOONLIGHT_LAMP.get(),
                    new ResourceLocation(HandheldMoon.MOD_ID, "powered"),
                    (stack, level, entity, seed) -> MoonlightLampItem.getPowered(stack)
            );
            registerMenuScreens();
        });
    }

    @SuppressWarnings("unchecked")
    static void registerMenuScreens() {
        MenuType<FlashlightConfigMenu> menuType =
                (MenuType<FlashlightConfigMenu>) (MenuType<?>) ModMenus.FLASHLIGHT_CONFIG.get();
        MenuScreens.register(menuType, FlashlightItemConfigScreen::new);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.FLASHLIGHT_SWITCH);
        event.register(ModKeyBindings.OPEN_MOD_CONFIG);
        event.register(ModKeyBindings.CONE_SWITCH);
    }

    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // ItemColors expects ARGB; lens colors are stored as 0xRRGGBB.
        event.register((stack, tintIndex) -> 0xFF000000 | ColorLensItem.getColor(stack),
                ModItems.COLOR_LENS.get());
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (net.minecraft.world.level.block.entity.BlockEntityType<cc.sighs.handheldmoon.block.MoonlightLampBlockEntity>)
                        ModBlockEntities.MOONLIGHT_LAMP.get(), MoonlightLampRenderer::new);
        event.registerEntityRenderer(
                (net.minecraft.world.entity.EntityType<cc.sighs.handheldmoon.entity.FullMoonEntity>)
                        ModEntities.MOONLIGHT.get(), FullMoonRenderer::new);
    }

    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(
                new ResourceLocation(HandheldMoon.MOD_ID, "moonlight_lamp"), "inventory"));
        event.register(new ModelResourceLocation(
                new ResourceLocation(HandheldMoon.MOD_ID, "moonlight_lamp_on"), "inventory"));
    }
}
