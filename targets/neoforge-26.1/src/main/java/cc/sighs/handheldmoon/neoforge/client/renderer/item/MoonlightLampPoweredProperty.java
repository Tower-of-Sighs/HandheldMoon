package cc.sighs.handheldmoon.neoforge.client.renderer.item;

import cc.sighs.handheldmoon.neoforge.item.MoonlightLampItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record MoonlightLampPoweredProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<MoonlightLampPoweredProperty> MAP_CODEC = MapCodec.unit(new MoonlightLampPoweredProperty());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity owner, int seed, ItemDisplayContext ctx) {
        return MoonlightLampItem.getPowered(itemStack) != 0;
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }
}
