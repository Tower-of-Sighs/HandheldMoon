package com.sighs.handheldmoon.compat.clothconfig;

import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class HandheldMoonClothConfigScreen {
    private HandheldMoonClothConfigScreen() {
    }

    private static final String TRANSLATE_TITLE = "config.handheldMoon.title";
    private static final String TRANSLATE_CLIENT_SETTINGS = "config.handheldMoon.client_settings";

    public static ConfigBuilder getConfigBuilder() {
        var root = ConfigBuilder.create().setTitle(Component.translatable(TRANSLATE_TITLE));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        var entry = root.entryBuilder();

        var clientSettings = root.getOrCreateCategory(Component.translatable(TRANSLATE_CLIENT_SETTINGS));
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldMoon.client_settings.enable_fixed_flashlight"),
                        Config.ENABLE_FIXED_FLASHLIGHT.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.enable_fixed_flashlight.tooltip"))
                .setSaveConsumer(Config.ENABLE_FIXED_FLASHLIGHT::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldMoon.client_settings.enable_player_ray"),
                        Config.PLAYER_RAY.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.enable_player_ray.tooltip", ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getDisplayName()))
                .setSaveConsumer(Config.PLAYER_RAY::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldMoon.client_settings.enable_real_light"),
                        Config.REAL_LIGHT.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.enable_real_light.tooltip", ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getDisplayName()))
                .setSaveConsumer(Config.REAL_LIGHT::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.light_intensity"),
                        Config.LIGHT_INTENSITY.get())
                .setDefaultValue(0.3)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.light_intensity.tooltip", ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getDisplayName()))
                .setSaveConsumer(Config.LIGHT_INTENSITY::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldMoon.client_settings.enable_light_occlusion"),
                        Config.LIGHT_OCCLUSION.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.enable_light_occlusion.tooltip"))
                .setSaveConsumer(Config.LIGHT_OCCLUSION::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldMoon.client_settings.enable_cone_raycast"),
                        Config.CONE_RAYCAST.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.enable_cone_raycast.tooltip"))
                .setSaveConsumer(Config.CONE_RAYCAST::set)
                .build());

        return root;
    }

    public static void registerModsPage(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> getConfigBuilder().setParentScreen(parent).build());
    }
}
