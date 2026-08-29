package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.light.EntityLightRuntimeState;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Common collector for visible cones emitted by placed lamp entities. */
public final class PlacedLampConeSources {
    private static final Map<FullMoonDynamicLightSource, CachedConeConfig> CONFIG_CACHE = new WeakHashMap<>();

    private PlacedLampConeSources() {
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
            if (entity instanceof FullMoonDynamicLightSource lamp) {
                EntityLightProfile profile = lamp.getLightProfile();
                EntityLightRuntimeState runtime = lamp.getLightRuntimeState();
                if (!lamp.isLampBound()
                        || profile.shape() != EntityLightProfile.Shape.CONE
                        || !profile.visibleCone()
                        || !runtime.enabled()) {
                    continue;
                }
                Vec3 direction = runtime.direction();
                Vec3 apex = runtime.position().add(profile.positionOffset()).add(direction.scale(0.24));
                IRayConeConfig renderConfig = cachedConfig(lamp, profile);
                sources.add(new RayConeRenderer.ConeSource(
                        apex,
                        direction,
                        renderConfig,
                        true
                ));
            }
        }
    }

    private static IRayConeConfig cachedConfig(
            FullMoonDynamicLightSource lamp, EntityLightProfile profile
    ) {
        // Rebuild only when the synced profile or device configuration values
        // actually change; the resulting render config is immutable.
        CachedConeConfig cached = CONFIG_CACHE.get(lamp);
        var deviceConfig = lamp.getLampConfig();
        if (cached == null || cached.deviceConfig != deviceConfig || !cached.profile.equals(profile)) {
            CachedConeConfig replacement = new CachedConeConfig(
                    deviceConfig, profile, RayEvent.buildLampConeConfig(deviceConfig, profile)
            );
            CONFIG_CACHE.put(lamp, replacement);
            cached = replacement;
        }
        return cached.renderConfig;
    }

    private record CachedConeConfig(
            cc.sighs.handheldmoon.config.LampDeviceConfig deviceConfig,
            EntityLightProfile profile,
            IRayConeConfig renderConfig
    ) {
    }
}
