package cc.sighs.handheldmoon.api.network;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;

public interface ClientDeviceConfigSyncPort<P, H, L> {
    void syncLampState(L lamp);

    void syncLampBlockConfig(P position, LampDeviceConfig config);

    void syncFullMoonBlockConfig(P position, FullMoonDeviceConfig config);

    void syncHeldLampConfig(H hand, LampDeviceConfig config);

    void syncHeldFullMoonConfig(H hand, FullMoonDeviceConfig config);
}
