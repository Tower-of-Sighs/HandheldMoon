package cc.sighs.handheldmoon.api.light;

import net.minecraft.client.Minecraft;

import java.util.Objects;
import java.util.function.Consumer;

/** Discovers entity-backed lights for the current client world. */
@FunctionalInterface
public interface EntityLightSourceProvider {
    void collect(Minecraft minecraft, Consumer<EntityLightSource> sink);

    /** Clears provider-owned caches when the client world changes. */
    default void reset() {
    }

    static EntityLightSourceProvider require(EntityLightSourceProvider provider) {
        return Objects.requireNonNull(provider, "provider");
    }
}
