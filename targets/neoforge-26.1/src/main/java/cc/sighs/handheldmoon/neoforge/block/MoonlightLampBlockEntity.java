package cc.sighs.handheldmoon.neoforge.block;

import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.MoonlightLampEntityHeartbeatCenter;
import cc.sighs.handheldmoon.neoforge.entity.FullMoonEntity;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.util.ClientUtils;
import cc.sighs.handheldmoon.util.LineLightMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MoonlightLampBlockEntity extends BlockEntity implements cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess {
    private float xRot = 0;
    private float yRot = 0;
    private boolean powered = true;
    private LampDeviceConfig lampConfig = LampDeviceConfig.fromGlobalConfig();
    private boolean lampConfigCustomized = false;
    private boolean clientInited = false;
    private UUID uuid = UUID.randomUUID();

    public MoonlightLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONLIGHT_LAMP.get(), pos, state);
    }

    public void clientTick() {
        if (!clientInited) {
            clientInited = true;
        }
    }

    @Override
    public void serverTick() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        FullMoonEntity entity = ensureBoundEntity();
        if (entity == null) {
            return;
        }
        entity.bindToLamp(getBlockPos());
        entity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 0);
        MoonlightLampEntityHeartbeatCenter.report(level, entity.getUUID());
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
        if (level != null && level.isClientSide()) {
            ClientUtils.syncMoonlightLampBlock(this);
        } else if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public float getYRot() {
        return yRot;
    }

    public Vec3 getViewVec() {
        return LineLightMath.computeDirection(this.getYRot(), this.getXRot() - 90.0f, true);
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
        if (level != null && level.isClientSide()) {
            ClientUtils.syncMoonlightLampBlock(this);
        } else if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean getPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        if (level != null && level.isClientSide()) {
            ClientUtils.syncMoonlightLampBlock(this);
        } else if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public LampDeviceConfig getLampConfig() {
        return lampConfigCustomized ? lampConfig : LampDeviceConfig.fromGlobalConfig();
    }

    public void setLampConfig(LampDeviceConfig lampConfig) {
        setLampConfig(lampConfig, true);
    }

    public void setLampConfig(LampDeviceConfig lampConfig, boolean customized) {
        this.lampConfig = lampConfig;
        this.lampConfigCustomized = customized;
        if (level != null && level.isClientSide()) {
        } else if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }

    public boolean hasCustomLampConfig() {
        return lampConfigCustomized;
    }

    public UUID getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("xRot", xRot);
        output.putFloat("yRot", yRot);
        output.putBoolean("powered", powered);
        output.putString("uuid", getUuid().toString());
        output.putBoolean("lampConfigCustomized", lampConfigCustomized);
        if (lampConfigCustomized) {
            output.store("lampConfig", DeviceConfigCodecs.LAMP, lampConfig);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        xRot = input.getFloatOr("xRot", 0f);
        yRot = input.getFloatOr("yRot", 0f);
        powered = input.getBooleanOr("powered", true);
        uuid = UUID.fromString(input.getStringOr("uuid", UUID.randomUUID().toString()));
        lampConfigCustomized = input.getBooleanOr("lampConfigCustomized", false);
        lampConfig = input.read("lampConfig", DeviceConfigCodecs.LAMP).orElse(LampDeviceConfig.fromGlobalConfig());
    }

    @Override
    public void setRemoved() {
        FullMoonEntity entity = getBoundEntity();
        super.setRemoved();
        if (entity != null) entity.discard();
    }

    private FullMoonEntity ensureBoundEntity() {
        FullMoonEntity entity = getBoundEntity();
        if (entity != null) {
            return entity;
        }
        entity = findNearbyBoundEntity();
        if (entity != null) {
            uuid = entity.getUUID();
            setChanged();
            return entity;
        }
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return null;
        }
        entity = new FullMoonEntity(serverLevel);
        entity.bindToLamp(getBlockPos());
        entity.setLampState(getXRot(), getYRot(), getPowered() ? 15 : 0);
        serverLevel.addFreshEntity(entity);
        uuid = entity.getUUID();
        setChanged();
        return entity;
    }

    private FullMoonEntity getBoundEntity() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel) || uuid == null) {
            return null;
        }
        var entity = serverLevel.getEntity(uuid);
        return entity instanceof FullMoonEntity fullMoon && fullMoon.isLampBound() ? fullMoon : null;
    }

    private FullMoonEntity findNearbyBoundEntity() {
        if (level == null) {
            return null;
        }
        BlockPos pos = getBlockPos();
        AABB box = new AABB(pos.getX() + 0.25, pos.getY() + 0.15, pos.getZ() + 0.25,
                pos.getX() + 0.75, pos.getY() + 0.65, pos.getZ() + 0.75);
        for (FullMoonEntity entity : level.getEntitiesOfClass(FullMoonEntity.class, box)) {
            if (entity.isLampBound() && pos.equals(entity.getAnchorPos())) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }
}
