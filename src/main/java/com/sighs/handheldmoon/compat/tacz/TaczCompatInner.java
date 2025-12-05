package com.sighs.handheldmoon.compat.tacz;

import com.sighs.handheldmoon.network.ServerToggleAttachmentLampPacket;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaczCompatInner {

    public static final String TAG_POWERED_LAMP = "poweredMoonlightLamp";
    public static boolean isUsingAttachmentFlashlight(Player player) {
        return isLampAttachment(player.getMainHandItem());
    }

    public static void toggleAttachmentFlashlight(Player player) {
        var mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        var iGun = IGun.getIGunOrNull(mainHand);
        if (iGun == null) return;

        if (!hasMoonlightAttachment(mainHand, iGun)) return;

        var tag = mainHand.getOrCreateTag();
        boolean currentlyOn = tag.getBoolean(TAG_POWERED_LAMP);
        tag.putBoolean(TAG_POWERED_LAMP, !currentlyOn);
        if(player.level().isClientSide) {
            ClientPlayNetworking.send(new ServerToggleAttachmentLampPacket());
        }
    }


    public static boolean hasMoonlightAttachment(ItemStack gunStack, IGun iGun) {
        var laser = iGun.getAttachmentId(gunStack, AttachmentType.LASER);
        var muzzle = iGun.getAttachmentId(gunStack, AttachmentType.MUZZLE);

        return laser.equals(new ResourceLocation("handheldmoon:handheldmoon_laser")) ||
                muzzle.equals(new ResourceLocation("handheldmoon:handheldmoon_muzzle"));
    }

    public static boolean isLampAttachment(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        var iGun = IGun.getIGunOrNull(itemStack);
        if (iGun == null) return false;

        if (!hasMoonlightAttachment(itemStack, iGun)) return false;

        var tag = itemStack.getOrCreateTag();
        return tag.getBoolean(TAG_POWERED_LAMP);
    }
}
