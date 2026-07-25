package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
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
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MoonlightLampBlockEntity extends BlockEntity {
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
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
        }
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
        if (level != null && level.isClientSide()) {
            ClientUtils.syncMoonlightLampBlock(this);
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
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
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
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
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
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
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(this);
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
        lampConfigCustomized = input.getBooleanOr("lampConfigCustomized", false);
        lampConfig = input.read("lampConfig", DeviceConfigCodecs.LAMP).orElse(LampDeviceConfig.fromGlobalConfig());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        HandheldMoonDynamicLightsInitializer.removeLampBehaviorAt(getBlockPos());
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
