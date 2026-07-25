package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class FullMoonBlockEntity extends BlockEntity {
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
        this.uuid = UUID.randomUUID();
    }

    public FullMoonDeviceConfig getFullMoonConfig() {
        return fullMoonConfigCustomized ? fullMoonConfig : FullMoonDeviceConfig.fromGlobalConfig();
    }

    public boolean isFullMoonConfigCustomized() {
        return fullMoonConfigCustomized;
    }

    public void setFullMoonConfig(FullMoonDeviceConfig config, boolean customized) {
        this.fullMoonConfig = config;
        this.fullMoonConfigCustomized = customized;
        setChanged();
        if (level != null) {
            if (level.isClientSide) {
                HandheldMoonDynamicLightsInitializer.addFullMoonBehavior(this);
            } else {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (level.isClientSide) {
            HandheldMoonDynamicLightsInitializer.addFullMoonBehavior(this);
        }

        if (!level.isClientSide) {
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
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        HandheldMoonDynamicLightsInitializer.removeFullMoonBehavior(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putUUID("uuid", uuid);
        tag.putBoolean("fullMoonConfigCustomized", fullMoonConfigCustomized);
        DeviceConfigCodecs.FULL_MOON.encodeStart(NbtOps.INSTANCE, fullMoonConfig)
                .result()
                .ifPresent(value -> tag.put("fullMoonConfig", value));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        uuid = tag.getUUID("uuid");
        fullMoonConfigCustomized = tag.getBoolean("fullMoonConfigCustomized");
        if (tag.contains("fullMoonConfig")) {
            DeviceConfigCodecs.FULL_MOON.parse(NbtOps.INSTANCE, tag.get("fullMoonConfig"))
                    .result()
                    .ifPresent(value -> fullMoonConfig = value);
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
}
