package cc.sighs.handheldmoon.compat.tacz;

import cc.sighs.handheldmoon.network.ServerToggleAttachmentLampPacket;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class TaczCompatInner {
    private static final ResourceLocation MOONLIGHT_LASER = ResourceLocation.fromNamespaceAndPath("handheldmoon", "handheldmoon_laser");
    private static final ResourceLocation MOONLIGHT_MUZZLE = ResourceLocation.fromNamespaceAndPath("handheldmoon", "handheldmoon_muzzle");

    public static boolean isUsingAttachmentFlashlight(Player player) {
        return isLampAttachment(player.getMainHandItem()) || isLampAttachment(player.getOffhandItem());
    }

    public static void toggleAttachmentFlashlight(Player player) {
        boolean changed = toggleAttachment(player.getMainHandItem())
                | toggleAttachment(player.getOffhandItem());
        if (changed && player.level().isClientSide) {
            PacketDistributor.sendToServer(new ServerToggleAttachmentLampPacket());
        }
    }

    private static boolean toggleAttachment(ItemStack gunStack) {
        if (!hasMoonlightAttachment(gunStack)) return false;
        boolean currentlyOn = gunStack.getOrDefault(TaczCompat.POWERED__TACZ, false);
        gunStack.set(TaczCompat.POWERED__TACZ, !currentlyOn);
        return true;
    }

    public static boolean hasMoonlightAttachment(ItemStack gunStack) {
        if (gunStack.isEmpty()) return false;
        var iGun = IGun.getIGunOrNull(gunStack);
        return iGun != null && hasMoonlightAttachment(gunStack, iGun);
    }

    public static boolean hasMoonlightAttachment(ItemStack gunStack, IGun iGun) {
        var laser = iGun.getAttachmentId(gunStack, AttachmentType.LASER);
        var muzzle = iGun.getAttachmentId(gunStack, AttachmentType.MUZZLE);

        return MOONLIGHT_LASER.equals(laser) || MOONLIGHT_MUZZLE.equals(muzzle);
    }

    public static boolean isLampAttachment(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        var iGun = IGun.getIGunOrNull(itemStack);
        if (iGun == null) return false;

        if (!hasMoonlightAttachment(itemStack, iGun)) return false;

        return itemStack.getOrDefault(TaczCompat.POWERED__TACZ, false);
    }
}
