package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FullMoonDeviceConfigScreen extends AbstractFullMoonDeviceConfigScreen {
    public FullMoonDeviceConfigScreen(ConfigTarget<FullMoonDeviceConfig> target) {
        super(target);
    }

    public FullMoonDeviceConfigScreen(ConfigTarget<FullMoonDeviceConfig> target, Screen parent) {
        super(target, parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = panelLeft();
        int top = panelTop();
        graphics.fill(left, top, left + panelWidth(), top + panelHeight(), 0xD0181C24);
        graphics.fill(left, top, left + panelWidth(), top + 2, 0xFF4AC0FF);
        graphics.centeredText(this.font, Component.translatable("screen.handheldmoon.device_config.title"), this.width / 2, top + 12, 0xFFFFFF);
        graphics.centeredText(this.font,
                Component.translatable("screen.handheldmoon.device_config.full_moon").withStyle(ChatFormatting.AQUA),
                this.width / 2, top + 28, 0xA0D8FF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
