package com.sighs.handheldmoon.compat.tacz;

import com.sighs.handheldmoon.network.ServerToggleAttachmentLampPacket;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaczCompatInner {

    public static boolean isUsingAttachmentFlashlight(Player player) {
        return isLampAttachment(player.getMainHandItem());
    }

    public static void toggleAttachmentFlashlight(Player player) {
        var mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        var iGun = IGun.getIGunOrNull(mainHand);
        if (iGun == null) return;

        if (!hasMoonlightAttachment(mainHand, iGun)) return;

        boolean currentlyOn = mainHand.getOrDefault(TaczCompat.POWERED__TACZ, false);
        mainHand.set(TaczCompat.POWERED__TACZ, !currentlyOn);
        if (player.level().isClientSide) {
            ClientPlayNetworking.send(new ServerToggleAttachmentLampPacket());
        }
    }


    public static boolean hasMoonlightAttachment(ItemStack gunStack, IGun iGun) {
        var laser = iGun.getAttachmentId(gunStack, AttachmentType.LASER);
        var muzzle = iGun.getAttachmentId(gunStack, AttachmentType.MUZZLE);

        return laser.equals(ResourceLocation.parse("handheldmoon:handheldmoon_laser")) ||
                muzzle.equals(ResourceLocation.parse("handheldmoon:handheldmoon_muzzle"));
    }

    public static boolean isLampAttachment(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        var iGun = IGun.getIGunOrNull(itemStack);
        if (iGun == null) return false;

        if (!hasMoonlightAttachment(itemStack, iGun)) return false;

        return itemStack.getOrDefault(TaczCompat.POWERED__TACZ, false);
    }
}
