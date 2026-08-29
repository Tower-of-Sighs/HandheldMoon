package cc.sighs.handheldmoon.api.content;

import cc.sighs.handheldmoon.config.LampDeviceConfig;

import java.util.UUID;
import net.minecraft.core.BlockPos;

/** Common contract for the version-specific lamp block entity. */
public interface MoonlightLampBlockEntityAccess {
    void clientTick();

    void serverTick();

    BlockPos getBlockPos();
    float getXRot();

    void setXRot(float xRot);

    float getYRot();

    void setYRot(float yRot);

    boolean getPowered();

    void setPowered(boolean powered);

    LampDeviceConfig getLampConfig();

    boolean hasCustomLampConfig();

    void setLampConfig(LampDeviceConfig config, boolean customized);

    UUID getUuid();

    void setUuid(UUID uuid);
}
