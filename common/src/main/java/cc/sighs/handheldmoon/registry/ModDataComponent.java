package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import cc.sighs.handheldmoon.spi.RegistrySupplier;

public final class ModDataComponent {
    public static final RegistrySupplier<DataComponentType<Integer>> POWERED =
            HandheldMoon.registry().registerDataComponent("powered", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final RegistrySupplier<DataComponentType<LampDeviceConfig>> LAMP_CONFIG =
            HandheldMoon.registry().registerDataComponent("lamp_config", () -> DataComponentType.<LampDeviceConfig>builder()
                    .persistent(DeviceConfigCodecs.LAMP).networkSynchronized(ByteBufCodecs.fromCodec(DeviceConfigCodecs.LAMP)).build());
    public static final RegistrySupplier<DataComponentType<FullMoonDeviceConfig>> FULL_MOON_CONFIG =
            HandheldMoon.registry().registerDataComponent("full_moon_config", () -> DataComponentType.<FullMoonDeviceConfig>builder()
                    .persistent(DeviceConfigCodecs.FULL_MOON).networkSynchronized(ByteBufCodecs.fromCodec(DeviceConfigCodecs.FULL_MOON)).build());

    private ModDataComponent() {
    }
}
