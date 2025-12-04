package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.util.ClientUtils;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MoonlightLampBlockEntity extends BlockEntity {
    private float xRot = 0;
    private float yRot = 0;
    private boolean powered = true;
    private boolean clientInited = false;
    private UUID uuid = UUID.randomUUID();

    public MoonlightLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONLIGHT_LAMP, pos, state);
    }

    public void clientTick() {
        if (!clientInited) {
            clientInited = true;
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        }
    }

    public Vec3 getViewVec() {
        return Utils.calculateViewVector(xRot, yRot);
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
}
