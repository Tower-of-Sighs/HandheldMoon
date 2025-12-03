package com.sighs.handheldmoon.registry;

import com.mojang.serialization.Codec;
import com.sighs.handheldmoon.HandheldMoon;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public class ModDataComponent {
    public static DataComponentType<Integer> POWERED;

    public static void init() {
        POWERED = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "powered"),
                DataComponentType.<Integer>builder()
                        .persistent(Codec.INT)
                        .networkSynchronized(ByteBufCodecs.INT)
                        .build()
        );
    }
}
