package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.registry.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

    public FullMoonBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntities.FULL_MOON.get(), p_155229_, p_155230_);
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
