package com.sighs.handheldmoon.compat.tacz;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.compat.CompatLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Range;

public class TaczLambDynLightsCompat implements CompatLayer {
    @Override
    public @Range(from = 0L, to = 15L) int getLivingEntityLuminanceFromItems(ItemLightSourceManager itemLightSourceManager, LivingEntity livingEntity, boolean b) {
        if (!(livingEntity instanceof Player player)) return 0;
        var mainHand = player.getMainHandItem();
        boolean hasAttachment = TaczCompat.hasMoonlightAttachment(mainHand);
        boolean using = TaczCompat.isUsingAttachmentFlashlight(player);
        if (hasAttachment && !using) {
            return 15;
        }
        return 0;
    }
}
