package cc.sighs.handheldmoon.api.light;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable configuration describing how an entity-backed light behaves.
 * Angles are cone half-angles in radians; offsets are expressed in world
 * coordinates relative to the runtime light position.
 *
 * @param shape light geometry
 * @param luminance peak real-light level
 * @param range maximum real-light range in blocks
 * @param innerAngle full-strength cone half-angle in radians
 * @param outerAngle falloff cone half-angle in radians
 * @param realLight whether the profile contributes to world lighting
 * @param visibleCone whether a client-side volumetric cone may be rendered
 * @param occlusion whether world geometry blocks the real light
 * @param positionOffset offset from the runtime light position
 * @param attenuationCurve distance falloff preset
 */
public record EntityLightProfile(
        Shape shape,
        double luminance,
        double range,
        double innerAngle,
        double outerAngle,
        boolean realLight,
        boolean visibleCone,
        boolean occlusion,
        Vec3 positionOffset,
        AttenuationCurve attenuationCurve
) {
    public static final Codec<EntityLightProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(EntityLightProfile::parseShape, Shape::name)
                    .fieldOf("shape").forGetter(EntityLightProfile::shape),
            Codec.DOUBLE.fieldOf("luminance").forGetter(EntityLightProfile::luminance),
            Codec.DOUBLE.fieldOf("range").forGetter(EntityLightProfile::range),
            Codec.DOUBLE.fieldOf("innerAngle").forGetter(EntityLightProfile::innerAngle),
            Codec.DOUBLE.fieldOf("outerAngle").forGetter(EntityLightProfile::outerAngle),
            Codec.BOOL.fieldOf("realLight").forGetter(EntityLightProfile::realLight),
            Codec.BOOL.fieldOf("visibleCone").forGetter(EntityLightProfile::visibleCone),
            Codec.BOOL.fieldOf("occlusion").forGetter(EntityLightProfile::occlusion),
            Codec.DOUBLE.fieldOf("offsetX").forGetter(profile -> profile.positionOffset().x),
            Codec.DOUBLE.fieldOf("offsetY").forGetter(profile -> profile.positionOffset().y),
            Codec.DOUBLE.fieldOf("offsetZ").forGetter(profile -> profile.positionOffset().z),
            Codec.STRING.xmap(AttenuationCurve::parse, AttenuationCurve::name)
                    .optionalFieldOf("attenuationCurve", AttenuationCurve.QUADRATIC)
                    .forGetter(EntityLightProfile::attenuationCurve)
    ).apply(instance, (shape, luminance, range, innerAngle, outerAngle, realLight,
            visibleCone, occlusion, offsetX, offsetY, offsetZ, attenuationCurve) -> new EntityLightProfile(
            shape, luminance, range, innerAngle, outerAngle, realLight, visibleCone,
            occlusion, new Vec3(offsetX, offsetY, offsetZ), attenuationCurve)));

    public EntityLightProfile {
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(positionOffset, "positionOffset");
        attenuationCurve = attenuationCurve == null ? AttenuationCurve.QUADRATIC : attenuationCurve;
        luminance = clamp(luminance, 0.0, 15.0);
        range = clamp(range, 0.0, 64.0);
        innerAngle = clamp(innerAngle, 0.0, Math.PI);
        outerAngle = clamp(outerAngle, innerAngle, Math.PI);
        positionOffset = new Vec3(
                finiteOrZero(positionOffset.x),
                finiteOrZero(positionOffset.y),
                finiteOrZero(positionOffset.z)
        );
    }

    /** Compatibility constructor retaining the original profile shape. */
    public EntityLightProfile(
            Shape shape,
            double luminance,
            double range,
            double innerAngle,
            double outerAngle,
            boolean realLight,
            boolean visibleCone,
            boolean occlusion,
            Vec3 positionOffset
    ) {
        this(shape, luminance, range, innerAngle, outerAngle, realLight,
                visibleCone, occlusion, positionOffset, AttenuationCurve.QUADRATIC);
    }

    public static EntityLightProfile point(
            double luminance, double range, boolean realLight,
            boolean occlusion, Vec3 positionOffset
    ) {
        return point(luminance, range, realLight, occlusion, positionOffset,
                AttenuationCurve.QUADRATIC);
    }

    public static EntityLightProfile point(
            double luminance, double range, boolean realLight,
            boolean occlusion, Vec3 positionOffset, AttenuationCurve attenuationCurve
    ) {
        return new EntityLightProfile(
                Shape.POINT, luminance, range, 0.0, 0.0,
                realLight, false, occlusion, positionOffset, attenuationCurve
        );
    }

    public static EntityLightProfile cone(
            double luminance, double range, double innerAngle, double outerAngle,
            boolean realLight, boolean visibleCone, boolean occlusion, Vec3 positionOffset
    ) {
        return cone(luminance, range, innerAngle, outerAngle, realLight, visibleCone,
                occlusion, positionOffset, AttenuationCurve.QUADRATIC);
    }

    public static EntityLightProfile cone(
            double luminance, double range, double innerAngle, double outerAngle,
            boolean realLight, boolean visibleCone, boolean occlusion, Vec3 positionOffset,
            AttenuationCurve attenuationCurve
    ) {
        return new EntityLightProfile(
                Shape.CONE, luminance, range, innerAngle, outerAngle,
                realLight, visibleCone, occlusion, positionOffset, attenuationCurve
        );
    }

    public CompoundTag toTag() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
                .result()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .orElseGet(CompoundTag::new);
    }

    public static EntityLightProfile fromTag(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }

    /** Compact wire representation for entity data synchronization. */
    public String toNetworkString() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this)
                .result()
                .map(Object::toString)
                .orElse("");
    }

    public static EntityLightProfile fromNetworkString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(value)).result().orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static double clamp(double value, double minimum, double maximum) {
        if (Double.isNaN(value)) {
            return minimum;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return maximum;
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return minimum;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double finiteOrZero(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }

    private static Shape parseShape(String value) {
        return Shape.valueOf(value.toUpperCase(Locale.ROOT));
    }

    public enum Shape {
        POINT,
        CONE
    }
}
