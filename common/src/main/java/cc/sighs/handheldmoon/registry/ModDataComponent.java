package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.oelib.registry.DeferredRegister;
import cc.sighs.oelib.registry.RegisterSupplier;
import com.mojang.serialization.Codec;
import cc.sighs.handheldmoon.HandheldMoon;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModDataComponent {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HandheldMoon.MOD_ID);

    public static final RegisterSupplier<DataComponentType<Integer>> POWERED =
            DATA_COMPONENT_TYPES.register("powered", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build()
            );

    public static final RegisterSupplier<DataComponentType<LampDeviceConfig>> LAMP_CONFIG =
            DATA_COMPONENT_TYPES.register("lamp_config", () ->
                    DataComponentType.<LampDeviceConfig>builder()
                            .persistent(LampDeviceConfig.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(LampDeviceConfig.CODEC))
                            .build()
            );

    public static final RegisterSupplier<DataComponentType<FullMoonDeviceConfig>> FULL_MOON_CONFIG =
            DATA_COMPONENT_TYPES.register("full_moon_config", () ->
                    DataComponentType.<FullMoonDeviceConfig>builder()
                            .persistent(FullMoonDeviceConfig.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(FullMoonDeviceConfig.CODEC))
                            .build()
            );
}
