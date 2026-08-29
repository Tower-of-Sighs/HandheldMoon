package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.EntityLightSource;
import cc.sighs.handheldmoon.api.light.EntityLightSourceProvider;
import cc.sighs.handheldmoon.registry.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** Discovers full-moon and placed-lamp entities exposed by the active loader. */
public final class FullMoonEntityDynamicLightTracker implements EntityLightSourceProvider {
    private static final String CHANNEL = "full-moon-entity";
    private final Map<UUID, FullMoonEntityLightBehavior> behaviors = new HashMap<>();

    @Override
    public void collect(Minecraft minecraft, Consumer<EntityLightSource> sink) {
        if (minecraft.level == null || !Config.REAL_LIGHT.get()) {
            behaviors.clear();
            return;
        }

        Set<UUID> seen = new HashSet<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof FullMoonDynamicLightSource source)
                    || !source.usesEntityBackedLight()
                    || source.getAnchorPos() == null
                    || (source.isLampBound() && source.getLampLuminance() <= 0)) {
                continue;
            }
            UUID id = entity.getUUID();
            seen.add(id);
            FullMoonEntityLightBehavior behavior = behaviors.computeIfAbsent(
                    id, ignored -> new FullMoonEntityLightBehavior(source));
            sink.accept(EntityLightSource.of(CHANNEL, id, behavior));
        }
        behaviors.keySet().removeIf(id -> !seen.contains(id));
    }

    @Override
    public void reset() {
        behaviors.clear();
    }
}
