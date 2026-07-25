package cc.sighs.handheldmoon.registry;

import com.mojang.serialization.Codec;
import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponent {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HandheldMoon.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> POWERED =
            DATA_COMPONENT_TYPES.register("powered", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LampDeviceConfig>> LAMP_CONFIG =
            DATA_COMPONENT_TYPES.register("lamp_config", () ->
                    DataComponentType.<LampDeviceConfig>builder()
                            .persistent(DeviceConfigCodecs.LAMP)
                            .networkSynchronized(ByteBufCodecs.fromCodec(DeviceConfigCodecs.LAMP))
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FullMoonDeviceConfig>> FULL_MOON_CONFIG =
            DATA_COMPONENT_TYPES.register("full_moon_config", () ->
                    DataComponentType.<FullMoonDeviceConfig>builder()
                            .persistent(DeviceConfigCodecs.FULL_MOON)
                            .networkSynchronized(ByteBufCodecs.fromCodec(DeviceConfigCodecs.FULL_MOON))
                            .build()
            );
}
