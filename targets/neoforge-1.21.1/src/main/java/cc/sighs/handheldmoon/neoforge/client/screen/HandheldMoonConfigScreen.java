package cc.sighs.handheldmoon.neoforge.client.screen;

import cc.sighs.handheldmoon.client.screen.AbstractHandheldMoonConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HandheldMoonConfigScreen extends AbstractHandheldMoonConfigScreen {
    public HandheldMoonConfigScreen() {
        super();
    }

    public HandheldMoonConfigScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Screen#render draws the blurred menu background before the widgets, so the
        // panel and its text must be drawn after it; drawing them from render() would
        // put the text underneath the blur and make it look fuzzy.
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = panelLeft();
        int top = panelTop();
        graphics.fill(left, top, left + panelWidth(), top + panelHeight(), 0xD0161A22);
        graphics.fill(left, top, left + panelWidth(), top + 2, 0xFF4AC0FF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.handheldmoon.mod_config.title"),
                this.width / 2, top + 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.handheldmoon.mod_config.subtitle"),
                this.width / 2, top + 28, 0xA0D8FF);
    }
}
