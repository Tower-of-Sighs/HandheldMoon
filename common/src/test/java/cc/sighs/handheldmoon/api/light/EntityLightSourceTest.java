package cc.sighs.handheldmoon.api.light;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityLightSourceTest {
    @Test
    void keepsIndependentChannelsForOneEntity() {
        UUID entityId = UUID.randomUUID();
        DynamicLightBehavior behavior = new EmptyBehavior();

        EntityLightSource flashlight = EntityLightSource.of("flashlight", entityId, behavior);
        EntityLightSource heldItem = EntityLightSource.of("held-item", entityId, behavior);

        assertNotEquals(flashlight.channel(), heldItem.channel());
        assertEquals(new EntityLightSource.Key("flashlight", entityId),
                new EntityLightSource.Key(flashlight.channel(), flashlight.entityId()));
    }

    @Test
    void rejectsIncompleteSourceIdentity() {
        UUID entityId = UUID.randomUUID();
        DynamicLightBehavior behavior = new EmptyBehavior();

        assertThrows(NullPointerException.class, () -> EntityLightSource.of(null, entityId, behavior));
        assertThrows(NullPointerException.class, () -> EntityLightSource.of("flashlight", null, behavior));
        assertThrows(NullPointerException.class, () -> EntityLightSource.of("flashlight", entityId, null));
    }

    private static final class EmptyBehavior implements DynamicLightBehavior {
        @Override
        public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
            return 0;
        }

        @Override
        public Bounds getBounds() {
            return Bounds.empty();
        }

        @Override
        public boolean hasChanged() {
            return false;
        }

        @Override
        public boolean isRemoved() {
            return false;
        }
    }
}
