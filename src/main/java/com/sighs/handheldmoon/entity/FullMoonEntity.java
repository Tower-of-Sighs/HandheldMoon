package com.sighs.handheldmoon.entity;

import com.sighs.handheldmoon.block.FullMoonBlock;
import com.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import com.sighs.handheldmoon.registry.ModEntities;
import com.sighs.handheldmoon.util.AeronauticsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Optional;

public class FullMoonEntity extends Entity {
    private int radius = 16;
    private static final EntityDataAccessor<Optional<BlockPos>> ANCHOR_POS = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
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
        builder.define(ANCHOR_POS, Optional.empty());
        builder.define(LAMP_BOUND, false);
        builder.define(LAMP_LUMINANCE, 15);
        builder.define(LAMP_X_ROT, 0.0f);
        builder.define(LAMP_Y_ROT, 0.0f);
    }

    public void setAnchor(BlockPos pos) {
        this.entityData.set(ANCHOR_POS, Optional.ofNullable(pos));
    }

    public void bindToLamp(BlockPos pos) {
        this.entityData.set(ANCHOR_POS, Optional.ofNullable(pos));
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
        Optional<BlockPos> blockPos = this.entityData.get(ANCHOR_POS);
        if (blockPos.isPresent()) {
            BlockEntity be = level().getBlockEntity(blockPos.get());
            if (be != null && AeronauticsUtils.isPhysicalized(be)) {
                Quaterniond direction = AeronauticsUtils.getPhysicalizedRenderOrientation(be);
                if (direction != null) {
                    Vec3 angle = calculateUpVector(this.entityData.get(LAMP_X_ROT), this.entityData.get(LAMP_Y_ROT));
                    Vector3d jomlVec = new Vector3d(angle.x, angle.y, angle.z);
                    direction.transform(jomlVec);
                    return getXRotFromVec3(new Vec3(jomlVec.x, jomlVec.y, jomlVec.z)) + 90;
                }
            }
        }
        return this.entityData.get(LAMP_X_ROT);
    }

    public float getLampYRot() {
        Optional<BlockPos> blockPos = this.entityData.get(ANCHOR_POS);
        if (blockPos.isPresent()) {
            BlockEntity be = level().getBlockEntity(blockPos.get());
            if (be != null && AeronauticsUtils.isPhysicalized(be)) {
                Quaterniond direction = AeronauticsUtils.getPhysicalizedRenderOrientation(be);
                if (direction != null) {
                    Vec3 angle = calculateUpVector(this.entityData.get(LAMP_X_ROT), this.entityData.get(LAMP_Y_ROT));
                    Vector3d jomlVec = new Vector3d(angle.x, angle.y, angle.z);
                    direction.transform(jomlVec);
                    return 180 - getYRotFromVec3(new Vec3(jomlVec.x, jomlVec.y, jomlVec.z));
                }
            }
        }
        return this.entityData.get(LAMP_Y_ROT);
    }

    public static float getXRotFromVec3(Vec3 vec) {
        return (float) Math.toDegrees(Math.asin(-vec.y));
    }

    public static float getYRotFromVec3(Vec3 vec) {
        return (float) Math.toDegrees(Math.atan2(-vec.x, vec.z));
    }

    public int getLampLuminance() {
        return this.entityData.get(LAMP_LUMINANCE);
    }

    private void syncToAnchor() {
        BlockPos pos = this.entityData.get(ANCHOR_POS).orElse(null);
        if (pos == null) return;
        this.setDeltaMovement(Vec3.ZERO);
        if (AeronauticsUtils.isPhysicalized(level(), pos)) {
            Vec3 renderPos = AeronauticsUtils.getPhysicalizedRenderPosition(level(), pos, new Vec3(0.5, 0.4, 0.5));
            if (renderPos != null) this.moveTo(renderPos);
        } else {
            this.moveTo(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
        }
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
            BlockPos anchor = this.entityData.get(ANCHOR_POS).orElse(null);
            BlockPos checkPos = anchor != null ? anchor : blockPosition();
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
            this.entityData.set(ANCHOR_POS, Optional.of(new BlockPos(tag.getInt("ax"), tag.getInt("ay"), tag.getInt("az"))));
        } else {
            this.entityData.set(ANCHOR_POS, Optional.empty());
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
        BlockPos anchorPos = this.entityData.get(ANCHOR_POS).orElse(null);
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
