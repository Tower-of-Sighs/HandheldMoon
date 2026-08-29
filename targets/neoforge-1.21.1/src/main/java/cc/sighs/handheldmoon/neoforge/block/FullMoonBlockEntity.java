package cc.sighs.handheldmoon.neoforge.block;

import cc.sighs.handheldmoon.neoforge.entity.FullMoonEntity;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
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

public class FullMoonBlockEntity extends BlockEntity implements cc.sighs.handheldmoon.api.content.FullMoonBlockEntityAccess {
    private UUID uuid;
    private FullMoonDeviceConfig fullMoonConfig = FullMoonDeviceConfig.fromGlobalConfig();
    private boolean fullMoonConfigCustomized;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public FullMoonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FULL_MOON.get(), pos, state);
    }

    public FullMoonDeviceConfig getFullMoonConfig() {
        return fullMoonConfigCustomized ? fullMoonConfig : FullMoonDeviceConfig.fromGlobalConfig();
    }

    public boolean isFullMoonConfigCustomized() {
        return fullMoonConfigCustomized;
    }

    public boolean hasCustomFullMoonConfig() {
        return isFullMoonConfigCustomized();
    }

    public void setFullMoonConfig(FullMoonDeviceConfig config, boolean customized) {
        this.fullMoonConfig = config;
        this.fullMoonConfigCustomized = customized;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (!level.isClientSide) {
            ensureBoundEntity();
        }
    }

    public void serverTick() {
        FullMoonEntity entity = ensureBoundEntity();
        if (entity != null) {
            entity.setAnchor(getBlockPos());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (uuid != null) {
            tag.putUUID("uuid", uuid);
        }
        tag.putBoolean("fullMoonConfigCustomized", fullMoonConfigCustomized);
        DeviceConfigCodecs.FULL_MOON.encodeStart(NbtOps.INSTANCE, fullMoonConfig)
                .result()
                .ifPresent(value -> tag.put("fullMoonConfig", value));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        uuid = tag.hasUUID("uuid") ? tag.getUUID("uuid") : null;
        fullMoonConfigCustomized = tag.getBoolean("fullMoonConfigCustomized");
        if (tag.contains("fullMoonConfig")) {
            DeviceConfigCodecs.FULL_MOON.parse(NbtOps.INSTANCE, tag.get("fullMoonConfig"))
                    .result()
                    .ifPresent(value -> fullMoonConfig = value);
        }
    }

    @Override
    public void clientTick() {
    }

    @Override
    public void setRemoved() {
        FullMoonEntity entity = getBoundEntity();
        super.setRemoved();
        if (entity != null) entity.discard();
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

    private FullMoonEntity ensureBoundEntity() {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        if (uuid != null && serverLevel.getEntity(uuid) instanceof FullMoonEntity entity && !entity.isLampBound()) {
            return entity;
        }

        FullMoonEntity nearby = findNearbyBoundEntity();
        if (nearby != null) {
            uuid = nearby.getUUID();
            setChanged();
            return nearby;
        }

        FullMoonEntity entity = new FullMoonEntity(serverLevel);
        entity.setAnchor(getBlockPos());
        serverLevel.addFreshEntity(entity);
        uuid = entity.getUUID();
        setChanged();
        return entity;
    }

    private FullMoonEntity findNearbyBoundEntity() {
        if (level == null) return null;
        BlockPos pos = getBlockPos();
        AABB box = new AABB(
                pos.getX() + 0.5 - 0.25, pos.getY() + 0.4 - 0.25, pos.getZ() + 0.5 - 0.25,
                pos.getX() + 0.5 + 0.25, pos.getY() + 0.4 + 0.25, pos.getZ() + 0.5 + 0.25
        );
        for (FullMoonEntity entity : level.getEntitiesOfClass(FullMoonEntity.class, box)) {
            if (!entity.isLampBound() && pos.equals(entity.getAnchorPos())) {
                return entity;
            }
        }
        return null;
    }

    private FullMoonEntity getBoundEntity() {
        if (!(level instanceof ServerLevel serverLevel) || uuid == null) return null;
        return serverLevel.getEntity(uuid) instanceof FullMoonEntity entity && !entity.isLampBound()
                ? entity : null;
    }
}
