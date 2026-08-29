package cc.sighs.handheldmoon.api.content;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;

import java.util.UUID;
import net.minecraft.core.BlockPos;

/** Common contract for the version-specific full-moon block entity. */
public interface FullMoonBlockEntityAccess {
    void clientTick();

    void serverTick();

    BlockPos getBlockPos();
    UUID getUuid();

    void setUuid(UUID uuid);

    FullMoonDeviceConfig getFullMoonConfig();

    boolean hasCustomFullMoonConfig();

    void setFullMoonConfig(FullMoonDeviceConfig config, boolean customized);
}
