package cc.sighs.handheldmoon.api.light;

import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Per-tick state for an entity-backed light.
 *
 * @param enabled whether the light is currently active
 * @param position world-space origin before profile offset
 * @param direction world-space cone direction (normalised by the constructor)
 */
public record EntityLightRuntimeState(boolean enabled, Vec3 position, Vec3 direction) {
    public EntityLightRuntimeState {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(direction, "direction");
        direction = direction.lengthSqr() > 1.0E-8 ? direction.normalize() : Vec3.ZERO;
    }

    public static EntityLightRuntimeState enabled(Vec3 position, Vec3 direction) {
        return new EntityLightRuntimeState(true, position, direction);
    }

    public static EntityLightRuntimeState disabled(Vec3 position) {
        return new EntityLightRuntimeState(false, position, Vec3.ZERO);
    }
}
