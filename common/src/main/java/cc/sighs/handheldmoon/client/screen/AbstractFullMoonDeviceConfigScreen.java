package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;

public abstract class AbstractFullMoonDeviceConfigScreen extends Screen {
    private final ConfigTarget<FullMoonDeviceConfig> target;
    private final Screen parent;
    private EditBox luminanceEdit;
    private EditBox radiusEdit;
    private EditBox attenuationEdit;
    private boolean realLight;
    private boolean lightOcclusion;

    protected AbstractFullMoonDeviceConfigScreen(ConfigTarget<FullMoonDeviceConfig> target) {
        this(target, null);
    }

    protected AbstractFullMoonDeviceConfigScreen(ConfigTarget<FullMoonDeviceConfig> target, Screen parent) {
        super(Component.translatable("screen.handheldmoon.device_config.full_moon"));
        this.target = target;
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        FullMoonDeviceConfig config = target.get();
        this.realLight = config.realLight();
        this.lightOcclusion = config.lightOcclusion();

        int panelWidth = Math.min(420, this.width - 24);
        int panelHeight = Math.min(290, this.height - 16);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int contentLeft = left + 16;
        int controlWidth = panelWidth - 32;

        addRenderableWidget(makeToggle(
                Component.translatable("config.handheldmoon.client_settings.enableRealLight"),
                contentLeft,
                top + 56,
                controlWidth,
                this.realLight,
                value -> this.realLight = value
        ));
        addRenderableWidget(makeToggle(
                Component.translatable("config.handheldmoon.client_settings.enableLightOcclusion"),
                contentLeft,
                top + 86,
                controlWidth,
                this.lightOcclusion,
                value -> this.lightOcclusion = value
        ));

        StringWidget label = new StringWidget(
                contentLeft,
                top + 110,
                controlWidth,
                18,
                Component.translatable("config.handheldmoon.client_settings.realLightLuminance"),
                this.font
        );
        label.active = false;
        addRenderableWidget(label);

        this.luminanceEdit = new EditBox(
                this.font,
                contentLeft,
                top + 130,
                controlWidth,
                20,
                Component.translatable("config.handheldmoon.client_settings.realLightLuminance")
        );
        this.luminanceEdit.setValue(doubleText(config.realLightLuminance()));
        addRenderableWidget(this.luminanceEdit);

        StringWidget radiusLabel = new StringWidget(
                contentLeft, top + 154, controlWidth, 18,
                Component.translatable("config.handheldmoon.client_settings.realLightRadius"), this.font);
        radiusLabel.active = false;
        addRenderableWidget(radiusLabel);
        this.radiusEdit = new EditBox(this.font, contentLeft, top + 174, controlWidth, 20,
                Component.translatable("config.handheldmoon.client_settings.realLightRadius"));
        this.radiusEdit.setValue(doubleText(config.realLightRadius()));
        addRenderableWidget(this.radiusEdit);

        StringWidget attenuationLabel = new StringWidget(
                contentLeft, top + 198, controlWidth, 18,
                Component.translatable("config.handheldmoon.client_settings.realLightAttenuation"), this.font);
        attenuationLabel.active = false;
        addRenderableWidget(attenuationLabel);
        this.attenuationEdit = new EditBox(this.font, contentLeft, top + 218, controlWidth, 20,
                Component.translatable("config.handheldmoon.client_settings.realLightAttenuation"));
        this.attenuationEdit.setValue(config.realLightAttenuation().name());
        addRenderableWidget(this.attenuationEdit);

        int buttonY = top + panelHeight - 30;
        int half = (controlWidth - 8) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> applyAndClose())
                .bounds(contentLeft, buttonY, half, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(contentLeft + half + 8, buttonY, half, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void applyAndClose() {
        FullMoonDeviceConfig old = target.get();
        target.apply(new FullMoonDeviceConfig(
                this.realLight,
                parseDouble(this.luminanceEdit.getValue(), old.realLightLuminance()),
                parseDouble(this.radiusEdit.getValue(), old.realLightRadius()),
                AttenuationCurve.parse(this.attenuationEdit.getValue()),
                this.lightOcclusion
        ));
        onClose();
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

    private static Component toggleLabel(Component label, boolean enabled) {
        return Component.empty()
                .append(label)
                .append(": ")
                .append(enabled
                        ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("options.off").withStyle(ChatFormatting.RED));
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static String doubleText(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    protected final int panelWidth() {
        return Math.min(420, this.width - 24);
    }

    protected final int panelHeight() {
        return Math.min(290, this.height - 16);
    }

    protected final int panelLeft() {
        return (this.width - panelWidth()) / 2;
    }

    protected final int panelTop() {
        return (this.height - panelHeight()) / 2;
    }
}
