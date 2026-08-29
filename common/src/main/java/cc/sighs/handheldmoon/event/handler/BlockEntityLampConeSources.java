package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;

import java.util.List;

/** Common collector for entity-backed lamp cones. */
public final class BlockEntityLampConeSources {
    private BlockEntityLampConeSources() {
    }

    public static void append(
            Minecraft minecraft,
            Iterable<?> ignored,
            List<RayConeRenderer.ConeSource> sources
    ) {
        if (minecraft.level == null) {
            return;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof FullMoonDynamicLightSource lamp
                    && lamp.isLampBound() && lamp.getLampLuminance() > 0) {
                Vec3 direction = lamp.getLampDirection();
                Vec3 apex = lamp.getLightPosition().add(direction.scale(0.24));
                sources.add(new RayConeRenderer.ConeSource(
                        apex,
                        direction,
                        RayEvent.buildLampConeConfig(lamp.getLampConfig()),
                        true
                ));
            }
        }
    }
}
