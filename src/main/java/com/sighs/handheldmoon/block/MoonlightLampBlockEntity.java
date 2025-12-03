package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.init.ClientUtils;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MoonlightLampBlockEntity extends BlockEntity {
    private float xRot = 0;
    private float yRot = 0;
    private boolean powered = true;
    private UUID uuid = UUID.randomUUID();

    public MoonlightLampBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntities.MOONLIGHT_LAMP.get(), p_155229_, p_155230_);
    }

    public Vec3 getViewVec() {
        return Utils.calculateViewVector(xRot, yRot);
    }

    public float getXRot() {
        return xRot;
    }
    public void setXRot(float xRot) {
        this.xRot = xRot;
        if (level.isClientSide) ClientUtils.syncMoonlightLampBlock(this);
        else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public float getYRot() {
        return yRot;
    }
    public void setYRot(float yRot) {
        this.yRot = yRot;
        if (level.isClientSide) ClientUtils.syncMoonlightLampBlock(this);
        else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public boolean getPowered() {
        return powered;
    }
    public void setPowered(boolean powered) {
        this.powered = powered;
        if (level.isClientSide) ClientUtils.syncMoonlightLampBlock(this);
        else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public UUID getUuid() {
        if (uuid == null) uuid = UUID.randomUUID();
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    // 数据持久化全家桶，yue
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("xRot", getXRot());
        tag.putFloat("yRot", getYRot());
        tag.putBoolean("powered", getPowered());
        tag.putUUID("uuid", getUuid());
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        xRot = tag.getFloat("xRot");
        yRot = tag.getFloat("yRot");
        powered = tag.getBoolean("powered");
        uuid = tag.getUUID("uuid");
    }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            load(pkt.getTag());
        }
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
}
