package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.content.FullMoonBlockEntityAccess;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.item.ItemData;
import cc.sighs.handheldmoon.util.BlockPropertiesCompat;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FullMoonBlock extends BaseEntityBlock {
    public FullMoonBlock() {
        super(RegistryPropertiesCompat.withId(BlockPropertiesCompat.noCollision().strength(1f), "block", "full_moon"));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return HandheldMoon.registry().createBlockEntity("full_moon", pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof FullMoonBlockEntityAccess moon) {
            if (ItemData.hasFullMoonConfig(stack)) {
                moon.setFullMoonConfig(ItemData.fullMoonConfig(stack), true);
            } else {
                moon.setFullMoonConfig(FullMoonDeviceConfig.fromGlobalConfig(), false);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof FullMoonBlockEntityAccess moon) {
                if (lvl.isClientSide) {
                    moon.clientTick();
                } else {
                    moon.serverTick();
                }
            }
        };
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        Block.popResource(level, pos, createDroppedStack(blockEntity));
    }

    private ItemStack createDroppedStack(@Nullable BlockEntity blockEntity) {
        ItemStack item = new ItemStack(this);
        if (blockEntity instanceof FullMoonBlockEntityAccess moon) {
            if (moon.hasCustomFullMoonConfig()) {
                ItemData.setFullMoonConfig(item, moon.getFullMoonConfig());
            } else {
                ItemData.removeFullMoonConfig(item);
            }
        }
        return item;
    }
}
