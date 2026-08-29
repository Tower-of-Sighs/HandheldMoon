package cc.sighs.handheldmoon.api.light;

import net.minecraft.client.Minecraft;

/** Coordinates registration and lifecycle of all entity-backed lights. */
public interface EntityLightSourceController {
    void register(EntityLightSourceProvider provider);

    void update(Minecraft minecraft);

    void reset();
}
