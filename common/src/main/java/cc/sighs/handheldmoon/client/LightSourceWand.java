package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.spi.PlatformServices;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Long-range targeting helper for the moonlight wand. */
public final class LightSourceWand {
    private LightSourceWand() {
    }

    /** Radius around the view ray within which an entity counts as aimed at. */
    private static final double AIM_TOLERANCE = 1.5;

    public static void openTarget(Player player) {
        LightSourceTarget target = findTarget(player);
        if (target != null) {
            PlatformServices.require().client().openLightSourceConfig(
                    target.source(), target.entityId(), target.uuid(), target.sourceId());
        }
    }

    /**
     * Returns the light-source entity closest to the crosshair, ignoring
     * distance and block occlusion. Only entities in the current view and
     * loaded on the client can be selected.
     */
    public static @Nullable LightSourceTarget findTarget(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 dir = player.getViewVector(1.0f).normalize();

        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof FullMoonDynamicLightSource source) || entity.isRemoved()) {
                continue;
            }
            AABB box = entity.getBoundingBox();
            Vec3 center = box.getCenter();
            Vec3 to = center.subtract(eye);
            double along = to.dot(dir);
            if (along <= 0.0) {
                continue;
            }
            double perpendicularSq = to.lengthSqr() - along * along;
            double tolerance = Math.max(0.5, box.getSize() * 0.5) + AIM_TOLERANCE;
            if (perpendicularSq > tolerance * tolerance) {
                continue;
            }
            double score = Math.sqrt(perpendicularSq) + along * 1.0e-4;
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }
        if (best == null) {
            return null;
        }
        FullMoonDynamicLightSource source = (FullMoonDynamicLightSource) best;
        return new LightSourceTarget(source, best.getId(), best.getUUID(), source.getSourceId());
    }
}
