package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.EntityLightSource;
import cc.sighs.handheldmoon.api.light.EntityLightSourceProvider;
import cc.sighs.handheldmoon.compat.FlashlightCompatHooks;
import cc.sighs.handheldmoon.dynamiclight.EntityItemLightBehavior;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.ItemState;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** Discovers player-held and dropped-item entity light sources. */
public final class EntityDynamicLightTracker implements EntityLightSourceProvider {
    private static final String PLAYER_FLASHLIGHT_CHANNEL = "player-flashlight";
    private static final String ENTITY_ITEM_CHANNEL = "entity-item";

    private final Map<UUID, PlayerFlashlightLineLightBehavior> playerBehaviors = new HashMap<>();
    private final Map<UUID, EntityItemLightBehavior> itemBehaviors = new HashMap<>();

    @Override
    public void collect(Minecraft minecraft, Consumer<EntityLightSource> sink) {
        if (minecraft.level == null || !Config.REAL_LIGHT.get()) {
            playerBehaviors.clear();
            itemBehaviors.clear();
            return;
        }

        Set<UUID> activePlayers = new HashSet<>();
        for (Player player : minecraft.level.players()) {
            UUID id = player.getUUID();
            if (Utils.isUsingFlashlight(player)) {
                activePlayers.add(id);
                PlayerFlashlightLineLightBehavior behavior = playerBehaviors.computeIfAbsent(
                        id, ignored -> new PlayerFlashlightLineLightBehavior(player));
                sink.accept(EntityLightSource.of(PLAYER_FLASHLIGHT_CHANNEL, id, behavior));
            }
        }
        playerBehaviors.keySet().removeIf(id -> !activePlayers.contains(id));

        Set<UUID> seenItems = new HashSet<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof Player) && !(entity instanceof ItemEntity)) {
                continue;
            }
            int luminance = itemLuminance(entity);
            UUID id = entity.getUUID();
            if (luminance > 0) {
                seenItems.add(id);
                EntityItemLightBehavior behavior = itemBehaviors.computeIfAbsent(
                        id, ignored -> new EntityItemLightBehavior(entity, () -> itemLuminance(entity)));
                sink.accept(EntityLightSource.of(ENTITY_ITEM_CHANNEL, id, behavior));
            }
        }
        itemBehaviors.keySet().removeIf(id -> !seenItems.contains(id));
    }

    @Override
    public void reset() {
        playerBehaviors.clear();
        itemBehaviors.clear();
    }

    private static int itemLuminance(Entity entity) {
        if (entity instanceof Player player) {
            // A held moonlight lamp is represented by the flashlight cone when powered.
            // It must not become an omnidirectional player light while switched off.
            int held = Math.max(
                    heldItemLuminance(player.getMainHandItem()),
                    heldItemLuminance(player.getOffhandItem())
            );
            return Math.max(held, FlashlightCompatHooks.itemLuminance(player));
        }
        if (entity instanceof ItemEntity itemEntity) {
            return itemLuminance(itemEntity.getItem());
        }
        return 0;
    }

    private static int heldItemLuminance(ItemStack stack) {
        return stack.is(ModItems.FULL_MOON.get()) ? 15 : 0;
    }

    private static int itemLuminance(ItemStack stack) {
        if (stack.is(ModItems.FULL_MOON.get())) return 15;
        if (stack.is(ModItems.MOONLIGHT_LAMP.get()) && ItemState.powered(stack) == 0) return 15;
        return 0;
    }
}
