package cc.sighs.handheldmoon.neoforge.entity;

import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.neoforge.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class FullMoonEntity extends Entity implements FullMoonDynamicLightSource {
    private int radius = 16;
    private static final EntityDataAccessor<Optional<BlockPos>> ANCHOR_POS = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> LAMP_BOUND = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LAMP_LUMINANCE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LAMP_X_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAMP_Y_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> LIGHT_PROFILE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> LIGHT_PROFILE_OVERRIDE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.BOOLEAN);

    public FullMoonEntity(Level level) {
        this((EntityType<? extends FullMoonEntity>) ModEntities.MOONLIGHT.get(), level);
    }

    public FullMoonEntity(EntityType<? extends FullMoonEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void setAnchor(BlockPos pos) {
        entityData.set(ANCHOR_POS, Optional.ofNullable(pos));
        entityData.set(LAMP_BOUND, false);
        syncToAnchor();
    }

    public void bindToLamp(BlockPos pos) {
        entityData.set(ANCHOR_POS, Optional.ofNullable(pos));
        entityData.set(LAMP_BOUND, true);
        syncToAnchor();
    }

    public void setLampState(float xRot, float yRot, int luminance) {
        entityData.set(LAMP_X_ROT, xRot);
        entityData.set(LAMP_Y_ROT, yRot);
        entityData.set(LAMP_LUMINANCE, luminance);
    }

    @Override
    public BlockPos getAnchorPos() {
        return entityData.get(ANCHOR_POS).orElse(null);
    }

    @Override
    public boolean isLampBound() {
        return entityData.get(LAMP_BOUND);
    }

    @Override
    public int getLampLuminance() {
        return entityData.get(LAMP_LUMINANCE);
    }

    @Override
    public Vec3 getLightPosition() {
        return position();
    }

    @Override
    public Vec3 getLampDirection() {
        return cc.sighs.handheldmoon.util.LineLightMath.computeDirection(
                entityData.get(LAMP_Y_ROT), entityData.get(LAMP_X_ROT) - 90.0f, true
        ).normalize().scale(-1.0);
    }

    @Override
    public LampDeviceConfig getLampConfig() {
        BlockEntity blockEntity = getAnchorBlockEntity();
        return blockEntity instanceof cc.sighs.handheldmoon.neoforge.block.MoonlightLampBlockEntity lamp
                ? lamp.getLampConfig() : LampDeviceConfig.fromGlobalConfig();
    }

    @Override
    public FullMoonDeviceConfig getFullMoonConfig() {
        BlockEntity blockEntity = getAnchorBlockEntity();
        return blockEntity instanceof FullMoonBlockEntity moon
                ? moon.getFullMoonConfig()
                : FullMoonDeviceConfig.fromGlobalConfig();
    }

    @Override
    public boolean isLightRemoved() {
        return isRemoved();
    }

    @Override
    public boolean usesEntityBackedLight() {
        return true;
    }

    @Override
    public EntityLightProfile getLightProfile() {
        if (!entityData.get(LIGHT_PROFILE_OVERRIDE)) {
            return FullMoonDynamicLightSource.super.getLightProfile();
        }
        EntityLightProfile profile = EntityLightProfile.fromNetworkString(entityData.get(LIGHT_PROFILE));
        return profile != null ? profile : FullMoonDynamicLightSource.super.getLightProfile();
    }

    @Override
    public void setLightProfile(EntityLightProfile profile) {
        entityData.set(LIGHT_PROFILE, profile.toNetworkString());
        entityData.set(LIGHT_PROFILE_OVERRIDE, true);
    }

    @Override
    public void clearLightProfileOverride() {
        entityData.set(LIGHT_PROFILE, "");
        entityData.set(LIGHT_PROFILE_OVERRIDE, false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ANCHOR_POS, Optional.empty());
        builder.define(LAMP_BOUND, false);
        builder.define(LAMP_LUMINANCE, 15);
        builder.define(LAMP_X_ROT, 0.0f);
        builder.define(LAMP_Y_ROT, 0.0f);
        builder.define(LIGHT_PROFILE, "");
        builder.define(LIGHT_PROFILE_OVERRIDE, false);
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (!level().isClientSide()) {
            if (isLampBound()) {
                syncToAnchor();
                if (!MoonlightLampEntityHeartbeatCenter.isAlive(level(), getUUID())) discard();
                return;
            }
            syncToAnchor();
            BlockPos checkPos = getAnchorPos() != null ? getAnchorPos() : blockPosition();
            BlockState state = level().getBlockState(checkPos);
            if (!(state.getBlock() instanceof FullMoonBlock)) {
                discard();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        radius = input.getIntOr("radius", 16);
        if (input.getInt("ax").isPresent() && input.getInt("ay").isPresent() && input.getInt("az").isPresent()) {
            entityData.set(ANCHOR_POS, Optional.of(new BlockPos(
                    input.getIntOr("ax", 0),
                    input.getIntOr("ay", 0),
                    input.getIntOr("az", 0)
            )));
        } else {
            entityData.set(ANCHOR_POS, Optional.empty());
        }
        entityData.set(LAMP_BOUND, input.getBooleanOr("lamp_bound", false));
        entityData.set(LAMP_LUMINANCE, input.getIntOr("lamp_luminance", 15));
        entityData.set(LAMP_X_ROT, input.getFloatOr("lamp_x_rot", 0.0f));
        entityData.set(LAMP_Y_ROT, input.getFloatOr("lamp_y_rot", 0.0f));
        entityData.set(LIGHT_PROFILE_OVERRIDE, input.getBooleanOr("light_profile_override", false));
        input.read("light_profile", EntityLightProfile.CODEC).ifPresent(this::setLightProfile);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("radius", radius);
        BlockPos anchorPos = getAnchorPos();
        if (anchorPos != null) {
            output.putInt("ax", anchorPos.getX());
            output.putInt("ay", anchorPos.getY());
            output.putInt("az", anchorPos.getZ());
        }
        output.putBoolean("lamp_bound", isLampBound());
        output.putInt("lamp_luminance", getLampLuminance());
        output.putFloat("lamp_x_rot", entityData.get(LAMP_X_ROT));
        output.putFloat("lamp_y_rot", entityData.get(LAMP_Y_ROT));
        output.putBoolean("light_profile_override", entityData.get(LIGHT_PROFILE_OVERRIDE));
        if (entityData.get(LIGHT_PROFILE_OVERRIDE)) {
            output.store("light_profile", EntityLightProfile.CODEC, getLightProfile());
        }
    }

    private BlockEntity getAnchorBlockEntity() {
        BlockPos anchor = getAnchorPos();
        return anchor == null ? null : level().getBlockEntity(anchor);
    }

    private void syncToAnchor() {
        BlockPos pos = getAnchorPos();
        if (pos == null) return;
        setDeltaMovement(Vec3.ZERO);
        setPos(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
    }
}
