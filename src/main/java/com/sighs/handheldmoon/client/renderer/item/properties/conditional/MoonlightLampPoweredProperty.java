package com.sighs.handheldmoon.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record MoonlightLampPoweredProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<MoonlightLampPoweredProperty> MAP_CODEC = MapCodec.unit(new MoonlightLampPoweredProperty());

    @Override
    public boolean get(
            ItemStack itemStack,
            @Nullable ClientLevel clientLevel,
            @Nullable LivingEntity livingEntity,
            int seed,
            ItemDisplayContext ctx
    ) {
        return MoonlightLampItem.isPowered(itemStack);
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }
}