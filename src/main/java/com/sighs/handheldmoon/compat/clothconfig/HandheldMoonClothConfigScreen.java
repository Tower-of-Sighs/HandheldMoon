package com.sighs.handheldmoon.compat.clothconfig;

import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.List;

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
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.light_range"),
                        Config.LIGHT_RANGE.get())
                .setDefaultValue(14.0)
                .setMin(1.0)
                .setMax(64.0)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.light_range.tooltip"))
                .setSaveConsumer(Config.LIGHT_RANGE::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.light_angle"),
                        Config.LIGHT_ANGLE.get())
                .setDefaultValue(56.0)
                .setMin(10.0)
                .setMax(120.0)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.light_angle.tooltip"))
                .setSaveConsumer(Config.LIGHT_ANGLE::set)
                .build());
        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldMoon.client_settings.light_colors_argb"),
                        (List<String>) Config.LIGHT_COLORS_ARGB.get())
                .setDefaultValue(List.of("FFFFFFFF"))
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.light_colors_argb.tooltip"))
                .setSaveConsumer(Config.LIGHT_COLORS_ARGB::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldMoon.client_settings.enable_real_light"),
                        Config.REAL_LIGHT.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.enable_real_light.tooltip", ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getDisplayName()))
                .setSaveConsumer(Config.REAL_LIGHT::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.real_light_luminance"),
                        Config.REAL_LIGHT_LUMINANCE.get())
                .setDefaultValue(15.0)
                .setMin(0.0)
                .setMax(15.0)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.real_light_luminance.tooltip"))
                .setSaveConsumer(Config.REAL_LIGHT_LUMINANCE::set)
                .build());
        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldMoon.client_settings.layer_size_scales"),
                        (List<String>) Config.LAYER_SIZE_SCALES.get())
                .setDefaultValue(List.of("1.00", "1.08", "1.16"))
                .setSaveConsumer(Config.LAYER_SIZE_SCALES::set)
                .build());
        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldMoon.client_settings.layer_center_alphas"),
                        (List<String>) Config.LAYER_CENTER_ALPHAS.get())
                .setDefaultValue(List.of("0.15", "0.12", "0.08"))
                .setSaveConsumer(Config.LAYER_CENTER_ALPHAS::set)
                .build());

        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldMoon.client_settings.layer_edge_alphas"),
                        (List<String>) Config.LAYER_EDGE_ALPHAS.get())
                .setDefaultValue(List.of("0.00", "0.00", "0.00"))
                .setSaveConsumer(Config.LAYER_EDGE_ALPHAS::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.color_noise_amplitude"),
                        Config.COLOR_NOISE_AMPLITUDE.get())
                .setDefaultValue(0.35)
                .setMin(0.0).setMax(1.0)
                .setSaveConsumer(Config.COLOR_NOISE_AMPLITUDE::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldMoon.client_settings.fog_enabled"),
                        Config.FOG_ENABLED.get())
                .setDefaultValue(false)
                .setSaveConsumer(Config.FOG_ENABLED::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.fog_size_scale"),
                        Config.FOG_SIZE_SCALE.get())
                .setDefaultValue(1.30)
                .setMin(1.0)
                .setMax(2.0)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.fog_size_scale.tooltip"))
                .setSaveConsumer(Config.FOG_SIZE_SCALE::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.fog_center_alpha"),
                        Config.FOG_CENTER_ALPHA.get())
                .setDefaultValue(0.06)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.fog_center_alpha.tooltip"))
                .setSaveConsumer(Config.FOG_CENTER_ALPHA::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldMoon.client_settings.fog_edge_alpha"),
                        Config.FOG_EDGE_ALPHA.get())
                .setDefaultValue(0.05)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.fog_edge_alpha.tooltip"))
                .setSaveConsumer(Config.FOG_EDGE_ALPHA::set)
                .build());
        clientSettings.addEntry(entry.startStrField(
                        Component.translatable("config.handheldMoon.client_settings.fog_color_argb"),
                        Config.FOG_COLOR_ARGB.get())
                .setDefaultValue("80FFFFFF")
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.fog_color_argb.tooltip"))
                .setSaveConsumer(Config.FOG_COLOR_ARGB::set)
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
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.handheldMoon.client_settings.enable_cone_raycast.tooltip"))
                .setSaveConsumer(Config.CONE_RAYCAST::set)
                .build());

        return root;
    }

    public static void registerModsPage(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> getConfigBuilder().setParentScreen(parent).build());
    }
}
