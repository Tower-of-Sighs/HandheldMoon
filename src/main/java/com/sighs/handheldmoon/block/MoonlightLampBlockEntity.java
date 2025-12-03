package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MoonlightLampBlockEntity extends BlockEntity {
    private float xRot = 0;
    private float yRot = 0;
    private boolean powered = true;

    private boolean clientInited = false;

    public MoonlightLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONLIGHT_LAMP, pos, state);
    }

    public void clientTick() {
        if (!clientInited) {
            clientInited = true;
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        }
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        } else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public float getYRot() {
        return yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        } else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean getPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        } else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // 数据持久化全家桶，yue
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("xRot", xRot);
        tag.putFloat("yRot", yRot);
        tag.putBoolean("powered", powered);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        xRot = tag.getFloat("xRot");
        yRot = tag.getFloat("yRot");
        powered = tag.getBoolean("powered");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }
}
