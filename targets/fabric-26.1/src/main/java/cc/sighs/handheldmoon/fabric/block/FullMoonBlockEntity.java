package cc.sighs.handheldmoon.fabric.block;

import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.fabric.entity.FullMoonEntity;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class FullMoonBlockEntity extends BlockEntity implements cc.sighs.handheldmoon.api.content.FullMoonBlockEntityAccess {
    private UUID uuid;
    private FullMoonDeviceConfig fullMoonConfig = FullMoonDeviceConfig.fromGlobalConfig();
    private boolean fullMoonConfigCustomized = false;

    public FullMoonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FULL_MOON.get(), pos, state);
        this.uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public FullMoonDeviceConfig getFullMoonConfig() {
        return fullMoonConfigCustomized ? fullMoonConfig : FullMoonDeviceConfig.fromGlobalConfig();
    }

    public void setFullMoonConfig(FullMoonDeviceConfig fullMoonConfig) {
        setFullMoonConfig(fullMoonConfig, true);
    }

    public void setFullMoonConfig(FullMoonDeviceConfig fullMoonConfig, boolean customized) {
        this.fullMoonConfig = fullMoonConfig;
        this.fullMoonConfigCustomized = customized;
        if (level != null && level.isClientSide()) {
            HandheldMoonDynamicLightsInitializer.ensureFullMoonBehaviorAt(getBlockPos());
        } else if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }

    public boolean hasCustomFullMoonConfig() {
        return fullMoonConfigCustomized;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (level.isClientSide()) {
            HandheldMoonDynamicLightsInitializer.addFullMoonBehavior(this);
            return;
        }

        BlockPos pos = getBlockPos();
        AABB box = new AABB(
                pos.getX() + 0.5 - 0.25, pos.getY() + 0.5 - 0.25, pos.getZ() + 0.5 - 0.25,
                pos.getX() + 0.5 + 0.25, pos.getY() + 0.5 + 0.25, pos.getZ() + 0.5 + 0.25
        );
        if (level.getEntitiesOfClass(FullMoonEntity.class, box).isEmpty()) {
            FullMoonEntity entity = new FullMoonEntity(level);
            entity.setPos(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
            entity.setAnchor(pos);
            level.addFreshEntity(entity);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        HandheldMoonDynamicLightsInitializer.removeFullMoonBehavior(this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("uuid", uuid.toString());
        output.putBoolean("fullMoonConfigCustomized", fullMoonConfigCustomized);
        if (fullMoonConfigCustomized) {
            output.store("fullMoonConfig", DeviceConfigCodecs.FULL_MOON, fullMoonConfig);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String value = input.getStringOr("uuid", UUID.randomUUID().toString());
        uuid = UUID.fromString(value);
        fullMoonConfigCustomized = input.getBooleanOr("fullMoonConfigCustomized", false);
        fullMoonConfig = input.read("fullMoonConfig", DeviceConfigCodecs.FULL_MOON).orElse(FullMoonDeviceConfig.fromGlobalConfig());
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


