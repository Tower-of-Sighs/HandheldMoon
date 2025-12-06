package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class HandheldMoonDynamicLightsInitializer implements DynamicLightsInitializer {
    private static DynamicLightBehaviorManager MANAGER;
    private static final Map<BlockPos, MoonLampLineLightBehavior> LAMP_BEHAVIORS = new HashMap<>();
    private static final Map<UUID, PlayerFlashlightLineLightBehavior> PLAYER_BEHAVIORS = new HashMap<>();

    public static Set<BlockPos> getActiveLampPositions() {
        return new HashSet<>(LAMP_BEHAVIORS.keySet());
    }

    private static final Map<BlockPos, FullMoonBlockBehavior> FULL_MOON_BEHAVIORS = new HashMap<>();

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        MANAGER = context.dynamicLightBehaviorManager();
    }

    @SuppressWarnings({"removal", "UnstableApiUsage"})
    @Override
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {

    }

    public static void syncLampBehavior(MoonlightLampBlockEntity lamp) {
        if (MANAGER == null) return;
        if (!Config.REAL_LIGHT.get()) return;
        var pos = lamp.getBlockPos();
        var existing = LAMP_BEHAVIORS.get(pos);
        if (lamp.getPowered()) {
            if (existing == null) {
                MoonLampLineLightBehavior behavior = new MoonLampLineLightBehavior(pos);
                LAMP_BEHAVIORS.put(pos, behavior);
                MANAGER.add(behavior);
                behavior.hasChanged();
            }
        } else {
            if (existing != null) {
                MANAGER.remove(existing);
                LAMP_BEHAVIORS.remove(pos);
            }
        }
    }

    public static void updatePlayerBehaviors() {
        if (MANAGER == null) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!Config.REAL_LIGHT.get()) return;
        for (Player p : mc.level.players()) {
            var id = p.getUUID();
            var existing = PLAYER_BEHAVIORS.get(id);
            boolean on = Utils.isUsingFlashlight(p);
            if (on) {
                if (existing == null) {
                    PlayerFlashlightLineLightBehavior b = new PlayerFlashlightLineLightBehavior(p);
                    PLAYER_BEHAVIORS.put(id, b);
                    MANAGER.add(b);
                }
            } else {
                if (existing != null) {
                    MANAGER.remove(existing);
                    PLAYER_BEHAVIORS.remove(id);
                }
            }
        }
    }

    public static void addFullMoonBehavior(FullMoonBlockEntity moon) {
        if (MANAGER == null) return;
        var pos = moon.getBlockPos();
        var existing = FULL_MOON_BEHAVIORS.get(pos);

        if (existing == null) {
            FullMoonBlockBehavior b = new FullMoonBlockBehavior(pos);
            FULL_MOON_BEHAVIORS.put(pos, b);
            MANAGER.add(b);
        }
    }

    public static void ensureFullMoonBehaviorAt(BlockPos pos) {
        if (MANAGER == null) return;
        var existing = FULL_MOON_BEHAVIORS.get(pos);
        if (existing == null) {
            FullMoonBlockBehavior b = new FullMoonBlockBehavior(pos);
            FULL_MOON_BEHAVIORS.put(pos, b);
            MANAGER.add(b);
        }
    }

    public static void removeFullMoonBehavior(FullMoonBlockEntity moon) {
        if (MANAGER == null) return;
        var pos = moon.getBlockPos();
        var existing = FULL_MOON_BEHAVIORS.get(pos);

        if (existing != null) {
            MANAGER.remove(existing);
            FULL_MOON_BEHAVIORS.remove(pos);
        }
    }

    public static void removeFullMoonBehaviorAt(BlockPos pos) {
        if (MANAGER == null) return;
        var existing = FULL_MOON_BEHAVIORS.get(pos);
        if (existing != null) {
            MANAGER.remove(existing);
            FULL_MOON_BEHAVIORS.remove(pos);
        }
    }
}