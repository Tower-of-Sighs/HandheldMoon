package cc.sighs.handheldmoon.spi;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.api.config.ConfigTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface ClientPlatformService {
    void initializeClient();

    void tickClient();

    void registerKeyBindings();

    int flashlightKeyCode();

    int deviceConfigKeyCode();

    boolean isFlashlightKeyDown();

    void sendLampState(BlockPos pos, float xRot, float yRot, boolean powered);

    void sendLampConfig(BlockPos pos, LampDeviceConfig config);

    void sendFullMoonConfig(BlockPos pos, FullMoonDeviceConfig config);

    void sendHeldLampConfig(InteractionHand hand, LampDeviceConfig config);

    void sendHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config);

    default boolean isAccessoryPlatformAvailable() {
        return false;
    }

    void openLampConfig(ConfigTarget<LampDeviceConfig> target);

    void openFullMoonConfig(ConfigTarget<FullMoonDeviceConfig> target);
}
