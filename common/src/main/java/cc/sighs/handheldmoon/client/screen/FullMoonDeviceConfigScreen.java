package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FullMoonDeviceConfigScreen extends Screen {
    private final DeviceConfigTargets.FullMoonTarget target;
    private final Screen parent;
    private EditBox luminanceEdit;
    private boolean realLight;
    private boolean lightOcclusion;

    public FullMoonDeviceConfigScreen(DeviceConfigTargets.FullMoonTarget target) {
        this(target, null);
    }

    public FullMoonDeviceConfigScreen(DeviceConfigTargets.FullMoonTarget target, Screen parent) {
        super(Component.translatable("screen.handheldmoon.device_config.full_moon"));
        this.target = target;
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        FullMoonDeviceConfig config = getCurrentConfig();
        this.realLight = config.realLight();
        this.lightOcclusion = config.lightOcclusion();

        int panelWidth = Math.min(420, this.width - 24);
        int panelHeight = Math.min(240, this.height - 16);
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

        this.luminanceEdit = new EditBox(
                this.font,
                contentLeft,
                top + 116,
                controlWidth,
                20,
                Component.translatable("config.handheldmoon.client_settings.realLightLuminance")
        );
        this.luminanceEdit.setValue(doubleText(config.realLightLuminance()));
        StringWidget labelWidget = new StringWidget(
                contentLeft,
                top + 104,
                controlWidth,
                18,
                Component.translatable("config.handheldmoon.client_settings.realLightLuminance"),
                this.font
        );
        labelWidget.active = false;
        addRenderableWidget(labelWidget);
        addRenderableWidget(this.luminanceEdit);

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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(420, this.width - 24);
        int panelHeight = Math.min(240, this.height - 16);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int contentLeft = left + 16;

        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xD0181C24);
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFF4AC0FF);

        graphics.centeredText(
                this.font,
                Component.translatable("screen.handheldmoon.device_config.title"),
                this.width / 2,
                top + 12,
                0xFFFFFF
        );
        graphics.centeredText(
                this.font,
                Component.translatable("screen.handheldmoon.device_config.full_moon").withStyle(ChatFormatting.AQUA),
                this.width / 2,
                top + 28,
                0xA0D8FF
        );
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private void applyAndClose() {
        FullMoonDeviceConfig old = target.get();
        FullMoonDeviceConfig updated = new FullMoonDeviceConfig(
                this.realLight,
                parseDouble(this.luminanceEdit.getValue(), old.realLightLuminance()),
                this.lightOcclusion
        );

        target.apply(updated);
        onClose();
    }

    private FullMoonDeviceConfig getCurrentConfig() {
        return target.get();
    }

    private Button makeToggle(Component label, int x, int y, int width, boolean initialValue, java.util.function.Consumer<Boolean> setter) {
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
                .append(enabled ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("options.off").withStyle(ChatFormatting.RED));
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String doubleText(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
