package com.sighs.handheldmoon.entity;

import com.sighs.handheldmoon.block.FullMoonBlock;
import com.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import com.sighs.handheldmoon.registry.ModEntities;
import com.sighs.handheldmoon.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FullMoonEntity extends Entity {
    private int radius = 16;
    private BlockPos anchorPos;
    private static final EntityDataAccessor<Boolean> LAMP_BOUND = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LAMP_LUMINANCE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LAMP_X_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAMP_Y_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);

    public FullMoonEntity(Level level) {
        this(ModEntities.MOONLIGHT.get(), level);
    }

    public FullMoonEntity(EntityType<? extends FullMoonEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LAMP_BOUND, false);
        builder.define(LAMP_LUMINANCE, 15);
        builder.define(LAMP_X_ROT, 0.0f);
        builder.define(LAMP_Y_ROT, 0.0f);
    }

    public void setAnchor(BlockPos pos) {
        this.anchorPos = pos;
    }

    public void bindToLamp(BlockPos pos) {
        this.anchorPos = pos;
        this.entityData.set(LAMP_BOUND, true);
        syncToAnchor();
    }

    public boolean isLampBound() {
        return this.entityData.get(LAMP_BOUND);
    }

    public void setLampState(float xRot, float yRot, int luminance) {
        this.entityData.set(LAMP_X_ROT, xRot);
        this.entityData.set(LAMP_Y_ROT, yRot);
        this.entityData.set(LAMP_LUMINANCE, luminance);
    }

    public float getLampXRot() {
        return this.entityData.get(LAMP_X_ROT);
    }

    public float getLampYRot() {
        return this.entityData.get(LAMP_Y_ROT);
    }

    public int getLampLuminance() {
        return this.entityData.get(LAMP_LUMINANCE);
    }

    private void syncToAnchor() {
        if (anchorPos == null) return;
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(anchorPos.getX() + 0.5, anchorPos.getY() + 0.4, anchorPos.getZ() + 0.5);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (!level().isClientSide) {
            if (isLampBound()) {
                syncToAnchor();
                if (!MoonlightLampEntityHeartbeatCenter.isAlive(level(), this.getUUID())) {
                    discard();
                }
                return;
            }
            BlockPos checkPos = anchorPos != null ? anchorPos : blockPosition();
            BlockState state = level().getBlockState(checkPos);
            if (!(state.getBlock() instanceof FullMoonBlock)) {
                discard();
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        radius = tag.getInt("radius");
        if (tag.contains("ax") && tag.contains("ay") && tag.contains("az")) {
            anchorPos = new BlockPos(tag.getInt("ax"), tag.getInt("ay"), tag.getInt("az"));
        } else {
            anchorPos = null;
        }
        if (tag.contains("lamp_bound")) {
            this.entityData.set(LAMP_BOUND, tag.getBoolean("lamp_bound"));
        }
        if (tag.contains("lamp_luminance")) {
            this.entityData.set(LAMP_LUMINANCE, tag.getInt("lamp_luminance"));
        }
        if (tag.contains("lamp_x_rot")) {
            this.entityData.set(LAMP_X_ROT, tag.getFloat("lamp_x_rot"));
        }
        if (tag.contains("lamp_y_rot")) {
            this.entityData.set(LAMP_Y_ROT, tag.getFloat("lamp_y_rot"));
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
        tag.putBoolean("lamp_bound", isLampBound());
        tag.putInt("lamp_luminance", getLampLuminance());
        tag.putFloat("lamp_x_rot", getLampXRot());
        tag.putFloat("lamp_y_rot", getLampYRot());
    }
}
