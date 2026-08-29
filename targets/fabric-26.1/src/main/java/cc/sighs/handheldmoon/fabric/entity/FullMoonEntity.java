package cc.sighs.handheldmoon.fabric.entity;

import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.fabric.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FullMoonEntity extends Entity implements FullMoonDynamicLightSource {
    private int radius = 16;
    private BlockPos anchorPos;

    public FullMoonEntity(Level level) {
        this((EntityType<? extends FullMoonEntity>) ModEntities.MOONLIGHT.get(), level);
    }

    public FullMoonEntity(EntityType<? extends FullMoonEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void setAnchor(BlockPos pos) {
        this.anchorPos = pos;
    }

    @Override
    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    @Override
    public boolean isLampBound() {
        return false;
    }

    @Override
    public int getLampLuminance() {
        return 15;
    }

    @Override
    public net.minecraft.world.phys.Vec3 getLightPosition() {
        return position();
    }

    @Override
    public net.minecraft.world.phys.Vec3 getLampDirection() {
        return net.minecraft.world.phys.Vec3.ZERO;
    }

    @Override
    public LampDeviceConfig getLampConfig() {
        return LampDeviceConfig.fromGlobalConfig();
    }

    @Override
    public FullMoonDeviceConfig getFullMoonConfig() {
        BlockEntity blockEntity = anchorPos == null ? null : level().getBlockEntity(anchorPos);
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
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            BlockPos checkPos = anchorPos != null ? anchorPos : blockPosition();
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
            anchorPos = new BlockPos(
                    input.getIntOr("ax", 0),
                    input.getIntOr("ay", 0),
                    input.getIntOr("az", 0)
            );
            return;
        }
        anchorPos = null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("radius", radius);
        if (anchorPos != null) {
            output.putInt("ax", anchorPos.getX());
            output.putInt("ay", anchorPos.getY());
            output.putInt("az", anchorPos.getZ());
        }
    }
}


