package cc.sighs.handheldmoon.entity;

import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FullMoonEntity extends ThrowableItemProjectile {
    private int radius = 16;
    private BlockPos anchorPos;

    public FullMoonEntity(Level level) {
        this(ModEntities.MOONLIGHT.get(), level);
    }

    public FullMoonEntity(EntityType<? extends FullMoonEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void setAnchor(BlockPos pos) {
        this.anchorPos = pos;
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
    protected Item getDefaultItem() {
        return ModItems.FULL_MOON.get();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
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
        super.addAdditionalSaveData(output);
        output.putInt("radius", radius);
        if (anchorPos != null) {
            output.putInt("ax", anchorPos.getX());
            output.putInt("ay", anchorPos.getY());
            output.putInt("az", anchorPos.getZ());
        }
    }
}
