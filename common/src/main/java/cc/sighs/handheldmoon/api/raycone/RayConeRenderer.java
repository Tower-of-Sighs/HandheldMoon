package cc.sighs.handheldmoon.api.raycone;

import cc.sighs.handheldmoon.api.raycone.impl.RayConeRendererImpl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * Unified entry point for rendering ray cones into the world.
 * <p>
 * Handles model-view stack setup, Iris shader pipeline bypass,
 * multi-layer cone geometry, colour gradients, noise, world-space
 * raycast clipping, and the optional fog layer.
 * <p>
 * Designed to be called once per frame from a level-rendering hook
 * (e.g. Fabric {@code END_MAIN} or NeoForge {@code AfterLevel}).
 *
 * <pre>{@code
 * // Build a config once and reuse it every frame
 * IRayConeConfig cfg = RayConeBuilder.create()
 *     .range(24.0).angle(30.0)
 *     .addColorStop("#FFFF00").addColorStop("#FF4400")
 *     .addLayer(1.0f, 0.12f, 0.02f)
 *     .addLayer(0.7f, 0.06f, 0.01f)
 *     .noiseAmplitude(0.15).raycast(true)
 *     .build();
 *
 * // Each frame, collect live sources and render
 * List<ConeSource> sources = new ArrayList<>();
 * for (Player p : players) {
 *     sources.add(new ConeSource(p.getEyePosition(pt), p.getViewVector(pt), cfg, false));
 * }
 * RayConeRenderer.render(poseStack, cameraPos, modelViewMatrix, sources);
 * }</pre>
 */
public final class RayConeRenderer {
    private RayConeRenderer() {
    }

    /**
     * Render all cone sources into the current frame.
     * <p>
     * This method pushes/pops the model-view stack and handles Iris
     * pipeline bypass internally. Callers do not need to wrap this
     * call with custom Iris compat code.
     *
     * @param poseStack       the current {@link PoseStack}
     * @param cameraPos       camera (viewer) position in world space
     * @param modelViewMatrix the current model-view matrix
     * @param sources         cone sources to render (non-null, may be empty)
     */
    public static void render(
            PoseStack poseStack,
            Vec3 cameraPos,
            Matrix4fc modelViewMatrix,
            List<ConeSource> sources
    ) {
        RayConeRendererImpl.render(poseStack, cameraPos, modelViewMatrix, sources);
    }

    /**
     * A single cone light source to render.
     *
     * @param apex            cone origin in world space
     * @param direction       normalised cone axis direction
     * @param config          cone rendering configuration
     * @param raycastAllLayers when true, raycast clip is applied to every layer
     *                         (not just layer 0). Used for placed lamps where
     *                         the cone origin is inside a block.
     */
    public record ConeSource(Vec3 apex, Vec3 direction, IRayConeConfig config, boolean raycastAllLayers) {
        /**
         * Convenience constructor with raycastAllLayers = false (player-held lights).
         */
        public ConeSource(Vec3 apex, Vec3 direction, IRayConeConfig config) {
            this(apex, direction, config, false);
        }
    }
}
