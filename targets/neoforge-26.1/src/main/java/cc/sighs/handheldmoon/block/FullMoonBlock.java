package cc.sighs.handheldmoon.block;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FullMoonBlock extends BaseEntityBlock {
    public FullMoonBlock() {
        super(Properties.of().noCollision().strength(1f).setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "full_moon"))));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(props -> new FullMoonBlock());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide()) {
            HandheldMoonDynamicLightsInitializer.ensureFullMoonBehaviorAt(pos);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new FullMoonBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof FullMoonBlockEntity moon) {
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
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack item = new ItemStack(this);
        BlockEntity be = params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (be instanceof FullMoonBlockEntity moon) {
            if (moon.hasCustomFullMoonConfig()) {
                item.set(ModDataComponent.FULL_MOON_CONFIG.get(), moon.getFullMoonConfig());
            } else {
                item.remove(ModDataComponent.FULL_MOON_CONFIG.get());
            }
        }
        return List.of(item);
    }
}
