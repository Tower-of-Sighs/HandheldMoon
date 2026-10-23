package cc.sighs.handheldmoon.fabric.compat.tacz;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Direct TACZ API usage; only loaded when TACZ is installed. */
public final class TaczCompatInner {
    private static final ResourceLocation MOONLIGHT_LASER =
            new ResourceLocation("handheldmoon", "handheldmoon_laser");
    private static final ResourceLocation MOONLIGHT_MUZZLE =
            new ResourceLocation("handheldmoon", "handheldmoon_muzzle");

    private TaczCompatInner() {
    }

    public static boolean isUsingAttachmentFlashlight(Player player) {
        return isLampAttachment(player.getMainHandItem()) || isLampAttachment(player.getOffhandItem());
    }

    /** Toggles every moonlight attachment the player holds; returns whether anything changed. */
    public static boolean toggleAttachmentFlashlight(Player player) {
        boolean changed = toggleAttachment(player.getMainHandItem());
        changed |= toggleAttachment(player.getOffhandItem());
        return changed;
    }

    private static boolean toggleAttachment(ItemStack gunStack) {
        if (!hasMoonlightAttachment(gunStack)) {
            return false;
        }
        TaczCompat.setPowered(gunStack, !TaczCompat.isPowered(gunStack));
        return true;
    }

    public static boolean hasMoonlightAttachment(ItemStack gunStack) {
        if (gunStack.isEmpty()) {
            return false;
        }
        IGun gun = IGun.getIGunOrNull(gunStack);
        return gun != null && hasMoonlightAttachment(gunStack, gun);
    }

    static boolean hasMoonlightAttachment(ItemStack gunStack, IGun gun) {
        ResourceLocation laser = gun.getAttachmentId(gunStack, AttachmentType.LASER);
        ResourceLocation muzzle = gun.getAttachmentId(gunStack, AttachmentType.MUZZLE);
        return MOONLIGHT_LASER.equals(laser) || MOONLIGHT_MUZZLE.equals(muzzle);
    }

    public static boolean isLampAttachment(ItemStack stack) {
        if (stack.isEmpty() || !hasMoonlightAttachment(stack)) {
            return false;
        }
        return TaczCompat.isPowered(stack);
    }
}
