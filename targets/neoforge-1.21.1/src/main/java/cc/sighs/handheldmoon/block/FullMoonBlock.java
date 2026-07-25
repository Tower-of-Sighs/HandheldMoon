package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import com.mojang.serialization.MapCodec;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FullMoonBlock extends BaseEntityBlock {
    public FullMoonBlock() {
        super(BlockBehaviour.Properties.of().noCollission().strength(1f));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(props -> new FullMoonBlock());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide) {
            HandheldMoonDynamicLightsInitializer.ensureFullMoonBehaviorAt(pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level.isClientSide) {
            HandheldMoonDynamicLightsInitializer.removeFullMoonBehaviorAt(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new FullMoonBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var item = new ItemStack(this);
        BlockEntity blockEntity = params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof FullMoonBlockEntity moon && moon.isFullMoonConfigCustomized()) {
            item.set(ModDataComponent.FULL_MOON_CONFIG, moon.getFullMoonConfig());
        }
        return List.of(item);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof FullMoonBlockEntity moon) {
            FullMoonDeviceConfig config = stack.get(ModDataComponent.FULL_MOON_CONFIG);
            moon.setFullMoonConfig(config != null ? config : FullMoonDeviceConfig.fromGlobalConfig(), config != null);
        }
    }
}
