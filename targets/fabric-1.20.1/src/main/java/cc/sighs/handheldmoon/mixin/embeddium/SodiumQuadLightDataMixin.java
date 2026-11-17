package cc.sighs.handheldmoon.mixin.embeddium;

import cc.sighs.handheldmoon.dynamiclight.SodiumVertexTintAccess;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = QuadLightData.class, remap = false)
public abstract class SodiumQuadLightDataMixin implements SodiumVertexTintAccess {
    @Unique private final int[] handheldmoon$tints = {-1, -1, -1, -1};
    @Override public void handheldmoon$setTint(int vertex, int tint) { handheldmoon$tints[vertex] = tint; }
    @Override public int handheldmoon$getTint(int vertex) { return handheldmoon$tints[vertex]; }
}
