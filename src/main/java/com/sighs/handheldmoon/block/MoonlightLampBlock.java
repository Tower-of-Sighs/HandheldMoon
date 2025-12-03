package com.sighs.handheldmoon.block;

import com.sighs.handheldmoon.item.MoonlightLampItem;
import com.sighs.handheldmoon.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoonlightLampBlock extends BaseEntityBlock {

    public MoonlightLampBlock(Properties properties) {
        super(properties.strength(1f));
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.FAIL;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoonlightLampBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        var be = level.getBlockEntity(pos);
        if (be instanceof MoonlightLampBlockEntity lamp) {
            lamp.setPowered(MoonlightLampItem.getPowered(stack) == 1);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(ModItems.MOONLIGHT_LAMP);
        BlockEntity be = params.getParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MoonlightLampBlockEntity lamp) {
            stack.getOrCreateTag().putBoolean("powered", lamp.getPowered());
        }
        return List.of(stack);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof MoonlightLampBlockEntity acEntity) {
                    acEntity.clientTick();
                }
            };
        }
        return null;
    }
}