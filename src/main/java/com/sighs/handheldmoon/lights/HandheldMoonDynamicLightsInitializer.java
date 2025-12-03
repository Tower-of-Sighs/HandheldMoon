package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HandheldMoonDynamicLightsInitializer implements DynamicLightsInitializer {
    private static DynamicLightBehaviorManager MANAGER;
    private static final Map<BlockPos, MoonLampLineLightBehavior> LAMP_BEHAVIORS = new HashMap<>();
    private static final Map<UUID, PlayerFlashlightLineLightBehavior> PLAYER_BEHAVIORS = new HashMap<>();

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        MANAGER = context.dynamicLightBehaviorManager();
    }

    @SuppressWarnings({"UnstableApiUsage", "removal"})
    @Override
    public void onInitializeDynamicLights() {

    }


    public static void syncLampBehavior(MoonlightLampBlockEntity lamp) {
        if (MANAGER == null) return;
        BlockPos pos = lamp.getBlockPos();
        MoonLampLineLightBehavior existing = LAMP_BEHAVIORS.get(pos);
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Player p : mc.level.players()) {
            UUID id = p.getUUID();
            PlayerFlashlightLineLightBehavior existing = PLAYER_BEHAVIORS.get(id);
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
}