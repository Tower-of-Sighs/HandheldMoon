package cc.sighs.handheldmoon.mixin.embeddium;

import cc.sighs.handheldmoon.dynamiclight.SodiumLightSampling;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.jellysquid.mods.sodium.client.model.light.data.LightDataAccess;
import org.spongepowered.asm.mixin.Mixin;

/** Keep flashlight energy out of the integer voxel cache shared by both Sodium pipelines. */
@Mixin(value = LightDataAccess.class, remap = false)
public abstract class SodiumLightDataAccessMixin {
    // Sodium 0.5 samples world brightness directly, unlike Embeddium's vanilla
    // light-color call. Fabric Loader 0.16.14 supplies MixinExtras 0.4.1.
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
