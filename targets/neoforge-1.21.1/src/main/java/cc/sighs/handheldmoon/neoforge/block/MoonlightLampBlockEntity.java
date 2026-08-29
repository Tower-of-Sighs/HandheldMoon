package cc.sighs.handheldmoon.neoforge.block;

import cc.sighs.handheldmoon.neoforge.entity.FullMoonEntity;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class MoonlightLampBlockEntity extends BlockEntity implements cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess {
    private float xRot = 0;
    private float yRot = 0;
    private boolean powered = true;
    private UUID uuid;
    private LampDeviceConfig lampConfig = LampDeviceConfig.fromGlobalConfig();
    private boolean lampConfigCustomized;

    public MoonlightLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONLIGHT_LAMP.get(), pos, state);
    }

    public void clientTick() {
        // The entity-backed light is discovered by the common client runtime.
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level == null || level.isClientSide) return;

        FullMoonEntity entity = findNearbyBoundEntity();
        if (entity == null) {
            entity = new FullMoonEntity(level);
            entity.bindToLamp(getBlockPos());
            entity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 0);
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
        entity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 0);
        MoonlightLampEntityHeartbeatCenter.report(level, entity.getUUID());
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
        if (level == null) return;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
        } else {
            syncBoundEntityState();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public float getYRot() {
        return yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
        if (level == null) return;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
        } else {
            syncBoundEntityState();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean getPowered() {
        return powered;
    }

    public LampDeviceConfig getLampConfig() {
        return lampConfigCustomized ? lampConfig : LampDeviceConfig.fromGlobalConfig();
    }

    public boolean isLampConfigCustomized() {
        return lampConfigCustomized;
    }

    public boolean hasCustomLampConfig() {
        return isLampConfigCustomized();
    }

    public void setLampConfig(LampDeviceConfig config, boolean customized) {
        this.lampConfig = config;
        this.lampConfigCustomized = customized;
        setChanged();
        if (level != null) {
            if (!level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        if (level == null) return;
        if (level.isClientSide) {
            ClientUtils.syncMoonlightLampBlock(this);
        } else {
            syncBoundEntityState();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void setRemoved() {
        FullMoonEntity entity = getBoundEntity();
        super.setRemoved();
        if (entity != null) entity.discard();
    }

    // 数据持久化全家桶，yue
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("xRot", xRot);
        tag.putFloat("yRot", yRot);
        tag.putBoolean("powered", powered);
        tag.putBoolean("lampConfigCustomized", lampConfigCustomized);
        DeviceConfigCodecs.LAMP.encodeStart(NbtOps.INSTANCE, lampConfig)
                .result()
                .ifPresent(value -> tag.put("lampConfig", value));
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
        lampConfigCustomized = tag.getBoolean("lampConfigCustomized");
        if (tag.contains("lampConfig")) {
            DeviceConfigCodecs.LAMP.parse(NbtOps.INSTANCE, tag.get("lampConfig"))
                    .result()
                    .ifPresent(value -> lampConfig = value);
        }
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
            entity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 0);
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
        newEntity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 0);
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
            if (entity.isLampBound() && pos.equals(entity.getAnchorPos())) {
                return entity;
            }
        }
        return null;
    }
}
