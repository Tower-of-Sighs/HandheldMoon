package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.item.ItemData;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.BlockPropertiesCompat;
import cc.sighs.handheldmoon.util.ItemState;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class MoonlightLampBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public MoonlightLampBlock() {
        super(RegistryPropertiesCompat.withId(BlockPropertiesCompat.noCollision().strength(1f), "block", "moonlight_lamp"));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.WEST));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Rendered by block entity renderer to allow free x/y rotation without blockstate model artifacts.
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        var face = blockPlaceContext.getNearestLookingDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, face);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return HandheldMoon.registry().createBlockEntity("moonlight_lamp", pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        var be = level.getBlockEntity(pos);
        if (be instanceof MoonlightLampBlockEntityAccess lamp) {
            lamp.setSourceStack(stack.copy());
            lamp.setPowered(ItemState.powered(stack) == 1);
            if (ItemData.hasLampConfig(stack)) {
                lamp.setLampConfig(ItemData.lampConfig(stack), true);
            } else {
                lamp.setLampConfig(LampDeviceConfig.fromGlobalConfig(), false);
            }
            Direction dir = state.getValue(FACING);
            float yaw;
            float xRot = switch (dir) {
                case NORTH -> {
                    yaw = 180.0f;
                    yield 90.0f;
                }
                case SOUTH -> {
                    yaw = 0.0f;
                    yield 90.0f;
                }
                case WEST -> {
                    yaw = -90.0f;
                    yield 90.0f;
                }
                case EAST -> {
                    yaw = 90.0f;
                    yield 90.0f;
                }
                case UP -> {
                    yaw = placer.getYRot();
                    yield 180.0f;
                }
                case DOWN -> {
                    yaw = placer.getYRot();
                    yield 0.0f;
                }
            };
            lamp.setYRot(yaw);
            lamp.setXRot(xRot);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        Block.popResource(level, pos, createDroppedStack(blockEntity));
    }

    private ItemStack createDroppedStack(@Nullable BlockEntity blockEntity) {
        ItemStack stack = new ItemStack(ModItems.MOONLIGHT_LAMP.get());

        if (blockEntity instanceof MoonlightLampBlockEntityAccess lamp) {
            ItemStack source = lamp.getSourceStack();
            if (!source.isEmpty() && source.is(ModItems.MOONLIGHT_LAMP.get())) {
                stack = source.copy();
            }
            ItemData.setPowered(stack, lamp.getPowered() ? 1 : 0);
            if (lamp.hasCustomLampConfig()) {
                ItemData.setLampConfig(stack, lamp.getLampConfig());
            } else {
                ItemData.removeLampConfig(stack);
            }
        }

        return stack;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return (lvl, pos, state, blockEntity) -> {
            if (blockEntity instanceof MoonlightLampBlockEntityAccess lamp) {
                if (lvl.isClientSide) {
                    lamp.clientTick();
                } else {
                    lamp.serverTick();
                }
            }
        };
    }
}
