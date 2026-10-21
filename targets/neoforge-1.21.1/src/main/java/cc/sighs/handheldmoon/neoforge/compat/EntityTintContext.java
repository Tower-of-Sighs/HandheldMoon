package cc.sighs.handheldmoon.neoforge.compat;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightEntityTint;
import net.minecraft.world.entity.Entity;

/** Thread-local entity currently being rendered by a living renderer. */
public final class EntityTintContext {
    private static final ThreadLocal<Entity> CURRENT = new ThreadLocal<>();

    private EntityTintContext() {
    }

    public static void begin(Entity entity) {
        CURRENT.set(entity);
    }

    public static void end() {
        CURRENT.remove();
    }

    public static int coefficient() {
        Entity entity = CURRENT.get();
        return entity == null ? -1 : DynamicLightEntityTint.entityTint(entity);
    }
}
