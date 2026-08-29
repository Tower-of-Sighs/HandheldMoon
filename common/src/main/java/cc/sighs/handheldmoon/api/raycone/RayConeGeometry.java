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

    /** Allocation-free vertex callback for render backends. */
    @FunctionalInterface
    public interface PrimitiveVertexSink {
        void vertex(double x, double y, double z, float r, float g, float b, float alpha);
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

    /**
     * Emits a layer without allocating a {@link Vec3} or RGB array per vertex.
     * The scratch RGB array is owned by the caller and may be reused between
     * layers on the same render thread.
     */
    public static void emitLayerPrimitive(
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
            float[] colorScratch,
            PrimitiveVertexSink sink
    ) {
        float scaledRange = baseRange * sizeScale;
        float scaledHalfAngle = baseAngleDegrees * sizeScale * 0.5f * Mth.DEG_TO_RAD;
        float scaledRadius = scaledRange * Mth.sin(scaledHalfAngle) / Mth.cos(scaledHalfAngle);

        double apexX = apex.x;
        double apexY = apex.y;
        double apexZ = apex.z;
        double directionX = direction.x;
        double directionY = direction.y;
        double directionZ = direction.z;
        double baseCenterX = apexX + directionX * scaledRange;
        double baseCenterY = apexY + directionY * scaledRange;
        double baseCenterZ = apexZ + directionZ * scaledRange;

        double referenceX;
        double referenceY;
        double referenceZ;
        if (Math.abs(directionY) > 0.99) {
            referenceX = 0.0;
            referenceY = 0.0;
            referenceZ = 1.0;
        } else {
            referenceX = 0.0;
            referenceY = 1.0;
            referenceZ = 0.0;
        }

        double rightX = referenceY * directionZ - referenceZ * directionY;
        double rightY = referenceZ * directionX - referenceX * directionZ;
        double rightZ = referenceX * directionY - referenceY * directionX;
        double rightLength = Math.sqrt(rightX * rightX + rightY * rightY + rightZ * rightZ);
        rightX /= rightLength;
        rightY /= rightLength;
        rightZ /= rightLength;

        double upX = directionY * rightZ - directionZ * rightY;
        double upY = directionZ * rightX - directionX * rightZ;
        double upZ = directionX * rightY - directionY * rightX;
        double upLength = Math.sqrt(upX * upX + upY * upY + upZ * upZ);
        upX /= upLength;
        upY /= upLength;
        upZ /= upLength;

        float centerR;
        float centerG;
        float centerB;
        if (layerColorOverride != null) {
            centerR = layerColorOverride[0];
            centerG = layerColorOverride[1];
            centerB = layerColorOverride[2];
        } else {
            ColorUtils.colorAtInto(colorStops, 0.0f, colorScratch);
            centerR = colorScratch[0];
            centerG = colorScratch[1];
            centerB = colorScratch[2];
        }
        sink.vertex(apexX, apexY, apexZ, centerR, centerG, centerB, clamp01(centerAlpha));

        long seed = Double.doubleToLongBits(apexX)
                ^ Double.doubleToLongBits(apexY)
                ^ Double.doubleToLongBits(apexZ)
                ^ Double.doubleToLongBits(directionX)
                ^ Double.doubleToLongBits(directionY)
                ^ Double.doubleToLongBits(directionZ);

        float baseT = Math.min(1.0f, 0.8f + (sizeScale - 1.0f) * 0.6f);
        for (int i = 0; i <= segments; i++) {
            float theta = Mth.TWO_PI * i / segments;
            double cosTheta = Mth.cos(theta);
            double sinTheta = Mth.sin(theta);
            double x = baseCenterX + rightX * scaledRadius * cosTheta + upX * scaledRadius * sinTheta;
            double y = baseCenterY + rightY * scaledRadius * cosTheta + upY * scaledRadius * sinTheta;
            double z = baseCenterZ + rightZ * scaledRadius * cosTheta + upZ * scaledRadius * sinTheta;

            if (raycast && minecraft.level != null) {
                Vec3 point = new Vec3(x, y, z);
                HitResult hit = minecraft.level.clip(new ClipContext(
                        apex, point, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, CollisionContext.empty()
                ));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    Vec3 clipped = hit.getLocation();
                    x = clipped.x;
                    y = clipped.y;
                    z = clipped.z;
                }
            }

            float thetaNormal = i / (float) segments;
            float n1 = Mth.sin((float) (thetaNormal * 7.23 + seed * 0.001));
            float n2 = Mth.sin((float) (thetaNormal * 13.69 + seed * 0.002));
            float n3 = Mth.sin((float) (thetaNormal * 19.41 + seed * 0.0007));
            if (layerColorOverride != null) {
                centerR = layerColorOverride[0];
                centerG = layerColorOverride[1];
                centerB = layerColorOverride[2];
            } else {
                ColorUtils.colorAtWithNoiseInto(colorStops, baseT, n1, n2, n3,
                        noiseAmplitude, colorScratch);
                centerR = colorScratch[0];
                centerG = colorScratch[1];
                centerB = colorScratch[2];
            }
            float alphaNoise = Mth.sin((float) (thetaNormal * 11.0 + seed * 0.001));
            float localAlpha = clamp01(edgeAlpha) * (0.85f + 0.15f * (alphaNoise * 0.5f + 0.5f));
            sink.vertex(x, y, z, centerR, centerG, centerB, localAlpha);
        }
    }

    /** Emits a layer as independent triangles so multiple layers can share one buffer. */
    public static void emitLayerTriangles(
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
            float[] colorScratch,
            PrimitiveVertexSink sink
    ) {
        float scaledRange = baseRange * sizeScale;
        float scaledHalfAngle = baseAngleDegrees * sizeScale * 0.5f * Mth.DEG_TO_RAD;
        float scaledRadius = scaledRange * Mth.sin(scaledHalfAngle) / Mth.cos(scaledHalfAngle);

        double apexX = apex.x;
        double apexY = apex.y;
        double apexZ = apex.z;
        double directionX = direction.x;
        double directionY = direction.y;
        double directionZ = direction.z;
        double baseCenterX = apexX + directionX * scaledRange;
        double baseCenterY = apexY + directionY * scaledRange;
        double baseCenterZ = apexZ + directionZ * scaledRange;

        double referenceX = 0.0;
        double referenceY = Math.abs(directionY) > 0.99 ? 0.0 : 1.0;
        double referenceZ = Math.abs(directionY) > 0.99 ? 1.0 : 0.0;
        double rightX = referenceY * directionZ - referenceZ * directionY;
        double rightY = referenceZ * directionX - referenceX * directionZ;
        double rightZ = referenceX * directionY - referenceY * directionX;
        double rightLength = Math.sqrt(rightX * rightX + rightY * rightY + rightZ * rightZ);
        rightX /= rightLength;
        rightY /= rightLength;
        rightZ /= rightLength;

        double upX = directionY * rightZ - directionZ * rightY;
        double upY = directionZ * rightX - directionX * rightZ;
        double upZ = directionX * rightY - directionY * rightX;
        double upLength = Math.sqrt(upX * upX + upY * upY + upZ * upZ);
        upX /= upLength;
        upY /= upLength;
        upZ /= upLength;

        float centerR;
        float centerG;
        float centerB;
        if (layerColorOverride != null) {
            centerR = layerColorOverride[0];
            centerG = layerColorOverride[1];
            centerB = layerColorOverride[2];
        } else {
            ColorUtils.colorAtInto(colorStops, 0.0f, colorScratch);
            centerR = colorScratch[0];
            centerG = colorScratch[1];
            centerB = colorScratch[2];
        }

        long seed = Double.doubleToLongBits(apexX)
                ^ Double.doubleToLongBits(apexY)
                ^ Double.doubleToLongBits(apexZ)
                ^ Double.doubleToLongBits(directionX)
                ^ Double.doubleToLongBits(directionY)
                ^ Double.doubleToLongBits(directionZ);
        float baseT = Math.min(1.0f, 0.8f + (sizeScale - 1.0f) * 0.6f);
        float clampedCenterAlpha = clamp01(centerAlpha);
        for (int i = 0; i < segments; i++) {
            sink.vertex(apexX, apexY, apexZ, centerR, centerG, centerB, clampedCenterAlpha);
            emitTriangleRingVertex(minecraft, apex, direction,
                    baseCenterX, baseCenterY, baseCenterZ,
                    rightX, rightY, rightZ, upX, upY, upZ,
                    scaledRadius, i, segments, seed, baseT,
                    colorStops, layerColorOverride, noiseAmplitude,
                    edgeAlpha, raycast, colorScratch, sink);
            emitTriangleRingVertex(minecraft, apex, direction,
                    baseCenterX, baseCenterY, baseCenterZ,
                    rightX, rightY, rightZ, upX, upY, upZ,
                    scaledRadius, i + 1, segments, seed, baseT,
                    colorStops, layerColorOverride, noiseAmplitude,
                    edgeAlpha, raycast, colorScratch, sink);
        }
    }

    private static void emitTriangleRingVertex(
            Minecraft minecraft,
            Vec3 apex,
            Vec3 direction,
            double baseCenterX,
            double baseCenterY,
            double baseCenterZ,
            double rightX,
            double rightY,
            double rightZ,
            double upX,
            double upY,
            double upZ,
            float scaledRadius,
            int index,
            int segments,
            long seed,
            float baseT,
            List<float[]> colorStops,
            float[] layerColorOverride,
            float noiseAmplitude,
            float edgeAlpha,
            boolean raycast,
            float[] colorScratch,
            PrimitiveVertexSink sink
    ) {
        float theta = Mth.TWO_PI * index / segments;
        double cosTheta = Mth.cos(theta);
        double sinTheta = Mth.sin(theta);
        double x = baseCenterX + rightX * scaledRadius * cosTheta + upX * scaledRadius * sinTheta;
        double y = baseCenterY + rightY * scaledRadius * cosTheta + upY * scaledRadius * sinTheta;
        double z = baseCenterZ + rightZ * scaledRadius * cosTheta + upZ * scaledRadius * sinTheta;

        if (raycast && minecraft.level != null) {
            Vec3 point = new Vec3(x, y, z);
            HitResult hit = minecraft.level.clip(new ClipContext(
                    apex, point, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, CollisionContext.empty()
            ));
            if (hit.getType() == HitResult.Type.BLOCK) {
                Vec3 clipped = hit.getLocation();
                x = clipped.x;
                y = clipped.y;
                z = clipped.z;
            }
        }

        float thetaNormal = index / (float) segments;
        float n1 = Mth.sin((float) (thetaNormal * 7.23 + seed * 0.001));
        float n2 = Mth.sin((float) (thetaNormal * 13.69 + seed * 0.002));
        float n3 = Mth.sin((float) (thetaNormal * 19.41 + seed * 0.0007));
        float r;
        float g;
        float b;
        if (layerColorOverride != null) {
            r = layerColorOverride[0];
            g = layerColorOverride[1];
            b = layerColorOverride[2];
        } else {
            ColorUtils.colorAtWithNoiseInto(colorStops, baseT, n1, n2, n3,
                    noiseAmplitude, colorScratch);
            r = colorScratch[0];
            g = colorScratch[1];
            b = colorScratch[2];
        }
        float alphaNoise = Mth.sin((float) (thetaNormal * 11.0 + seed * 0.001));
        float localAlpha = clamp01(edgeAlpha) * (0.85f + 0.15f * (alphaNoise * 0.5f + 0.5f));
        sink.vertex(x, y, z, r, g, b, localAlpha);
    }

    public static float clamp01(float value) {
        return value < 0.0f ? 0.0f : Math.min(value, 1.0f);
    }
}
