package cc.sighs.handheldmoon.mixin.embeddium;

import cc.sighs.handheldmoon.dynamiclight.SodiumVertexTintAccess;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(value = QuadLightData.class, remap = false)
public abstract class SodiumQuadLightDataMixin implements SodiumVertexTintAccess {
    @Unique private final int[] handheldmoon$tints = new int[4];

    // Forge's Mixin 0.8.5 rejects IASTORE in copied field initializers.
    @Inject(method = "<init>", at = @At("RETURN"))
    private void handheldmoon$initializeTints(CallbackInfo ci) {
        Arrays.fill(handheldmoon$tints, -1);
    }
    @Override public void handheldmoon$setTint(int vertex, int tint) { handheldmoon$tints[vertex] = tint; }
    @Override public int handheldmoon$getTint(int vertex) { return handheldmoon$tints[vertex]; }
}
