package cc.sighs.handheldmoon.neoforge.client.screen;

import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import cc.sighs.handheldmoon.client.screen.AbstractMoonlightLampDeviceConfigScreen;

public final class MoonlightLampDeviceConfigScreen extends AbstractMoonlightLampDeviceConfigScreen {
    public MoonlightLampDeviceConfigScreen(ConfigTarget<LampDeviceConfig> target) {
        super(target);
    }

    public MoonlightLampDeviceConfigScreen(ConfigTarget<LampDeviceConfig> target, Screen parent) {
        super(target, parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = panelLeft();
        int top = panelTop();
        graphics.fill(0, 0, this.width, this.height, 0xB0101318);
        graphics.fill(left, top, left + panelWidth(), top + panelHeight(), 0xF0161A22);
        graphics.fill(left, top, left + panelWidth(), top + 2, 0xFF4AC0FF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.handheldmoon.device_config.title"), this.width / 2, top + 10, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.handheldmoon.device_config.moonlight_lamp").withStyle(ChatFormatting.AQUA),
                this.width / 2, top + 24, 0xA0D8FF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
