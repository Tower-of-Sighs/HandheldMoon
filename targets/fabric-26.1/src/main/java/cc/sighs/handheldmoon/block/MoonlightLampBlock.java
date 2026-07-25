package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import com.mojang.serialization.MapCodec;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoonlightLampBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public MoonlightLampBlock() {
        super(Properties.of().noCollision().strength(1f).setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "moonlight_lamp"))));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.WEST));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(props -> new MoonlightLampBlock());
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
        return new MoonlightLampBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        var be = level.getBlockEntity(pos);
        if (be instanceof MoonlightLampBlockEntity lamp) {
            lamp.setPowered(MoonlightLampItem.getPowered(stack) == 1);
            if (stack.has(ModDataComponent.LAMP_CONFIG.get())) {
                LampDeviceConfig cfg = stack.getOrDefault(ModDataComponent.LAMP_CONFIG.get(), LampDeviceConfig.fromGlobalConfig());
                lamp.setLampConfig(cfg, true);
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
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(ModItems.MOONLIGHT_LAMP.get());

        BlockEntity be = params.getParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MoonlightLampBlockEntity lamp) {
            int poweredInt = lamp.getPowered() ? 1 : 0;
            stack.set(ModDataComponent.POWERED.get(), poweredInt);
            if (lamp.hasCustomLampConfig()) {
                stack.set(ModDataComponent.LAMP_CONFIG.get(), lamp.getLampConfig());
            } else {
                stack.remove(ModDataComponent.LAMP_CONFIG.get());
            }
        }

        return List.of(stack);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof MoonlightLampBlockEntity acEntity) {
                    acEntity.clientTick();
                }
            };
        }
        return null;
    }
}
