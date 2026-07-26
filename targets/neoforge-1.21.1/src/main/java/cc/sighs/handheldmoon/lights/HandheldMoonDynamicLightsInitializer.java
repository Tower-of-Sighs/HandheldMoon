package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.handheldmoon.registry.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Keeps the client-side dynamic-light manager synchronized with entity state. */
public final class HandheldMoonDynamicLightsInitializer {
    private static final Map<UUID, FullMoonEntityLightBehavior> FULL_MOON_ENTITY_BEHAVIORS = new HashMap<>();

    private HandheldMoonDynamicLightsInitializer() {
    }

    public static void reset() {
        FULL_MOON_ENTITY_BEHAVIORS.values().forEach(DynamicLightManager::remove);
        FULL_MOON_ENTITY_BEHAVIORS.clear();
        EntityDynamicLightTracker.reset();
    }

    public static void updatePlayerBehaviors() {
        EntityDynamicLightTracker.updatePlayerBehaviors();
    }

    public static void updateFullMoonEntityBehaviors() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!Config.REAL_LIGHT.get()) {
            FULL_MOON_ENTITY_BEHAVIORS.values().forEach(DynamicLightManager::remove);
            FULL_MOON_ENTITY_BEHAVIORS.clear();
            return;
        }

        Set<UUID> seen = new HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof FullMoonEntity fullMoon) || fullMoon.getAnchorPos() == null) continue;
            UUID id = fullMoon.getUUID();
            seen.add(id);
            if (!FULL_MOON_ENTITY_BEHAVIORS.containsKey(id)) {
                FullMoonEntityLightBehavior behavior = new FullMoonEntityLightBehavior(fullMoon);
                FULL_MOON_ENTITY_BEHAVIORS.put(id, behavior);
                DynamicLightManager.add(behavior);
            }
        }
        removeUnseen(FULL_MOON_ENTITY_BEHAVIORS, seen);
    }

    public static void updateItemBehaviors() {
        EntityDynamicLightTracker.updateItemBehaviors();
    }

    private static <T> void removeUnseen(Map<UUID, T> behaviors, Set<UUID> seen) {
        Iterator<Map.Entry<UUID, T>> iterator = behaviors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, T> entry = iterator.next();
            if (seen.contains(entry.getKey())) continue;
            if (entry.getValue() instanceof cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior behavior) {
                DynamicLightManager.remove(behavior);
            }
            iterator.remove();
        }
    }
}
