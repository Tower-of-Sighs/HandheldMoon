package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.registry.BlockEntities;
import com.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MoonlightLampBlockEntity extends BlockEntity {
    private float xRot = 0;
    private float yRot = 0;
    private boolean powered = true;

    public MoonlightLampBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntities.MOONLIGHT_LAMP, p_155229_, p_155230_);
    }

    public float getXRot() {
        return xRot;
    }
    public void setXRot(float xRot) {
        this.xRot = xRot;
        if (level.isClientSide()) ClientUtils.syncMoonlightLampBlock(this);
        else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public float getYRot() {
        return yRot;
    }
    public void setYRot(float yRot) {
        this.yRot = yRot;
        if (level.isClientSide()) ClientUtils.syncMoonlightLampBlock(this);
        else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public boolean getPowered() {
        return powered;
    }
    public void setPowered(boolean powered) {
        this.powered = powered;
        if (level.isClientSide()) ClientUtils.syncMoonlightLampBlock(this);
        else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // 数据持久化全家桶，yue

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("xRot", xRot);
        output.putFloat("yRot", yRot);
        output.putBoolean("powered", powered);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        xRot = input.getFloatOr("xRot", 0.0f);
        yRot = input.getFloatOr("yRot", 0.0f);
        powered = input.getBooleanOr("powered", false);
    }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        var output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        saveAdditional(output);
        return output.buildResult();
    }
}