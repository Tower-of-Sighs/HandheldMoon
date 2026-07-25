package cc.sighs.handheldmoon.api.network;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ClientDeviceConfigSync<P, H, L> {
    private volatile ClientDeviceConfigSyncPort<P, H, L> port = noOpPort();

    public void install(ClientDeviceConfigSyncPort<P, H, L> port) {
        this.port = Objects.requireNonNull(port);
    }

    public void installLampState(Consumer<L> sender) {
        Objects.requireNonNull(sender);
        ClientDeviceConfigSyncPort<P, H, L> current = port;
        install(new ClientDeviceConfigSyncPort<P, H, L>() {
            @Override
            public void syncLampState(L lamp) {
                sender.accept(lamp);
            }

            @Override
            public void syncLampBlockConfig(P position, LampDeviceConfig config) {
                current.syncLampBlockConfig(position, config);
            }

            @Override
            public void syncFullMoonBlockConfig(P position, FullMoonDeviceConfig config) {
                current.syncFullMoonBlockConfig(position, config);
            }

            @Override
            public void syncHeldLampConfig(H hand, LampDeviceConfig config) {
                current.syncHeldLampConfig(hand, config);
            }

            @Override
            public void syncHeldFullMoonConfig(H hand, FullMoonDeviceConfig config) {
                current.syncHeldFullMoonConfig(hand, config);
            }
        });
    }

    public void installDeviceConfigSenders(
            BiConsumer<P, LampDeviceConfig> lampBlock,
            BiConsumer<P, FullMoonDeviceConfig> fullMoonBlock,
            BiConsumer<H, LampDeviceConfig> heldLamp,
            BiConsumer<H, FullMoonDeviceConfig> heldFullMoon
    ) {
        Objects.requireNonNull(lampBlock);
        Objects.requireNonNull(fullMoonBlock);
        Objects.requireNonNull(heldLamp);
        Objects.requireNonNull(heldFullMoon);
        ClientDeviceConfigSyncPort<P, H, L> current = port;
        install(new ClientDeviceConfigSyncPort<P, H, L>() {
            @Override
            public void syncLampState(L lamp) {
                current.syncLampState(lamp);
            }

            @Override
            public void syncLampBlockConfig(P position, LampDeviceConfig config) {
                lampBlock.accept(position, config);
            }

            @Override
            public void syncFullMoonBlockConfig(P position, FullMoonDeviceConfig config) {
                fullMoonBlock.accept(position, config);
            }

            @Override
            public void syncHeldLampConfig(H hand, LampDeviceConfig config) {
                heldLamp.accept(hand, config);
            }

            @Override
            public void syncHeldFullMoonConfig(H hand, FullMoonDeviceConfig config) {
                heldFullMoon.accept(hand, config);
            }
        });
    }

    public void syncLampState(L lamp) {
        port.syncLampState(lamp);
    }

    public void syncLampBlockConfig(P position, LampDeviceConfig config) {
        port.syncLampBlockConfig(position, config);
    }

    public void syncFullMoonBlockConfig(P position, FullMoonDeviceConfig config) {
        port.syncFullMoonBlockConfig(position, config);
    }

    public void syncHeldLampConfig(H hand, LampDeviceConfig config) {
        port.syncHeldLampConfig(hand, config);
    }

    public void syncHeldFullMoonConfig(H hand, FullMoonDeviceConfig config) {
        port.syncHeldFullMoonConfig(hand, config);
    }

    private static <P, H, L> ClientDeviceConfigSyncPort<P, H, L> noOpPort() {
        return new ClientDeviceConfigSyncPort<P, H, L>() {
            @Override
            public void syncLampState(L lamp) {
            }

            @Override
            public void syncLampBlockConfig(P position, LampDeviceConfig config) {
            }

            @Override
            public void syncFullMoonBlockConfig(P position, FullMoonDeviceConfig config) {
            }

            @Override
            public void syncHeldLampConfig(H hand, LampDeviceConfig config) {
            }

            @Override
            public void syncHeldFullMoonConfig(H hand, FullMoonDeviceConfig config) {
            }
        };
    }
}
