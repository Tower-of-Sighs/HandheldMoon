package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.compat.FlashlightCompatHooks;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.dynamiclight.EntityItemLightBehavior;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.Utils;
import cc.sighs.handheldmoon.util.ItemState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Shared tracking for player-held and dropped item light sources. */
public final class EntityDynamicLightTracker {
    private static final Map<UUID, PlayerFlashlightLineLightBehavior> PLAYER_BEHAVIORS = new HashMap<>();
    private static final Map<UUID, EntityItemLightBehavior> ITEM_BEHAVIORS = new HashMap<>();

    private EntityDynamicLightTracker() {
    }

    public static void reset() {
        PLAYER_BEHAVIORS.values().forEach(DynamicLightManager::remove);
        ITEM_BEHAVIORS.values().forEach(DynamicLightManager::remove);
        PLAYER_BEHAVIORS.clear();
        ITEM_BEHAVIORS.clear();
    }

    public static void updatePlayerBehaviors() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!Config.REAL_LIGHT.get()) {
            PLAYER_BEHAVIORS.values().forEach(DynamicLightManager::remove);
            PLAYER_BEHAVIORS.clear();
            return;
        }

        Set<UUID> seen = new HashSet<>();
        for (Player player : mc.level.players()) {
            UUID id = player.getUUID();
            seen.add(id);
            PlayerFlashlightLineLightBehavior existing = PLAYER_BEHAVIORS.get(id);
            if (Utils.isUsingFlashlight(player)) {
                if (existing == null) {
                    existing = new PlayerFlashlightLineLightBehavior(player);
                    PLAYER_BEHAVIORS.put(id, existing);
                    DynamicLightManager.add(existing);
                }
            } else if (existing != null) {
                DynamicLightManager.remove(existing);
                PLAYER_BEHAVIORS.remove(id);
            }
        }
        removeUnseen(PLAYER_BEHAVIORS, seen);
    }

    public static void updateItemBehaviors() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Set<UUID> active = new HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player) && !(entity instanceof ItemEntity)) continue;
            int luminance = itemLuminance(entity);
            UUID id = entity.getUUID();
            EntityItemLightBehavior existing = ITEM_BEHAVIORS.get(id);
            if (luminance > 0) {
                active.add(id);
                if (existing == null) {
                    existing = new EntityItemLightBehavior(entity, () -> itemLuminance(entity));
                    ITEM_BEHAVIORS.put(id, existing);
                    DynamicLightManager.add(existing);
                }
            } else if (existing != null) {
                DynamicLightManager.remove(existing);
                ITEM_BEHAVIORS.remove(id);
            }
        }
        removeUnseen(ITEM_BEHAVIORS, active);
    }

    private static int itemLuminance(Entity entity) {
        if (entity instanceof Player player) {
            int held = Math.max(itemLuminance(player.getMainHandItem()), itemLuminance(player.getOffhandItem()));
            return Math.max(held, FlashlightCompatHooks.itemLuminance(player));
        }
        if (entity instanceof ItemEntity itemEntity) {
            return itemLuminance(itemEntity.getItem());
        }
        return 0;
    }

    private static int itemLuminance(ItemStack stack) {
        if (stack.is(ModItems.FULL_MOON.get())) return 15;
        if (stack.is(ModItems.MOONLIGHT_LAMP.get()) && ItemState.powered(stack) == 0) return 15;
        return 0;
    }

    private static <T extends DynamicLightBehavior> void removeUnseen(Map<UUID, T> behaviors, Set<UUID> seen) {
        Iterator<Map.Entry<UUID, T>> iterator = behaviors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, T> entry = iterator.next();
            if (seen.contains(entry.getKey())) continue;
            DynamicLightManager.remove(entry.getValue());
            iterator.remove();
        }
    }
}
