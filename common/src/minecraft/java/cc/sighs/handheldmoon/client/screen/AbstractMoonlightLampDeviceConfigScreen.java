package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public abstract class AbstractMoonlightLampDeviceConfigScreen extends Screen {
    private enum Page {
        BASIC,
        LAYER,
        FOG
    }

    private final ConfigTarget<LampDeviceConfig> target;
    private final Screen parent;
    private Page page = Page.BASIC;
    private boolean loaded;
    private boolean realLight;
    private boolean lightOcclusion;
    private boolean coneRaycast;
    private boolean fogEnabled;
    private String lightRangeText;
    private String lightAngleText;
    private String lightColorsText;
    private String lightIntensityText;
    private String realLightLuminanceText;
    private String layerSizeScalesText;
    private String layerCenterAlphasText;
    private String layerEdgeAlphasText;
    private String layerColorsText;
    private String colorNoiseAmplitudeText;
    private String fogSizeScaleText;
    private String fogCenterAlphaText;
    private String fogEdgeAlphaText;
    private String fogColorText;

    protected AbstractMoonlightLampDeviceConfigScreen(ConfigTarget<LampDeviceConfig> target) {
        this(target, null);
    }

    protected AbstractMoonlightLampDeviceConfigScreen(ConfigTarget<LampDeviceConfig> target, Screen parent) {
        super(Component.translatable("screen.handheldmoon.device_config.moonlight_lamp"));
        this.target = target;
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (!loaded) {
            loadFromCurrentConfig();
            loaded = true;
        }
        rebuildPage();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void rebuildPage() {
        clearWidgets();
        int panelWidth = Math.min(640, this.width - 24);
        int panelHeight = Math.min(330, this.height - 16);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int contentLeft = left + 14;
        int contentWidth = panelWidth - 28;
        int buttonY = top + panelHeight - 28;
        int rowStep = 22;

        int tabWidth = (contentWidth - 8) / 3;
        addRenderableWidget(Button.builder(Component.translatable("screen.handheldmoon.device_config.page.basic"), button -> switchPage(Page.BASIC))
                .bounds(contentLeft, top + 40, tabWidth, 20)
                .build()).active = this.page != Page.BASIC;
        addRenderableWidget(Button.builder(Component.translatable("screen.handheldmoon.device_config.page.layer"), button -> switchPage(Page.LAYER))
                .bounds(contentLeft + tabWidth + 4, top + 40, tabWidth, 20)
                .build()).active = this.page != Page.LAYER;
        addRenderableWidget(Button.builder(Component.translatable("screen.handheldmoon.device_config.page.fog"), button -> switchPage(Page.FOG))
                .bounds(contentLeft + (tabWidth + 4) * 2, top + 40, tabWidth, 20)
                .build()).active = this.page != Page.FOG;

        int y = top + 66;
        if (page == Page.BASIC) {
            addRenderableWidget(makeToggle(Component.translatable("config.handheldmoon.client_settings.enableRealLight"), contentLeft, y, contentWidth, realLight, value -> realLight = value));
            y += rowStep;
            addRenderableWidget(makeToggle(Component.translatable("config.handheldmoon.client_settings.enableLightOcclusion"), contentLeft, y, contentWidth, lightOcclusion, value -> lightOcclusion = value));
            y += rowStep;
            addRenderableWidget(makeToggle(Component.translatable("config.handheldmoon.client_settings.enableConeRaycast"), contentLeft, y, contentWidth, coneRaycast, value -> coneRaycast = value));
            y += rowStep;
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.lightRange"), lightRangeText, value -> lightRangeText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.lightAngle"), lightAngleText, value -> lightAngleText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.realLightLuminance"), realLightLuminanceText, value -> realLightLuminanceText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.lightIntensity"), lightIntensityText, value -> lightIntensityText = value);
            addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.lightColorsARGB"), lightColorsText, value -> lightColorsText = value);
        } else if (page == Page.LAYER) {
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.layerSizeScales"), layerSizeScalesText, value -> layerSizeScalesText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.layerCenterAlphas"), layerCenterAlphasText, value -> layerCenterAlphasText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.layerEdgeAlphas"), layerEdgeAlphasText, value -> layerEdgeAlphasText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.layerColorsARGB"), layerColorsText, value -> layerColorsText = value);
            addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.colorNoiseAmplitude"), colorNoiseAmplitudeText, value -> colorNoiseAmplitudeText = value);
        } else {
            addRenderableWidget(makeToggle(Component.translatable("config.handheldmoon.client_settings.fogEnabled"), contentLeft, y, contentWidth, fogEnabled, value -> fogEnabled = value));
            y += rowStep;
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.fogSizeScale"), fogSizeScaleText, value -> fogSizeScaleText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.fogCenterAlpha"), fogCenterAlphaText, value -> fogCenterAlphaText = value);
            y = addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.fogEdgeAlpha"), fogEdgeAlphaText, value -> fogEdgeAlphaText = value);
            addLabeledEditRow(contentLeft, contentWidth, y, Component.translatable("config.handheldmoon.client_settings.fogColorARGB"), fogColorText, value -> fogColorText = value);
        }

        int half = (contentWidth - 8) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> applyAndClose())
                .bounds(contentLeft, buttonY, half, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(contentLeft + half + 8, buttonY, half, 20)
                .build());
    }

    private int addLabeledEditRow(int contentLeft, int contentWidth, int y, Component label, String initialValue, Consumer<String> saver) {
        int labelWidth = Math.min(240, Math.max(130, contentWidth / 2 - 6));
        int fieldX = contentLeft + labelWidth + 8;
        int fieldWidth = Math.max(120, contentWidth - labelWidth - 8);
        StringWidget labelWidget = new StringWidget(contentLeft, y + 5, labelWidth, 18, label, this.font);
        labelWidget.active = false;
        addRenderableWidget(labelWidget);
        EditBox box = new EditBox(this.font, fieldX, y + 2, fieldWidth, 18, label);
        box.setValue(initialValue);
        box.setResponder(saver);
        addRenderableWidget(box);
        return y + 22;
    }

    private Button makeToggle(Component label, int x, int y, int width, boolean initialValue, Consumer<Boolean> setter) {
        final boolean[] value = {initialValue};
        setter.accept(initialValue);
        return Button.builder(toggleLabel(label, value[0]), button -> {
                    value[0] = !value[0];
                    setter.accept(value[0]);
                    button.setMessage(toggleLabel(label, value[0]));
                })
                .bounds(x, y, width, 20)
                .build();
    }

    private void switchPage(Page targetPage) {
        if (this.page != targetPage) {
            this.page = targetPage;
            rebuildPage();
        }
    }

    private void applyAndClose() {
        LampDeviceConfig old = target.get();
        LampDeviceConfig.FogSettings oldFog = old.fog();
        target.apply(new LampDeviceConfig(
                parseDouble(lightRangeText, old.lightRange()),
                parseDouble(lightAngleText, old.lightAngle()),
                parseList(lightColorsText, old.lightColorsARGB(), false),
                realLight,
                parseDouble(lightIntensityText, old.lightIntensity()),
                lightOcclusion,
                coneRaycast,
                parseDouble(realLightLuminanceText, old.realLightLuminance()),
                parseList(layerSizeScalesText, old.layerSizeScales(), false),
                parseList(layerCenterAlphasText, old.layerCenterAlphas(), false),
                parseList(layerEdgeAlphasText, old.layerEdgeAlphas(), false),
                parseList(layerColorsText, old.layerColorsARGB(), true),
                parseDouble(colorNoiseAmplitudeText, old.colorNoiseAmplitude()),
                new LampDeviceConfig.FogSettings(
                        fogEnabled,
                        parseDouble(fogSizeScaleText, oldFog.sizeScale()),
                        parseDouble(fogCenterAlphaText, oldFog.centerAlpha()),
                        parseDouble(fogEdgeAlphaText, oldFog.edgeAlpha()),
                        nonBlankOr(fogColorText, oldFog.colorARGB())
                )
        ));
        onClose();
    }

    private void loadFromCurrentConfig() {
        LampDeviceConfig config = target.get();
        LampDeviceConfig.FogSettings fog = config.fog();
        this.realLight = config.realLight();
        this.lightOcclusion = config.lightOcclusion();
        this.coneRaycast = config.coneRaycast();
        this.fogEnabled = fog.enabled();
        this.lightRangeText = doubleText(config.lightRange());
        this.lightAngleText = doubleText(config.lightAngle());
        this.lightColorsText = join(config.lightColorsARGB());
        this.lightIntensityText = doubleText(config.lightIntensity());
        this.realLightLuminanceText = doubleText(config.realLightLuminance());
        this.layerSizeScalesText = join(config.layerSizeScales());
        this.layerCenterAlphasText = join(config.layerCenterAlphas());
        this.layerEdgeAlphasText = join(config.layerEdgeAlphas());
        this.layerColorsText = join(config.layerColorsARGB());
        this.colorNoiseAmplitudeText = doubleText(config.colorNoiseAmplitude());
        this.fogSizeScaleText = doubleText(fog.sizeScale());
        this.fogCenterAlphaText = doubleText(fog.centerAlpha());
        this.fogEdgeAlphaText = doubleText(fog.edgeAlpha());
        this.fogColorText = fog.colorARGB();
    }

    private static Component toggleLabel(Component label, boolean enabled) {
        return Component.empty().append(label).append(": ").append(enabled
                ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                : Component.translatable("options.off").withStyle(ChatFormatting.RED));
    }

    private static List<String> parseList(String raw, List<String> fallback, boolean allowEmpty) {
        if (raw == null || raw.isBlank()) {
            return allowEmpty ? List.of() : fallback;
        }
        List<String> values = new ArrayList<>();
        for (String token : raw.split(",")) {
            String value = token.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values.isEmpty() ? (allowEmpty ? List.of() : fallback) : values;
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static String nonBlankOr(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static String join(List<String> values) {
        return String.join(", ", values);
    }

    private static String doubleText(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    protected final int panelWidth() {
        return Math.min(640, this.width - 24);
    }

    protected final int panelHeight() {
        return Math.min(330, this.height - 16);
    }

    protected final int panelLeft() {
        return (this.width - panelWidth()) / 2;
    }

    protected final int panelTop() {
        return (this.height - panelHeight()) / 2;
    }
}
