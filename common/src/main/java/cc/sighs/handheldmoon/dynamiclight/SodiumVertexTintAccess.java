package cc.sighs.handheldmoon.dynamiclight;

/** Carries per-vertex tint from Sodium 0.5 lighting to its immutable-quad buffer step. */
public interface SodiumVertexTintAccess {
    void handheldmoon$setTint(int vertex, int tint);
    int handheldmoon$getTint(int vertex);
}
