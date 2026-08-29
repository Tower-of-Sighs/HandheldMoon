package cc.sighs.handheldmoon.neoforge.entity;

import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.neoforge.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.neoforge.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.neoforge.util.AeronauticsUtils;
import cc.sighs.handheldmoon.util.LineLightMath;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Optional;

public class FullMoonEntity extends Entity implements FullMoonDynamicLightSource {
    private int radius = 16;
    private static final EntityDataAccessor<Optional<BlockPos>> ANCHOR_POS = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> LAMP_BOUND = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LAMP_LUMINANCE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LAMP_X_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAMP_Y_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);

    public FullMoonEntity(Level level) {
        this((EntityType<? extends FullMoonEntity>) ModEntities.MOONLIGHT.get(), level);
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
        syncToAnchor();
    }

    public BlockPos getAnchorPos() {
        return this.entityData.get(ANCHOR_POS).orElse(null);
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
        double horizontal = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
        return (float) (Mth.atan2(-vec.y, horizontal) * Mth.RAD_TO_DEG);
    }

    public static float getYRotFromVec3(Vec3 vec) {
        return (float) (Mth.atan2(-vec.x, vec.z) * Mth.RAD_TO_DEG);
    }

    public int getLampLuminance() {
        return this.entityData.get(LAMP_LUMINANCE);
    }

    @Override
    public Vec3 getLightPosition() {
        return position();
    }

    @Override
    public boolean isLightRemoved() {
        return isRemoved();
    }

    @Override
    public boolean usesEntityBackedLight() {
        return true;
    }

    public Vec3 getLampDirection() {
        Vec3 direction = LineLightMath.computeDirection(
                this.entityData.get(LAMP_Y_ROT),
                this.entityData.get(LAMP_X_ROT) - 90.0f,
                true
        ).normalize().scale(-1.0);
        BlockEntity anchor = getAnchorBlockEntity();
        return anchor != null ? AeronauticsUtils.transformDirection(anchor, direction) : direction;
    }

    public LampDeviceConfig getLampConfig() {
        BlockEntity anchor = getAnchorBlockEntity();
        return anchor instanceof MoonlightLampBlockEntity lamp
                ? lamp.getLampConfig()
                : LampDeviceConfig.fromGlobalConfig();
    }

    public FullMoonDeviceConfig getFullMoonConfig() {
        BlockEntity anchor = getAnchorBlockEntity();
        return anchor instanceof FullMoonBlockEntity moon
                ? moon.getFullMoonConfig()
                : FullMoonDeviceConfig.fromGlobalConfig();
    }

    private BlockEntity getAnchorBlockEntity() {
        BlockPos anchor = getAnchorPos();
        return anchor != null ? level().getBlockEntity(anchor) : null;
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
            syncToAnchor();
            BlockPos anchor = getAnchorPos();
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
