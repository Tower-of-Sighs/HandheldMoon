package cc.sighs.handheldmoon.forge.compat.tacz;

import cc.sighs.handheldmoon.item.ItemData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.function.Consumer;

/** Timeless and Classics Zero (TACZ) compatibility. Gun attachment data lives in item NBT. */
public final class TaczCompat {
    private static final String MOD_ID = "tacz";
    private static boolean installed;
    /** Bumped locally so the client can send a state sync to the server. */
    private static Consumer<Player> stateSync = player -> {
    };

    private TaczCompat() {
    }

    public static void init() {
        installed = ModList.get().isLoaded(MOD_ID);
    }

    public static void setStateSync(Consumer<Player> sync) {
        stateSync = sync;
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static boolean isUsingAttachmentFlashlight(Player player) {
        return installed && TaczCompatInner.isUsingAttachmentFlashlight(player);
    }

    public static void toggleAttachmentFlashlight(Player player) {
        if (!installed) {
            return;
        }
        boolean changed = TaczCompatInner.toggleAttachmentFlashlight(player);
        if (changed && player.level().isClientSide) {
            stateSync.accept(player);
        }
    }

    public static boolean hasMoonlightAttachment(ItemStack stack) {
        return installed && TaczCompatInner.hasMoonlightAttachment(stack);
    }

    public static boolean isLampAttachment(ItemStack stack) {
        return installed && TaczCompatInner.isLampAttachment(stack);
    }

    public static boolean isPowered(ItemStack stack) {
        return ItemData.powered(stack) == 1;
    }

    public static void setPowered(ItemStack stack, boolean powered) {
        ItemData.setPowered(stack, powered ? 1 : 0);
    }
}
