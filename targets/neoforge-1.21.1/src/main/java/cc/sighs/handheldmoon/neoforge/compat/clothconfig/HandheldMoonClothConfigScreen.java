package cc.sighs.handheldmoon.neoforge.compat.clothconfig;

import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.neoforge.registry.ModKeyBindings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.List;

public class HandheldMoonClothConfigScreen {
    private HandheldMoonClothConfigScreen() {
    }

    private static final String TRANSLATE_TITLE = "config.handheldmoon.title";
    private static final String TRANSLATE_CLIENT_SETTINGS = "config.handheldmoon.client_settings.title";

    public static ConfigBuilder getConfigBuilder() {
        var root = ConfigBuilder.create().setTitle(Component.translatable(TRANSLATE_TITLE));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        var entry = root.entryBuilder();

        var clientSettings = root.getOrCreateCategory(Component.translatable(TRANSLATE_CLIENT_SETTINGS));
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldmoon.client_settings.enableFixedFlashlight"),
                        Config.ENABLE_FIXED_FLASHLIGHT.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.enableFixedFlashlight.tooltip"))
                .setSaveConsumer(Config.ENABLE_FIXED_FLASHLIGHT::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldmoon.client_settings.enablePlayerRay"),
                        Config.PLAYER_RAY.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.enablePlayerRay.tooltip", ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getDisplayName()))
                .setSaveConsumer(Config.PLAYER_RAY::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.lightRange"),
                        Config.LIGHT_RANGE.get())
                .setDefaultValue(14.0)
                .setMin(1.0)
                .setMax(64.0)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.lightRange.tooltip"))
                .setSaveConsumer(Config.LIGHT_RANGE::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.lightAngle"),
                        Config.LIGHT_ANGLE.get())
                .setDefaultValue(56.0)
                .setMin(10.0)
                .setMax(120.0)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.lightAngle.tooltip"))
                .setSaveConsumer(Config.LIGHT_ANGLE::set)
                .build());
        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldmoon.client_settings.lightColorsARGB"),
                        (List<String>) Config.LIGHT_COLORS_ARGB.get())
                .setDefaultValue(List.of("FFFFFFFF"))
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.lightColorsARGB.tooltip"))
                .setSaveConsumer(Config.LIGHT_COLORS_ARGB::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldmoon.client_settings.enableRealLight"),
                        Config.REAL_LIGHT.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.enableRealLight.tooltip", ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getDisplayName()))
                .setSaveConsumer(Config.REAL_LIGHT::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.realLightLuminance"),
                        Config.REAL_LIGHT_LUMINANCE.get())
                .setDefaultValue(15.0)
                .setMin(0.0)
                .setMax(15.0)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.realLightLuminance.tooltip"))
                .setSaveConsumer(Config.REAL_LIGHT_LUMINANCE::set)
                .build());
        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldmoon.client_settings.layerSizeScales"),
                        (List<String>) Config.LAYER_SIZE_SCALES.get())
                .setDefaultValue(List.of("1.00", "1.08", "1.16"))
                .setSaveConsumer(Config.LAYER_SIZE_SCALES::set)
                .build());
        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldmoon.client_settings.layerCenterAlphas"),
                        (List<String>) Config.LAYER_CENTER_ALPHAS.get())
                .setDefaultValue(List.of("0.15", "0.12", "0.08"))
                .setSaveConsumer(Config.LAYER_CENTER_ALPHAS::set)
                .build());

        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldmoon.client_settings.layerEdgeAlphas"),
                        (List<String>) Config.LAYER_EDGE_ALPHAS.get())
                .setDefaultValue(List.of("0.00", "0.00", "0.00"))
                .setSaveConsumer(Config.LAYER_EDGE_ALPHAS::set)
                .build());
        clientSettings.addEntry(entry.startStrList(
                        Component.translatable("config.handheldmoon.client_settings.layerColorsARGB"),
                        (List<String>) Config.LAYER_COLORS_ARGB.get())
                .setDefaultValue(List.of())
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.layerColorsARGB.tooltip"))
                .setSaveConsumer(Config.LAYER_COLORS_ARGB::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.colorNoiseAmplitude"),
                        Config.COLOR_NOISE_AMPLITUDE.get())
                .setDefaultValue(0.35)
                .setMin(0.0).setMax(1.0)
                .setSaveConsumer(Config.COLOR_NOISE_AMPLITUDE::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldmoon.client_settings.fogEnabled"),
                        Config.FOG_ENABLED.get())
                .setDefaultValue(false)
                .setSaveConsumer(Config.FOG_ENABLED::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.fogSizeScale"),
                        Config.FOG_SIZE_SCALE.get())
                .setDefaultValue(1.30)
                .setMin(1.0)
                .setMax(2.0)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.fogSizeScale.tooltip"))
                .setSaveConsumer(Config.FOG_SIZE_SCALE::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.fogCenterAlpha"),
                        Config.FOG_CENTER_ALPHA.get())
                .setDefaultValue(0.06)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.fogCenterAlpha.tooltip"))
                .setSaveConsumer(Config.FOG_CENTER_ALPHA::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.fogEdgeAlpha"),
                        Config.FOG_EDGE_ALPHA.get())
                .setDefaultValue(0.05)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.fogEdgeAlpha.tooltip"))
                .setSaveConsumer(Config.FOG_EDGE_ALPHA::set)
                .build());
        clientSettings.addEntry(entry.startStrField(
                        Component.translatable("config.handheldmoon.client_settings.fogColorARGB"),
                        Config.FOG_COLOR_ARGB.get())
                .setDefaultValue("80FFFFFF")
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.fogColorARGB.tooltip"))
                .setSaveConsumer(Config.FOG_COLOR_ARGB::set)
                .build());
        clientSettings.addEntry(entry.startDoubleField(
                        Component.translatable("config.handheldmoon.client_settings.lightIntensity"),
                        Config.LIGHT_INTENSITY.get())
                .setDefaultValue(0.3)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.lightIntensity.tooltip", ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getDisplayName()))
                .setSaveConsumer(Config.LIGHT_INTENSITY::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldmoon.client_settings.enableLightOcclusion"),
                        Config.LIGHT_OCCLUSION.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.enableLightOcclusion.tooltip"))
                .setSaveConsumer(Config.LIGHT_OCCLUSION::set)
                .build());
        clientSettings.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.handheldmoon.client_settings.enableConeRaycast"),
                        Config.CONE_RAYCAST.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.handheldmoon.client_settings.enableConeRaycast.tooltip"))
                .setSaveConsumer(Config.CONE_RAYCAST::set)
                .build());

        return root;
    }

    public static void registerModsPage(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> getConfigBuilder().setParentScreen(parent).build());
    }
}
