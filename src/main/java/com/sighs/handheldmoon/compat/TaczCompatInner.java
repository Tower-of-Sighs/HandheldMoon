package com.sighs.handheldmoon.compat;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaczCompatInner {
    public static boolean isUsingAttachmentFlashlight(Player player) {
        return isLampAttachment(player.getMainHandItem());
    }
    public static void toggleAttachmentFlashlight(Player player) {
        ItemStack itemStack = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(itemStack);
        if (iGun != null) {
            var tag = player.getPersistentData();
            tag.putBoolean("poweredMoonlightLamp", !tag.getBoolean("poweredMoonlightLamp"));
        }
    }
    public static boolean isLampAttachment(ItemStack itemStack) {
        boolean result = false;
        IGun iGun = IGun.getIGunOrNull(itemStack);
        if (iGun != null) {
            var muzzle = iGun.getAttachmentId(itemStack, AttachmentType.MUZZLE);
            var laser = iGun.getAttachmentId(itemStack, AttachmentType.LASER);
            boolean matched = laser.equals(new ResourceLocation("handheldmoon:handheldmoon_laser")) || muzzle.equals(new ResourceLocation("handheldmoon:handheldmoon_muzzle"));
            var tag = itemStack.getOrCreateTag();
            boolean powered = tag.getBoolean("poweredMoonlightLamp");
            result = matched && powered;
        }
        return result;
    }
}
