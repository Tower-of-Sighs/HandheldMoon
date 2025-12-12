package com.sighs.handheldmoon.entity;

import com.sighs.handheldmoon.block.FullMoonBlock;
import com.sighs.handheldmoon.registry.ModEntities;
import com.sighs.handheldmoon.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FullMoonEntity extends ThrowableItemProjectile {
    private int radius = 16;
    private BlockPos anchorPos;

    public FullMoonEntity(Level level) {
        this(ModEntities.MOONLIGHT, level);
    }

    public FullMoonEntity(EntityType<? extends FullMoonEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void setAnchor(BlockPos pos) {
        this.anchorPos = pos;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            BlockPos checkPos = anchorPos != null ? anchorPos : blockPosition();
            BlockState state = level().getBlockState(checkPos);
            if (!(state.getBlock() instanceof FullMoonBlock)) {
                discard();
            }
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FULL_MOON;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        radius = tag.getIntOr("radius", 16);
        if (tag.contains("ax") && tag.contains("ay") && tag.contains("az")) {
            int x = tag.getIntOr("ax", 0);
            int y = tag.getIntOr("ay", 0);
            int z = tag.getIntOr("az", 0);

            anchorPos = (x == 0 && y == 0 && z == 0) ? null : new BlockPos(x, y, z);
        } else {
            anchorPos = null;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("radius", radius);
        if (anchorPos != null) {
            tag.putInt("ax", anchorPos.getX());
            tag.putInt("ay", anchorPos.getY());
            tag.putInt("az", anchorPos.getZ());
        }
    }
}
