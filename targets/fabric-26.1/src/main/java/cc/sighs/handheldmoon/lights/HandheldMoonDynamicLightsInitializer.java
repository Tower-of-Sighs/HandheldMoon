package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class HandheldMoonDynamicLightsInitializer {
    private static final Map<BlockPos, MoonLampLineLightBehavior> LAMP_BEHAVIORS = new HashMap<>();
    private static final Set<BlockPos> POWERED_LAMP_POSITIONS = new HashSet<>();

    public static Set<BlockPos> getActiveLampPositions() {
        return new HashSet<>(POWERED_LAMP_POSITIONS);
    }

    private static final Map<BlockPos, FullMoonBlockBehavior> FULL_MOON_BEHAVIORS = new HashMap<>();

    private HandheldMoonDynamicLightsInitializer() {
    }

    public static void reset() {
        LAMP_BEHAVIORS.clear();
        POWERED_LAMP_POSITIONS.clear();
        EntityDynamicLightTracker.reset();
        FULL_MOON_BEHAVIORS.clear();
    }

    public static void syncLampBehavior(MoonlightLampBlockEntity lamp) {
        var pos = lamp.getBlockPos();
        if (lamp.getPowered()) {
            POWERED_LAMP_POSITIONS.add(pos);
        } else {
            POWERED_LAMP_POSITIONS.remove(pos);
        }

        var existing = LAMP_BEHAVIORS.get(pos);
        if (lamp.getPowered() && lamp.getLampConfig().realLight()) {
            if (existing == null) {
                MoonLampLineLightBehavior behavior = new MoonLampLineLightBehavior(pos);
                LAMP_BEHAVIORS.put(pos, behavior);
                DynamicLightManager.add(behavior);
                behavior.hasChanged();
            }
            return;
        }
        if (existing != null) {
            DynamicLightManager.remove(existing);
            LAMP_BEHAVIORS.remove(pos);
        }
    }

    public static void removeLampBehaviorAt(BlockPos pos) {
        POWERED_LAMP_POSITIONS.remove(pos);
        var existing = LAMP_BEHAVIORS.remove(pos);
        if (existing != null) {
            DynamicLightManager.remove(existing);
        }
    }

    public static void updatePlayerBehaviors() {
        EntityDynamicLightTracker.updatePlayerBehaviors();
    }

    public static void updateItemBehaviors() {
        EntityDynamicLightTracker.updateItemBehaviors();
    }

    public static void addFullMoonBehavior(FullMoonBlockEntity moon) {
        var pos = moon.getBlockPos();
        var existing = FULL_MOON_BEHAVIORS.get(pos);

        if (existing == null) {
            FullMoonBlockBehavior b = new FullMoonBlockBehavior(pos);
            FULL_MOON_BEHAVIORS.put(pos, b);
            DynamicLightManager.add(b);
        }
    }

    public static void ensureFullMoonBehaviorAt(BlockPos pos) {
        var existing = FULL_MOON_BEHAVIORS.get(pos);
        if (existing == null) {
            FullMoonBlockBehavior b = new FullMoonBlockBehavior(pos);
            FULL_MOON_BEHAVIORS.put(pos, b);
            DynamicLightManager.add(b);
        }
    }

    public static void removeFullMoonBehavior(FullMoonBlockEntity moon) {
        var pos = moon.getBlockPos();
        var existing = FULL_MOON_BEHAVIORS.get(pos);

        if (existing != null) {
            DynamicLightManager.remove(existing);
            FULL_MOON_BEHAVIORS.remove(pos);
        }
    }

    public static void removeFullMoonBehaviorAt(BlockPos pos) {
        var existing = FULL_MOON_BEHAVIORS.get(pos);
        if (existing != null) {
            DynamicLightManager.remove(existing);
            FULL_MOON_BEHAVIORS.remove(pos);
        }
    }
}
