package cc.sighs.handheldmoon.entity;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.util.LineLightMath;
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

import java.util.Optional;

public class FullMoonEntity extends Entity implements FullMoonDynamicLightSource {
    private int radius = 16;
    private String lightProfileCacheKey;
    private EntityLightProfile lightProfileCache;
    private boolean resolvingAnchor;
    private static final EntityDataAccessor<Optional<BlockPos>> ANCHOR_POS = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> LAMP_BOUND = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LAMP_LUMINANCE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LAMP_X_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAMP_Y_ROT = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> LIGHT_PROFILE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> LIGHT_PROFILE_OVERRIDE = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SOURCE_ID = SynchedEntityData.defineId(FullMoonEntity.class, EntityDataSerializers.STRING);

    public FullMoonEntity(Level level) {
        this((EntityType<? extends FullMoonEntity>) ModEntities.MOONLIGHT.get(), level);
    }

    public FullMoonEntity(EntityType<? extends FullMoonEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ANCHOR_POS, Optional.empty());
        this.entityData.define(LAMP_BOUND, false);
        this.entityData.define(LAMP_LUMINANCE, 15);
        this.entityData.define(LAMP_X_ROT, 0.0f);
        this.entityData.define(LAMP_Y_ROT, 0.0f);
        this.entityData.define(LIGHT_PROFILE, "");
        this.entityData.define(LIGHT_PROFILE_OVERRIDE, false);
        this.entityData.define(SOURCE_ID, "");
    }

    public void setAnchor(BlockPos pos) {
        this.entityData.set(ANCHOR_POS, Optional.ofNullable(pos));
        this.entityData.set(LAMP_BOUND, false);
        setSourceId("handheldmoon:full_moon");
        syncDefaultLightProfile();
        syncToAnchor();
    }

    public BlockPos getAnchorPos() {
        return this.entityData.get(ANCHOR_POS).orElse(null);
    }

    public void bindToLamp(BlockPos pos) {
        this.entityData.set(ANCHOR_POS, Optional.ofNullable(pos));
        this.entityData.set(LAMP_BOUND, true);
        setSourceId("handheldmoon:moonlight_lamp");
        syncDefaultLightProfile();
        syncToAnchor();
    }

    @Override
    public String getSourceId() {
        return this.entityData.get(SOURCE_ID);
    }

    public void setSourceId(String sourceId) {
        String value = sourceId == null ? "" : sourceId;
        if (!this.entityData.get(SOURCE_ID).equals(value)) {
            this.entityData.set(SOURCE_ID, value);
        }
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

    @Override
    public EntityLightProfile getLightProfile() {
        String encoded = entityData.get(LIGHT_PROFILE);
        if (encoded.equals(lightProfileCacheKey)) {
            return lightProfileCache != null
                    ? lightProfileCache : FullMoonDynamicLightSource.super.getDefaultLightProfile();
        }
        EntityLightProfile profile = EntityLightProfile.fromNetworkString(encoded);
        lightProfileCacheKey = encoded;
        lightProfileCache = profile;
        return profile != null ? profile : FullMoonDynamicLightSource.super.getDefaultLightProfile();
    }

    @Override
    public void setLightProfile(EntityLightProfile profile) {
        writeLightProfile(profile);
        entityData.set(LIGHT_PROFILE_OVERRIDE, true);
    }

    @Override
    public void clearLightProfileOverride() {
        entityData.set(LIGHT_PROFILE_OVERRIDE, false);
        syncDefaultLightProfile();
    }

    /** Refreshes the synchronized profile when the bound device uses defaults. */
    public void syncDefaultLightProfile() {
        if (entityData.get(LIGHT_PROFILE_OVERRIDE)) {
            return;
        }
        EntityLightProfile profile = FullMoonDynamicLightSource.super.getDefaultLightProfile();
        if (!profile.equals(lightProfileCache)) {
            writeLightProfile(profile);
        }
    }

    private void writeLightProfile(EntityLightProfile profile) {
        String encoded = profile.toNetworkString();
        if (!encoded.equals(entityData.get(LIGHT_PROFILE))) {
            entityData.set(LIGHT_PROFILE, encoded);
        }
        lightProfileCacheKey = encoded;
        lightProfileCache = profile;
    }

    public Vec3 getLampDirection() {
        return LineLightMath.computeDirection(
                this.entityData.get(LAMP_Y_ROT),
                this.entityData.get(LAMP_X_ROT) - 90.0f,
                true
        ).normalize().scale(-1.0);
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
        if (resolvingAnchor) {
            // The anchor chunk may still be registering this block entity; a
            // nested lookup would re-enter setLevel -> bindToLamp -> profile.
            return null;
        }
        BlockPos anchor = getAnchorPos();
        if (anchor == null) {
            return null;
        }
        resolvingAnchor = true;
        try {
            return level().getBlockEntity(anchor);
        } finally {
            resolvingAnchor = false;
        }
    }

    private void syncToAnchor() {
        BlockPos pos = entityData.get(ANCHOR_POS).orElse(null);
        if (pos == null) return;
        this.setDeltaMovement(Vec3.ZERO);
        this.moveTo(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (!level().isClientSide) {
            syncDefaultLightProfile();
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
        this.entityData.set(LIGHT_PROFILE_OVERRIDE, tag.getBoolean("light_profile_override"));
        if (tag.get("light_profile") instanceof CompoundTag profileTag) {
            EntityLightProfile profile = EntityLightProfile.fromTag(profileTag);
            if (profile != null) {
                writeLightProfile(profile);
            }
        }
        if (tag.contains("source_id")) {
            setSourceId(tag.getString("source_id"));
        }
        if (entityData.get(LIGHT_PROFILE).isEmpty()) {
            syncDefaultLightProfile();
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
        tag.putBoolean("light_profile_override", entityData.get(LIGHT_PROFILE_OVERRIDE));
        tag.put("light_profile", getLightProfile().toTag());
        tag.putString("source_id", getSourceId());
    }
}
