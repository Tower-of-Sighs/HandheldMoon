package cc.sighs.handheldmoon.api.raycone;

import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class LampConeTransformHooks {
    @FunctionalInterface
    public interface Transformer {
        LampCone transform(MoonlightLampBlockEntity lamp, Vec3 apex, Vec3 direction);
    }

    public record LampCone(Vec3 apex, Vec3 direction) {
    }

    private static Transformer transformer = (lamp, apex, direction) -> new LampCone(apex, direction);

    private LampConeTransformHooks() {
    }

    public static void install(Transformer lampTransformer) {
        transformer = Objects.requireNonNull(lampTransformer);
    }

    public static LampCone transform(MoonlightLampBlockEntity lamp, Vec3 apex, Vec3 direction) {
        return transformer.transform(lamp, apex, direction);
    }
}
