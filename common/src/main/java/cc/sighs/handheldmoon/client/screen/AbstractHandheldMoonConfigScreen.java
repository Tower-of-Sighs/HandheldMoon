package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.registry.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Mod-wide settings page. It exposes the three values that still live in the
 * global {@code handheldmoon.properties} file; everything else is configured
 * per device or per flashlight item.
 */
public abstract class AbstractHandheldMoonConfigScreen extends Screen {
    private final @Nullable Screen parent;

    protected AbstractHandheldMoonConfigScreen() {
        this(null);
    }

    protected AbstractHandheldMoonConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.handheldmoon.mod_config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int left = panelLeft();
        int top = panelTop();
        int contentLeft = left + 16;
        int contentWidth = panelWidth - 32;

        int y = top + 52;
        addToggle(
                Component.translatable("config.handheldmoon.client_settings.enableConeRaycast"),
                contentLeft, y, contentWidth, Config.CONE_RAYCAST.get(),
                value -> {
                    Config.CONE_RAYCAST.set(value);
                    Config.CONE_RAYCAST.save();
                });
        y += 26;
        addToggle(
                Component.translatable("config.handheldmoon.client_settings.enableFixedFlashlight"),
                contentLeft, y, contentWidth, Config.ENABLE_FIXED_FLASHLIGHT.get(),
                value -> {
                    Config.ENABLE_FIXED_FLASHLIGHT.set(value);
                    Config.ENABLE_FIXED_FLASHLIGHT.save();
                });
        y += 26;
        addToggle(
                Component.translatable("config.handheldmoon.client_settings.enablePlayerRay"),
                contentLeft, y, contentWidth, Config.PLAYER_RAY.get(),
                value -> {
                    Config.PLAYER_RAY.set(value);
                    Config.PLAYER_RAY.save();
                });

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(contentLeft, top + panelHeight - 30, contentWidth, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void addToggle(
            Component label, int x, int y, int width, boolean initialValue, Consumer<Boolean> setter
    ) {
        final boolean[] value = {initialValue};
        addRenderableWidget(Button.builder(toggleLabel(label, value[0]), button -> {
                    value[0] = !value[0];
                    setter.accept(value[0]);
                    button.setMessage(toggleLabel(label, value[0]));
                })
                .bounds(x, y, width, 20)
                .build());
    }

    private static Component toggleLabel(Component label, boolean enabled) {
        return Component.empty().append(label).append(": ").append(enabled
                ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                : Component.translatable("options.off").withStyle(ChatFormatting.RED));
    }

    protected final int panelWidth() {
        return Math.min(420, this.width - 24);
    }

    protected final int panelHeight() {
        return Math.min(230, this.height - 16);
    }

    protected final int panelLeft() {
        return (this.width - panelWidth()) / 2;
    }

    protected final int panelTop() {
        return (this.height - panelHeight()) / 2;
    }
}
