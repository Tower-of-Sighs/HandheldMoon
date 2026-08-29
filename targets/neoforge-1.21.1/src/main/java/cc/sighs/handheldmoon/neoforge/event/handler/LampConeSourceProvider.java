package cc.sighs.handheldmoon.neoforge.event.handler;

import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.neoforge.entity.FullMoonEntity;
import cc.sighs.handheldmoon.event.handler.RayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class LampConeSourceProvider {
    private LampConeSourceProvider() {
    }

    public static void append(Minecraft minecraft, List<RayConeRenderer.ConeSource> sources) {
        if (minecraft.level == null) {
            return;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof FullMoonEntity lampEntity)
                    || !lampEntity.isLampBound()
                    || lampEntity.getLampLuminance() <= 0) {
                continue;
            }
            Vec3 direction = lampEntity.getLampDirection();
            Vec3 apex = lampEntity.position().add(direction.scale(0.24));
            sources.add(new RayConeRenderer.ConeSource(
                    apex,
                    direction,
                    RayEvent.buildLampConeConfig(lampEntity.getLampConfig()),
                    true
            ));
        }
    }
}
