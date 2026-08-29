package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.EntityLightSource;
import cc.sighs.handheldmoon.api.light.EntityLightSourceController;
import cc.sighs.handheldmoon.api.light.EntityLightSourceProvider;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.registry.Config;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Shared lifecycle implementation for all entity-backed dynamic lights. */
public final class EntityDynamicLightController implements EntityLightSourceController {
    private final List<EntityLightSourceProvider> providers = new ArrayList<>();
    private final Map<EntityLightSource.Key, DynamicLightBehavior> active = new HashMap<>();

    @Override
    public void register(EntityLightSourceProvider provider) {
        EntityLightSourceProvider.require(provider);
        if (!providers.contains(provider)) {
            providers.add(provider);
        }
    }

    @Override
    public void update(Minecraft minecraft) {
        if (minecraft.level == null || !Config.REAL_LIGHT.get()) {
            clearActive();
            providers.forEach(EntityLightSourceProvider::reset);
            return;
        }

        Set<EntityLightSource.Key> seen = new HashSet<>();
        for (EntityLightSourceProvider provider : providers) {
            provider.collect(minecraft, source -> {
                EntityLightSource.Key key = new EntityLightSource.Key(source.channel(), source.entityId());
                DynamicLightBehavior behavior = source.behavior();
                if (behavior.isRemoved()) {
                    DynamicLightBehavior previous = active.remove(key);
                    if (previous != null) {
                        DynamicLightManager.remove(previous);
                    }
                    return;
                }
                DynamicLightBehavior previous = active.put(key, behavior);
                seen.add(key);
                if (previous == behavior) {
                    return;
                }
                if (previous != null) {
                    DynamicLightManager.remove(previous);
                }
                DynamicLightManager.add(behavior);
            });
        }

        active.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey())) {
                return false;
            }
            DynamicLightManager.remove(entry.getValue());
            return true;
        });
    }

    @Override
    public void reset() {
        clearActive();
        for (EntityLightSourceProvider provider : providers) {
            provider.reset();
        }
    }

    private void clearActive() {
        active.values().forEach(DynamicLightManager::remove);
        active.clear();
    }
}
