package cc.sighs.handheldmoon.neoforge.client.screen;

import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import cc.sighs.handheldmoon.client.screen.AbstractFullMoonDeviceConfigScreen;

public final class FullMoonDeviceConfigScreen extends AbstractFullMoonDeviceConfigScreen {
    public FullMoonDeviceConfigScreen(ConfigTarget<FullMoonDeviceConfig> target) {
        super(target);
    }

    public FullMoonDeviceConfigScreen(ConfigTarget<FullMoonDeviceConfig> target, Screen parent) {
        super(target, parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = panelLeft();
        int top = panelTop();
        graphics.fill(0, 0, this.width, this.height, 0xB0101318);
        graphics.fill(left, top, left + panelWidth(), top + panelHeight(), 0xF0181C24);
        graphics.fill(left, top, left + panelWidth(), top + 2, 0xFF4AC0FF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.handheldmoon.device_config.title"), this.width / 2, top + 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.handheldmoon.device_config.full_moon").withStyle(ChatFormatting.AQUA),
                this.width / 2, top + 28, 0xA0D8FF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
