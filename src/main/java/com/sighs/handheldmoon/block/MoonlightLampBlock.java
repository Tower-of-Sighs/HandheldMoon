package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.Item.MoonlightLampItem;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.registry.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class MoonlightLampBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public MoonlightLampBlock() {
        super(BlockBehaviour.Properties.of().noCollission().strength(1f));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.WEST));
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_49727_) {
        return InteractionResult.FAIL;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            FullMoonEntity entity = new FullMoonEntity(level);
            entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            level.addFreshEntity(entity);
            entity.setInvisible(true);

            if (level.getBlockEntity(pos) instanceof MoonlightLampBlockEntity be) {
                be.setUuid(entity.getUUID());
                be.setChanged();
            }
        }
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MoonlightLampBlockEntity be) {
            UUID entityId = be.getUuid();
            if (entityId != null) {
                Entity entity = ((ServerLevel) level).getEntity(entityId);
                if (entity != null) entity.discard();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new MoonlightLampBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var item = new ItemStack(this);
        if (item.is(Items.MOONLIGHT_LAMP.get())) {
            MoonlightLampItem.togglePowered(item);
        }
        return List.of(item);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        var be = level.getBlockEntity(pos);
        if (be instanceof MoonlightLampBlockEntity lamp) {
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
}
