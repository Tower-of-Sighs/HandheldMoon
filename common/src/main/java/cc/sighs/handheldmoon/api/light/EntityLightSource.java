package cc.sighs.handheldmoon.api.light;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;

import java.util.Objects;
import java.util.UUID;

/**
 * A dynamic light owned by an entity.
 *
 * <p>The channel is part of the identity because one entity may expose more
 * than one light (for example, a flashlight cone and a held-item point light).</p>
 */
public interface EntityLightSource {
    String channel();

    UUID entityId();

    DynamicLightBehavior behavior();

    static EntityLightSource of(String channel, UUID entityId, DynamicLightBehavior behavior) {
        return new Default(channel, entityId, behavior);
    }

    record Key(String channel, UUID entityId) {
        public Key {
            Objects.requireNonNull(channel, "channel");
            Objects.requireNonNull(entityId, "entityId");
        }
    }

    record Default(String channel, UUID entityId, DynamicLightBehavior behavior) implements EntityLightSource {
        public Default {
            Objects.requireNonNull(channel, "channel");
            Objects.requireNonNull(entityId, "entityId");
            Objects.requireNonNull(behavior, "behavior");
        }
    }
}
