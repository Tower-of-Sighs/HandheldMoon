package cc.sighs.handheldmoon.api.network;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientDeviceConfigSyncTest {
    @Test
    void independentlyInstalledSendersRemainActive() {
        ClientDeviceConfigSync<String, String, String> sync = new ClientDeviceConfigSync<>();
        AtomicReference<String> lampState = new AtomicReference<>();
        AtomicReference<String> heldFullMoon = new AtomicReference<>();
        FullMoonDeviceConfig fullMoonConfig = new FullMoonDeviceConfig(true, 12.0, false);

        sync.installLampState(lampState::set);
        sync.installDeviceConfigSenders(
                (position, config) -> { },
                (position, config) -> { },
                (hand, config) -> { },
                (hand, config) -> heldFullMoon.set(hand + ':' + config.realLightLuminance())
        );

        sync.syncLampState("lamp");
        sync.syncHeldFullMoonConfig("offhand", fullMoonConfig);

        assertEquals("lamp", lampState.get());
        assertEquals("offhand:12.0", heldFullMoon.get());
    }
}
