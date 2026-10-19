package cc.sighs.handheldmoon.spi;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

public interface ClientPlatformService {
    void initializeClient();

    void tickClient();

    void registerKeyBindings();

    int flashlightKeyCode();

    int coneSwitchKeyCode();

    /** Key code that opens the mod-wide settings screen. */
    int modConfigKeyCode();

    boolean isFlashlightKeyDown();

    void sendLampState(BlockPos pos, float xRot, float yRot, boolean powered);

    void sendLampConfig(BlockPos pos, LampDeviceConfig config);

    void sendFullMoonConfig(BlockPos pos, FullMoonDeviceConfig config);

    void sendHeldLampConfig(InteractionHand hand, LampDeviceConfig config);

    void sendHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config);

    default boolean isAccessoryPlatformAvailable() {
        return false;
    }

    /** Asks the server to open the flashlight page for the held lamp item. */
    void requestOpenFlashlightConfig();

    /** Asks the server to open the same flashlight page for a placed lamp block. */
    void requestOpenLampConfig(BlockPos pos);

    /** Opens the mod-wide settings screen. */
    void openModConfig();
}
