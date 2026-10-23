package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;
import java.util.function.DoubleConsumer;

/**
 * Per-item flashlight configuration page, opened by sneaking and right-clicking
 * with a moonlight lamp in hand. The numeric and toggle values are stored in the
 * item's own lamp config, while the lens slot is backed by
 * {@link FlashlightConfigMenu} and persists the installed lens color there too.
 * <p>
 * 1.20.1 variant: the panel size is written to the non-final
 * {@code imageWidth}/{@code imageHeight} fields instead of a size constructor.
 */
public abstract class AbstractFlashlightItemConfigScreen extends AbstractContainerScreen<FlashlightConfigMenu> {
    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 240;
    private static final int CONTENT_X = 20;
    private static final int CONTENT_WIDTH = 160;

    private boolean loaded;
    private boolean flushed;
    private double realLightIntensity;
    private double filterLightIntensity;
    private boolean enableVisibleCone;
    private double lensTintStrength;

    protected AbstractFlashlightItemConfigScreen(
            FlashlightConfigMenu menu, Inventory inventory, Component title
    ) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
        this.inventoryLabelY = 150;
    }

    @Override
    protected void init() {
        clearWidgets();
        super.init();
        if (!loaded) {
            LampDeviceConfig config = currentConfig();
            this.realLightIntensity = config.realLightIntensity();
            this.filterLightIntensity = config.filterLightIntensity();
            this.enableVisibleCone = config.enableVisibleCone();
            this.lensTintStrength = config.lensTintStrength();
            this.loaded = true;
        }

        int left = this.leftPos + CONTENT_X;
        int tintLeft = this.leftPos + FlashlightConfigMenu.LENS_SLOT_X + 24;
        int tintWidth = CONTENT_X + CONTENT_WIDTH - (FlashlightConfigMenu.LENS_SLOT_X + 24);

        addRenderableWidget(new ValueSlider(
                tintLeft, this.topPos + FlashlightConfigMenu.LENS_SLOT_Y, tintWidth, 20,
                "config.handheldmoon.client_settings.lensTintStrength",
                0.0, 1.0, lensTintStrength,
                value -> this.lensTintStrength = value));
        addRenderableWidget(new ValueSlider(
                left, this.topPos + 52, CONTENT_WIDTH, 20,
                "config.handheldmoon.client_settings.realLightIntensity",
                0.0, LampDeviceConfig.DEFAULT_REAL_LIGHT_INTENSITY, realLightIntensity,
                value -> this.realLightIntensity = value));
        addRenderableWidget(new ValueSlider(
                left, this.topPos + 78, CONTENT_WIDTH, 20,
                "config.handheldmoon.client_settings.filterLightIntensity",
                0.0, 1.0, filterLightIntensity,
                value -> this.filterLightIntensity = value));

        final boolean[] visibleCone = {enableVisibleCone};
        addRenderableWidget(Button.builder(toggleLabel(forVisibleCone(), visibleCone[0]), button -> {
                    visibleCone[0] = !visibleCone[0];
                    this.enableVisibleCone = visibleCone[0];
                    button.setMessage(toggleLabel(forVisibleCone(), visibleCone[0]));
                })
                .bounds(left, this.topPos + 104, CONTENT_WIDTH, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, this.topPos + 126, CONTENT_WIDTH, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1.20.1 container screens do not draw the menu background themselves.
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        flushConfig();
        super.onClose();
    }

    @Override
    public void removed() {
        flushConfig();
        super.removed();
    }

    private void flushConfig() {
        if (this.flushed || !this.loaded || this.minecraft == null || this.minecraft.player == null
                || this.minecraft.getConnection() == null) {
            return;
        }
        this.flushed = true;
        LampDeviceConfig config = this.menu.readConfig();
        this.menu.writeConfig(config
                .withRealLightIntensity(this.realLightIntensity)
                .withFilterLightIntensity(this.filterLightIntensity)
                .withEnableVisibleCone(this.enableVisibleCone)
                .withLensTintStrength(this.lensTintStrength), true);
    }

    private LampDeviceConfig currentConfig() {
        return this.menu.readConfig();
    }

    private static Component forVisibleCone() {
        return Component.translatable("config.handheldmoon.client_settings.enableVisibleCone");
    }

    private static Component toggleLabel(Component label, boolean enabled) {
        return Component.empty().append(label).append(": ").append(enabled
                ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                : Component.translatable("options.off").withStyle(ChatFormatting.RED));
    }

    /** Slider that maps its 0..1 position onto a configured numeric range. */
    private static final class ValueSlider extends AbstractSliderButton {
        private final String labelKey;
        private final double minimum;
        private final double maximum;
        private final DoubleConsumer setter;

        private ValueSlider(
                int x, int y, int width, int height,
                String labelKey, double minimum, double maximum,
                double initialValue, DoubleConsumer setter
        ) {
            super(x, y, width, height, Component.empty(),
                    (initialValue - minimum) / (maximum - minimum));
            this.labelKey = labelKey;
            this.minimum = minimum;
            this.maximum = maximum;
            this.setter = setter;
            updateMessage();
        }

        private double actualValue() {
            return minimum + value * (maximum - minimum);
        }

        @Override
        protected void updateMessage() {
            String formatted = maximum <= 1.0
                    ? String.format(Locale.ROOT, "%.2f", actualValue())
                    : String.format(Locale.ROOT, "%.1f", actualValue());
            setMessage(Component.translatable(labelKey).append(": ").append(Component.literal(formatted)));
        }

        @Override
        protected void applyValue() {
            setter.accept(actualValue());
            updateMessage();
        }
    }
}
