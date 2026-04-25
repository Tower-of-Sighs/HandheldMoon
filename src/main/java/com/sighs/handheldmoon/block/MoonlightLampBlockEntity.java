package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.util.AeronauticsUtils;
import com.sighs.handheldmoon.util.ClientUtils;
import com.sighs.handheldmoon.util.LineLightMath;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.UUID;

public class MoonlightLampBlockEntity extends BlockEntity {
    @Getter
    private float xRot = 0;
    @Getter
    private float yRot = 0;
    private boolean powered = true;
    private boolean clientInited = false;
    @Setter
    @Getter
    private UUID uuid;

    public MoonlightLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONLIGHT_LAMP.get(), pos, state);
    }

    public void clientTick() {
        if (!clientInited) {
            clientInited = true;
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level == null || level.isClientSide) return;

        FullMoonEntity entity = findNearbyBoundEntity();
        if (entity == null) {
            entity = new FullMoonEntity(level);
            entity.bindToLamp(getBlockPos());
            entity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 1);
            level.addFreshEntity(entity);
        }

        setUuid(entity.getUUID());
        setChanged();
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel)) return;
        FullMoonEntity entity = ensureBoundEntity();
        if (entity == null) return;
        entity.bindToLamp(getBlockPos());
        if (AeronauticsUtils.isPhysicalized(this)) {
            Vec3 position = AeronauticsUtils.getPhysicalizedRenderPosition(this);
            if (position != null) entity.moveTo(position);
            Quaterniond direction = AeronauticsUtils.getPhysicalizedRenderOrientation(this);
            if (direction != null) {
                Vec3 angle = entity.getLookAngle();
                Vector3d jomlVec = new Vector3d(angle.x, angle.y, angle.z);
                direction.transform(jomlVec);
            }
        }
        else MoonlightLampEntityHeartbeatCenter.report(level, entity.getUUID());
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
        if (level == null) return;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        } else {
            syncBoundEntityState();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public Vec3 getViewVec() {
        return LineLightMath.computeDirection(this.getYRot(), this.getXRot() - 90.0f, true);
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
        if (level == null) return;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        } else {
            syncBoundEntityState();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean getPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        if (level == null) return;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        } else {
            syncBoundEntityState();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 数据持久化全家桶，yue
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("xRot", xRot);
        tag.putFloat("yRot", yRot);
        tag.putBoolean("powered", powered);
        if (uuid != null) {
            tag.putUUID("uuid", uuid);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        xRot = tag.getFloat("xRot");
        yRot = tag.getFloat("yRot");
        powered = tag.getBoolean("powered");
        if (tag.hasUUID("uuid")) {
            uuid = tag.getUUID("uuid");
        }
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

    private void syncBoundEntityState() {
        FullMoonEntity entity = getBoundEntity();
        if (entity != null) {
            entity.bindToLamp(getBlockPos());
            entity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 1);
        }
    }

    private FullMoonEntity ensureBoundEntity() {
        FullMoonEntity entity = getBoundEntity();
        if (entity != null) {
            return entity;
        }

        entity = findNearbyBoundEntity();
        if (entity != null) {
            setUuid(entity.getUUID());
            setChanged();
            return entity;
        }

        if (!(level instanceof ServerLevel serverLevel)) return null;
        FullMoonEntity newEntity = new FullMoonEntity(serverLevel);
        newEntity.bindToLamp(getBlockPos());
        newEntity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 1);
        serverLevel.addFreshEntity(newEntity);
        setUuid(newEntity.getUUID());
        setChanged();
        return newEntity;
    }

    private FullMoonEntity getBoundEntity() {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        if (uuid == null) return null;
        var entity = serverLevel.getEntity(uuid);
        return entity instanceof FullMoonEntity fullMoon ? fullMoon : null;
    }

    private FullMoonEntity findNearbyBoundEntity() {
        if (level == null) return null;
        BlockPos pos = getBlockPos();
        AABB box = new AABB(
                pos.getX() + 0.5 - 0.25, pos.getY() + 0.4 - 0.25, pos.getZ() + 0.5 - 0.25,
                pos.getX() + 0.5 + 0.25, pos.getY() + 0.4 + 0.25, pos.getZ() + 0.5 + 0.25
        );
        for (FullMoonEntity entity : level.getEntitiesOfClass(FullMoonEntity.class, box)) {
            if (entity.isLampBound()) {
                return entity;
            }
        }
        return null;
    }
}
