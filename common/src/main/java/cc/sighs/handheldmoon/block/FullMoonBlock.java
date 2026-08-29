package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.api.content.FullMoonBlockEntityAccess;
import cc.sighs.handheldmoon.util.BlockPropertiesCompat;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FullMoonBlock extends BaseEntityBlock {
    public FullMoonBlock() {
        super(RegistryPropertiesCompat.withId(BlockPropertiesCompat.noCollision().strength(1f), "block", "full_moon"));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(props -> new FullMoonBlock());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return HandheldMoon.registry().createBlockEntity("full_moon", p_153215_, p_153216_);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof FullMoonBlockEntityAccess moon) {
            if (stack.has(ModDataComponent.FULL_MOON_CONFIG.get())) {
                FullMoonDeviceConfig cfg = stack.getOrDefault(ModDataComponent.FULL_MOON_CONFIG.get(), FullMoonDeviceConfig.fromGlobalConfig());
                moon.setFullMoonConfig(cfg, true);
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
                if (lvl.isClientSide()) {
                    moon.clientTick();
                } else {
                    moon.serverTick();
                }
            }
        };
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack item = new ItemStack(this);
        BlockEntity be = params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (be instanceof FullMoonBlockEntityAccess moon) {
            if (moon.hasCustomFullMoonConfig()) {
                item.set(ModDataComponent.FULL_MOON_CONFIG.get(), moon.getFullMoonConfig());
            } else {
                item.remove(ModDataComponent.FULL_MOON_CONFIG.get());
            }
        }
        return List.of(item);
    }
}
