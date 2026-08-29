package cc.sighs.handheldmoon.api.raycone;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class LampConeTransformHooks {
    @FunctionalInterface
    public interface Transformer {
        LampCone transform(MoonlightLampBlockEntityAccess lamp, Vec3 apex, Vec3 direction);
    }

    public record LampCone(Vec3 apex, Vec3 direction) {
    }

    private static Transformer transformer = (lamp, apex, direction) -> new LampCone(apex, direction);

    private LampConeTransformHooks() {
    }

    public static void install(Transformer lampTransformer) {
        transformer = Objects.requireNonNull(lampTransformer);
    }

    public static LampCone transform(MoonlightLampBlockEntityAccess lamp, Vec3 apex, Vec3 direction) {
        return transformer.transform(lamp, apex, direction);
    }
}
