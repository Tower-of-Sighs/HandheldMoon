package cc.sighs.handheldmoon.api.raycone;

import cc.sighs.handheldmoon.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

/** Version-independent cone geometry and colour sampling. */
public final class RayConeGeometry {
    @FunctionalInterface
    public interface VertexSink {
        void vertex(Vec3 position, float[] rgb, float alpha);
    }

    private RayConeGeometry() {
    }

    public static void emitLayer(
            Minecraft minecraft,
            Vec3 apex,
            Vec3 direction,
            float baseRange,
            float baseAngleDegrees,
            List<float[]> colorStops,
            float sizeScale,
            float centerAlpha,
            float edgeAlpha,
            float[] layerColorOverride,
            float noiseAmplitude,
            boolean raycast,
            int segments,
            VertexSink sink
    ) {
        float scaledRange = baseRange * sizeScale;
        float scaledHalfAngle = baseAngleDegrees * sizeScale * 0.5f * Mth.DEG_TO_RAD;
        float scaledRadius = scaledRange * Mth.sin(scaledHalfAngle) / Mth.cos(scaledHalfAngle);
        Vec3 baseCenter = apex.add(direction.scale(scaledRange));
        Vec3 upReference = Math.abs(direction.y) > 0.99 ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 right = upReference.cross(direction).normalize();
        Vec3 up = direction.cross(right).normalize();

        float[] centerColor = layerColorOverride != null
                ? layerColorOverride
                : ColorUtils.colorAt(colorStops, 0.0f);
        sink.vertex(apex, centerColor, clamp01(centerAlpha));

        long seed = Double.doubleToLongBits(apex.x)
                ^ Double.doubleToLongBits(apex.y)
                ^ Double.doubleToLongBits(apex.z)
                ^ Double.doubleToLongBits(direction.x)
                ^ Double.doubleToLongBits(direction.y)
                ^ Double.doubleToLongBits(direction.z);

        for (int i = 0; i <= segments; i++) {
            float theta = Mth.TWO_PI * i / segments;
            Vec3 point = baseCenter
                    .add(right.scale(scaledRadius * Mth.cos(theta)))
                    .add(up.scale(scaledRadius * Mth.sin(theta)));
            if (raycast && minecraft.level != null) {
                HitResult hit = minecraft.level.clip(new ClipContext(
                        apex, point, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, CollisionContext.empty()
                ));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    point = hit.getLocation();
                }
            }

            float thetaNormal = i / (float) segments;
            float baseT = Math.min(1.0f, 0.8f + (sizeScale - 1.0f) * 0.6f);
            float n1 = Mth.sin((float) (thetaNormal * 7.23 + seed * 0.001));
            float n2 = Mth.sin((float) (thetaNormal * 13.69 + seed * 0.002));
            float n3 = Mth.sin((float) (thetaNormal * 19.41 + seed * 0.0007));
            float[] edgeColor = ColorUtils.colorAtWithNoise(colorStops, baseT, n1, n2, n3, noiseAmplitude);
            float alphaNoise = Mth.sin((float) (thetaNormal * 11.0 + seed * 0.001));
            float localAlpha = clamp01(edgeAlpha) * (0.85f + 0.15f * (alphaNoise * 0.5f + 0.5f));
            sink.vertex(point, edgeColor, localAlpha);
        }
    }

    public static float clamp01(float value) {
        return value < 0.0f ? 0.0f : Math.min(value, 1.0f);
    }
}
