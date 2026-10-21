package cc.sighs.handheldmoon.neoforge.client.screen;

import cc.sighs.handheldmoon.client.screen.AbstractFlashlightItemConfigScreen;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FlashlightItemConfigScreen extends AbstractFlashlightItemConfigScreen {
    public FlashlightItemConfigScreen(FlashlightConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xD0161A22);
        graphics.fill(left, top, left + this.imageWidth, top + 2, 0xFF4AC0FF);
        int slotX = left + FlashlightConfigMenu.LENS_SLOT_X - 1;
        int slotY = top + FlashlightConfigMenu.LENS_SLOT_Y - 1;
        graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF0A0D12);
        graphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFF3A4656);
        graphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFF3A4656);
        graphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFF3A4656);
        graphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFF3A4656);
    }
}
