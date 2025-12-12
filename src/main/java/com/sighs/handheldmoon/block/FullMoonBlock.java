package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FullMoonBlock extends BaseEntityBlock {
    public FullMoonBlock() {
        super(BlockBehaviour.Properties.of().noCollission().strength(1f));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide) {
            HandheldMoonDynamicLightsInitializer.ensureFullMoonBehaviorAt(pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level.isClientSide) {
            HandheldMoonDynamicLightsInitializer.removeFullMoonBehaviorAt(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new FullMoonBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var item = new ItemStack(this);
        return List.of(item);
    }
}
