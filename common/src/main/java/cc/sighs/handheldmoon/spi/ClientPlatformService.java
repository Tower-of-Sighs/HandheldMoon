package cc.sighs.handheldmoon.spi;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

import java.util.UUID;

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

    /** Opens the light-source profile editor selected by the moonlight wand. */
    void openLightSourceConfig(
            FullMoonDynamicLightSource source, int entityId, UUID uuid, String sourceId);

    /** Sends an edited light profile to the server. */
    void sendLightProfile(int entityId, EntityLightProfile profile);
}
