package cc.sighs.handheldmoon.util;

import net.minecraft.world.level.block.state.BlockBehaviour;

import java.lang.reflect.InvocationTargetException;

/** Bridges the collision-builder rename between supported Minecraft versions. */
public final class BlockPropertiesCompat {
    private BlockPropertiesCompat() {
    }

    public static BlockBehaviour.Properties noCollision() {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
        for (String methodName : new String[]{"noCollision", "noCollission"}) {
            try {
                return (BlockBehaviour.Properties) BlockBehaviour.Properties.class
                        .getMethod(methodName)
                        .invoke(properties);
            } catch (NoSuchMethodException ignored) {
                // Try the spelling used by the other supported Minecraft version.
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new IllegalStateException("Unable to configure non-colliding block", exception);
            }
        }
        throw new IllegalStateException("Minecraft block properties do not expose a collision toggle");
    }
}
