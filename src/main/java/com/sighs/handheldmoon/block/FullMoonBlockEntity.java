package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class FullMoonBlockEntity extends BlockEntity {
    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public FullMoonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FULL_MOON, pos, state);
        this.uuid = UUID.randomUUID();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (level.isClientSide) {
            HandheldMoonDynamicLightsInitializer.addFullMoonBehavior(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        HandheldMoonDynamicLightsInitializer.removeFullMoonBehavior(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putUUID("uuid", uuid);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        uuid = tag.getUUID("uuid");
    }
}
