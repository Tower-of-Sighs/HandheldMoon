package cc.sighs.handheldmoon.mixin.embeddium;

import cc.sighs.handheldmoon.dynamiclight.SodiumLightSampling;
import me.jellysquid.mods.sodium.client.model.light.data.LightDataAccess;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keep flashlight energy out of the integer voxel cache shared by both Sodium pipelines. */
@Mixin(value = LightDataAccess.class, remap = false)
public abstract class SodiumLightDataAccessMixin {
    // Embeddium 0.3 bundles MixinExtras 0.3.5, which predates WrapMethod.
    // Its cache obtains dynamic light through this single vanilla call.
    @Redirect(method = "compute", remap = false, at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I"))
    private int handheldmoon$sampleEnvironment(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        boolean previous = SodiumLightSampling.beginBlockSamples();
        try {
            return LevelRenderer.getLightColor(level, state, pos);
        } finally {
            SodiumLightSampling.endBlockSamples(previous);
        }
    }
}
