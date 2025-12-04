package com.sighs.handheldmoon.init;

import com.sighs.handheldmoon.Item.MoonlightLampItem;
import com.sighs.handheldmoon.compat.CuriosCompat;
import com.sighs.handheldmoon.compat.TaczCompat;
import com.sighs.handheldmoon.registry.Items;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class Utils {
    public static boolean takeFlashlight(Player player) {
        boolean result = false;
        if (isFlashlight(player.getMainHandItem())) {
            result = true;
        }
        if (isFlashlight(player.getOffhandItem())) {
            result = true;
        }
        return result || CuriosCompat.hasCuriosFlashlight(player) || TaczCompat.isUsingAttachmentFlashlight(player);
    }
    public static boolean isUsingFlashlight(Player player) {
        boolean result = false;
        if (isFlashlight(player.getMainHandItem())) {
            result = isPoweredFlashlight(player.getMainHandItem());
        }
        if (isFlashlight(player.getOffhandItem())) {
            result = isPoweredFlashlight(player.getOffhandItem());
        }
        return result || CuriosCompat.isUsingCuriosFlashlight(player) || TaczCompat.isUsingAttachmentFlashlight(player);
    }
    public static boolean isFlashlight(ItemStack itemStack) {
        boolean isLampItem = itemStack.is(Items.MOONLIGHT_LAMP.get());
        return itemStack.is(Items.MOONLIGHT_LAMP.get());
    }
    public static boolean isPoweredFlashlight(ItemStack itemStack) {
        return MoonlightLampItem.getPowered(itemStack) == 1;
    }
    public static void toggleFlashlight(Player player) {
        if (isFlashlight(player.getMainHandItem())) {
            MoonlightLampItem.togglePowered(player.getMainHandItem());
        }
        if (isFlashlight(player.getOffhandItem())) {
            MoonlightLampItem.togglePowered(player.getOffhandItem());
        }
        CuriosCompat.toggleCuriosFlashlight(player);
        TaczCompat.toggleAttachmentFlashlight(player);
    }

    public static Vec3 calculateViewVector(float xRot, float yRot) {
        float f = xRot * ((float) Math.PI / 180F);
        float f1 = -yRot * ((float) Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }
}
