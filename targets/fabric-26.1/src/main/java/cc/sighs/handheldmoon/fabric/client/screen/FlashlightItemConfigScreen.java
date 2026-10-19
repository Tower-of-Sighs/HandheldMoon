package cc.sighs.handheldmoon.fabric.client.screen;

import cc.sighs.handheldmoon.client.screen.AbstractFlashlightItemConfigScreen;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FlashlightItemConfigScreen extends AbstractFlashlightItemConfigScreen {
    public FlashlightItemConfigScreen(FlashlightConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
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
