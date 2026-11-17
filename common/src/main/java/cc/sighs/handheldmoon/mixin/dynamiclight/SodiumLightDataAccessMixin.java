package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.SodiumLightSampling;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import org.spongepowered.asm.mixin.Mixin;

/** Keep flashlight energy out of the integer voxel cache shared by both Sodium pipelines. */
@Mixin(value = LightDataAccess.class, remap = false)
public abstract class SodiumLightDataAccessMixin {
    @WrapMethod(method = "compute", remap = false)
    private int handheldmoon$sampleEnvironment(int x, int y, int z, Operation<Integer> original) {
        boolean previous = SodiumLightSampling.beginBlockSamples();
        try {
            return original.call(x, y, z);
        } finally {
            SodiumLightSampling.endBlockSamples(previous);
        }
    }
}
