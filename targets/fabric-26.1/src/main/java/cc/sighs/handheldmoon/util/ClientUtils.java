package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.handheldmoon.network.ClientNetworkHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public final class ClientUtils {

    public static MoonlightLampBlockEntity getPoweredMoonlightLampBlock(FullMoonEntity entity) {
        if (entity.level().getBlockEntity(entity.blockPosition()) instanceof MoonlightLampBlockEntity lamp) {
            if (lamp.getPowered()) {
                return lamp;
            }
        }
        return null;
    }

    public static @Nullable MoonlightLampBlockEntity getCursorMoonlightLampBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockentity = mc.level.getBlockEntity(result.getBlockPos());
            if (blockentity instanceof MoonlightLampBlockEntity lamp) {
                return lamp;
            }
        }
        return null;
    }

    public static @Nullable FullMoonBlockEntity getCursorFullMoonBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockentity = mc.level.getBlockEntity(result.getBlockPos());
            if (blockentity instanceof FullMoonBlockEntity moon) {
                return moon;
            }
        }
        return null;
    }

    public static void syncMoonlightLampBlock(MoonlightLampBlockEntity lamp) {
        ClientNetworkHooks.syncLampState(lamp);
    }

    public static void syncMoonlightLampConfig(BlockPos pos, LampDeviceConfig config) {
        ClientNetworkHooks.syncLampBlockConfig(pos, config);
    }

    public static void syncFullMoonConfig(BlockPos pos, FullMoonDeviceConfig config) {
        ClientNetworkHooks.syncFullMoonBlockConfig(pos, config);
    }

    public static void syncHeldMoonlightLampConfig(InteractionHand hand, LampDeviceConfig config) {
        ClientNetworkHooks.syncHeldLampConfig(hand, config);
    }

    public static void syncHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config) {
        ClientNetworkHooks.syncHeldFullMoonConfig(hand, config);
    }
}
