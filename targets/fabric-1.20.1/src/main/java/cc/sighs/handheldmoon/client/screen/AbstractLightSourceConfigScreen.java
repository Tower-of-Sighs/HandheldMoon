package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.spi.PlatformServices;
import cc.sighs.handheldmoon.util.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.function.DoubleConsumer;

/**
 * Light-source profile editor opened by the moonlight wand. Unlike the device
 * pages this edits the entity's synchronized profile directly, so the server
 * does not apply any distance check. The source identifier and UUID are shown
 * read-only; all other values map onto {@link EntityLightProfile} fields.
 * <p>
 * 1.20.1 variant: screens draw their own background, so the panel is rendered
 * from {@link #renderBackground(GuiGraphics)} which {@link #render} calls before
 * the widgets.
 */
public abstract class AbstractLightSourceConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 232;

    private final FullMoonDynamicLightSource source;
    private final int entityId;
    private final @Nullable UUID uuid;
    private final String sourceId;

    private boolean loaded;
    private EntityLightProfile initialProfile;

    private double luminance;
    private double range;
    private double innerAngleDegrees;
    private double outerAngleDegrees;
    private double red;
    private double green;
    private double blue;
    private double alpha;
    private boolean realLight;
    private boolean visibleCone;
    private boolean occlusion;

    protected AbstractLightSourceConfigScreen(
            FullMoonDynamicLightSource source, int entityId, UUID uuid, String sourceId
    ) {
        super(Component.translatable("screen.handheldmoon.light_source_config.title"));
        this.source = source;
        this.entityId = entityId;
        this.uuid = uuid;
        this.sourceId = sourceId == null ? "" : sourceId;
    }

    @Override
    protected void init() {
        clearWidgets();
        if (!loaded) {
            loadProfile(this.source.getLightProfile());
            this.loaded = true;
        }

        int left = panelLeft();
        int top = panelTop();
        int contentLeft = left + 16;
        int contentWidth = panelWidth() - 32;
        int columnGap = 12;
        int columnWidth = (contentWidth - columnGap) / 2;
        int leftColumn = contentLeft;
        int rightColumn = contentLeft + columnWidth + columnGap;

        addSlider(leftColumn, top + 34, columnWidth,
                "config.handheldmoon.light_source.luminance", 0.0, 15.0, luminance,
                "%.1f", value -> this.luminance = value);
        addSlider(leftColumn, top + 56, columnWidth,
                "config.handheldmoon.light_source.range", 0.0, 64.0, range,
                "%.1f", value -> this.range = value);
        addSlider(leftColumn, top + 78, columnWidth,
                "config.handheldmoon.light_source.innerAngle", 0.0, 180.0, innerAngleDegrees,
                "%.0f°", value -> this.innerAngleDegrees = value);
        addSlider(leftColumn, top + 100, columnWidth,
                "config.handheldmoon.light_source.outerAngle", 0.0, 180.0, outerAngleDegrees,
                "%.0f°", value -> this.outerAngleDegrees = value);

        addSlider(rightColumn, top + 34, columnWidth,
                "config.handheldmoon.light_source.red", 0.0, 255.0, red,
                "%.0f", value -> this.red = value);
        addSlider(rightColumn, top + 56, columnWidth,
                "config.handheldmoon.light_source.green", 0.0, 255.0, green,
                "%.0f", value -> this.green = value);
        addSlider(rightColumn, top + 78, columnWidth,
                "config.handheldmoon.light_source.blue", 0.0, 255.0, blue,
                "%.0f", value -> this.blue = value);
        addSlider(rightColumn, top + 100, columnWidth,
                "config.handheldmoon.light_source.alpha", 0.0, 255.0, alpha,
                "%.0f", value -> this.alpha = value);

        int toggleGap = 6;
        int toggleWidth = (contentWidth - toggleGap * 2) / 3;
        addToggle(contentLeft, top + 126, toggleWidth,
                "config.handheldmoon.light_source.realLight", realLight, value -> this.realLight = value);
        addToggle(contentLeft + toggleWidth + toggleGap, top + 126, toggleWidth,
                "config.handheldmoon.light_source.visibleCone", visibleCone, value -> this.visibleCone = value);
        addToggle(contentLeft + (toggleWidth + toggleGap) * 2, top + 126, toggleWidth,
                "config.handheldmoon.light_source.occlusion", occlusion, value -> this.occlusion = value);

        int buttonY = top + 202;
        int buttonWidth = columnWidth / 2 - 4;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> applyAndClose())
                .bounds(rightColumn, buttonY, buttonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(rightColumn + columnWidth / 2 + 4, buttonY, buttonWidth, 20)
                .build());
    }

    private void loadProfile(EntityLightProfile profile) {
        this.initialProfile = profile;
        this.luminance = profile.luminance();
        this.range = profile.range();
        this.innerAngleDegrees = Math.toDegrees(profile.innerAngle());
        this.outerAngleDegrees = Math.toDegrees(profile.outerAngle());
        this.realLight = profile.realLight();
        this.visibleCone = profile.visibleCone();
        this.occlusion = profile.occlusion();
        float[] rgba = ColorUtils.parseColorRGBAWithAlpha(profile.lightColor());
        this.red = rgba[0] * 255.0;
        this.green = rgba[1] * 255.0;
        this.blue = rgba[2] * 255.0;
        this.alpha = rgba[3] * 255.0;
    }

    private void applyAndClose() {
        if (initialProfile != null) {
            String color = String.format(Locale.ROOT, "#%02X%02X%02X%02X",
                    clampByte(red), clampByte(green), clampByte(blue), clampByte(alpha));
            EntityLightProfile updated = new EntityLightProfile(
                    initialProfile.shape(),
                    luminance,
                    range,
                    Math.toRadians(innerAngleDegrees),
                    Math.toRadians(outerAngleDegrees),
                    realLight,
                    visibleCone,
                    occlusion,
                    initialProfile.positionOffset(),
                    initialProfile.attenuationCurve(),
                    color
            );
            PlatformServices.require().client().sendLightProfile(entityId, updated);
        }
        onClose();
    }

    private static int clampByte(double value) {
        return (int) Math.max(0.0, Math.min(255.0, Math.round(value)));
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        // The panel and its text have to be drawn after the menu background,
        // otherwise the title, subtitle and the read-only UUID / source-id lines
        // end up underneath it.
        super.renderBackground(graphics);
        int left = panelLeft();
        int top = panelTop();
        int contentLeft = left + 16;
        graphics.fill(left, top, left + panelWidth(), top + panelHeight(), 0xD0161A22);
        graphics.fill(left, top, left + panelWidth(), top + 2, 0xFF4AC0FF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.handheldmoon.light_source_config.title"),
                this.width / 2, top + 6, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.handheldmoon.light_source_config.subtitle"),
                this.width / 2, top + 20, 0xA0D8FF);

        String uuidText = uuid == null ? "-" : uuid.toString();
        graphics.drawString(this.font, Component.translatable("config.handheldmoon.light_source.uuid")
                        .append(": ").append(Component.literal(uuidText)),
                contentLeft, top + 148, 0xFFE8EEF6, false);
        graphics.drawString(this.font, Component.translatable("config.handheldmoon.light_source.sourceId")
                        .append(": ").append(Component.literal(sourceId.isBlank() ? "-" : sourceId)),
                contentLeft, top + 164, 0xFFE8EEF6, false);
    }

    private void addSlider(
            int x, int y, int width, String labelKey,
            double minimum, double maximum, double initialValue,
            String format, DoubleConsumer setter
    ) {
        addRenderableWidget(new ValueSlider(x, y, width, 20, labelKey,
                minimum, maximum, initialValue, format, setter));
    }

    private void addToggle(
            int x, int y, int width, String labelKey,
            boolean initialValue, java.util.function.Consumer<Boolean> setter
    ) {
        final boolean[] value = {initialValue};
        addRenderableWidget(Button.builder(toggleLabel(labelKey, value[0]), button -> {
                    value[0] = !value[0];
                    setter.accept(value[0]);
                    button.setMessage(toggleLabel(labelKey, value[0]));
                })
                .bounds(x, y, width, 20)
                .build());
    }

    private static Component toggleLabel(String labelKey, boolean enabled) {
        return Component.translatable(labelKey).append(": ").append(enabled
                ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                : Component.translatable("options.off").withStyle(ChatFormatting.RED));
    }

    private int panelWidth() {
        return Math.min(PANEL_WIDTH, this.width - 16);
    }

    private int panelHeight() {
        return Math.min(PANEL_HEIGHT, this.height - 16);
    }

    private int panelLeft() {
        return (this.width - panelWidth()) / 2;
    }

    private int panelTop() {
        return (this.height - panelHeight()) / 2;
    }

    /** Slider mapping its 0..1 position onto an arbitrary numeric range. */
    private static final class ValueSlider extends AbstractSliderButton {
        private final String labelKey;
        private final double minimum;
        private final double maximum;
        private final String format;
        private final DoubleConsumer setter;

        private ValueSlider(
                int x, int y, int width, int height,
                String labelKey, double minimum, double maximum,
                double initialValue, String format, DoubleConsumer setter
        ) {
            super(x, y, width, height, Component.empty(),
                    (initialValue - minimum) / (maximum - minimum));
            this.labelKey = labelKey;
            this.minimum = minimum;
            this.maximum = maximum;
            this.format = format;
            this.setter = setter;
            updateMessage();
        }

        private double actualValue() {
            return minimum + value * (maximum - minimum);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(labelKey)
                    .append(": ")
                    .append(Component.literal(String.format(Locale.ROOT, format, actualValue()))));
        }

        @Override
        protected void applyValue() {
            setter.accept(actualValue());
            updateMessage();
        }
    }
}
