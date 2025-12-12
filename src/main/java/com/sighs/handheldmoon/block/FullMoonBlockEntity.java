package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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

        if (!level.isClientSide) {
            BlockPos pos = getBlockPos();
            AABB box = new AABB(
                    pos.getX() + 0.5 - 0.25, pos.getY() + 0.5 - 0.25, pos.getZ() + 0.5 - 0.25,
                    pos.getX() + 0.5 + 0.25, pos.getY() + 0.5 + 0.25, pos.getZ() + 0.5 + 0.25
            );
            if (level.getEntitiesOfClass(FullMoonEntity.class, box).isEmpty()) {
                FullMoonEntity entity = new FullMoonEntity(level);
                entity.setPos(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
                entity.setAnchor(pos);
                level.addFreshEntity(entity);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        HandheldMoonDynamicLightsInitializer.removeFullMoonBehavior(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.storeNullable("uuid", UUIDUtil.CODEC, uuid);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        uuid = tag.read("uuid", UUIDUtil.CODEC).orElse(null);
    }
}
