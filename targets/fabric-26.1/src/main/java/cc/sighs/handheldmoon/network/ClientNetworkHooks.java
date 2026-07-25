package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.api.network.ClientDeviceConfigSync;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ClientNetworkHooks {
    private static final ClientDeviceConfigSync<BlockPos, InteractionHand, MoonlightLampBlockEntity> SYNC =
            new ClientDeviceConfigSync<>();

    private ClientNetworkHooks() {
    }

    public static void installLampState(Consumer<MoonlightLampBlockEntity> sender) {
        SYNC.installLampState(sender);
    }

    public static void installDeviceConfigSenders(
            BiConsumer<BlockPos, LampDeviceConfig> lampBlock,
            BiConsumer<BlockPos, FullMoonDeviceConfig> fullMoonBlock,
            BiConsumer<InteractionHand, LampDeviceConfig> heldLamp,
            BiConsumer<InteractionHand, FullMoonDeviceConfig> heldFullMoon
    ) {
        SYNC.installDeviceConfigSenders(lampBlock, fullMoonBlock, heldLamp, heldFullMoon);
    }

    public static void syncLampState(MoonlightLampBlockEntity lamp) {
        SYNC.syncLampState(lamp);
    }

    public static void syncLampBlockConfig(BlockPos pos, LampDeviceConfig config) {
        SYNC.syncLampBlockConfig(pos, config);
    }

    public static void syncFullMoonBlockConfig(BlockPos pos, FullMoonDeviceConfig config) {
        SYNC.syncFullMoonBlockConfig(pos, config);
    }

    public static void syncHeldLampConfig(InteractionHand hand, LampDeviceConfig config) {
        SYNC.syncHeldLampConfig(hand, config);
    }

    public static void syncHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config) {
        SYNC.syncHeldFullMoonConfig(hand, config);
    }
}

