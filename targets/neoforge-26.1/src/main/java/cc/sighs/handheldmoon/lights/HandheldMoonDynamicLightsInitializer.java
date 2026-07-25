package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class HandheldMoonDynamicLightsInitializer implements DynamicLightsInitializer {
    private static DynamicLightBehaviorManager MANAGER;
    private static final Map<BlockPos, MoonLampLineLightBehavior> LAMP_BEHAVIORS = new HashMap<>();
    private static final Set<BlockPos> POWERED_LAMP_POSITIONS = new HashSet<>();
    private static final Map<UUID, PlayerFlashlightLineLightBehavior> PLAYER_BEHAVIORS = new HashMap<>();

    public static Set<BlockPos> getActiveLampPositions() {
        return new HashSet<>(POWERED_LAMP_POSITIONS);
    }

    private static final Map<BlockPos, FullMoonBlockBehavior> FULL_MOON_BEHAVIORS = new HashMap<>();

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        MANAGER = context.dynamicLightBehaviorManager();
    }

    public static void syncLampBehavior(MoonlightLampBlockEntity lamp) {
        var pos = lamp.getBlockPos();
        if (lamp.getPowered()) {
            POWERED_LAMP_POSITIONS.add(pos);
        } else {
            POWERED_LAMP_POSITIONS.remove(pos);
        }

        if (MANAGER == null) return;
        var existing = LAMP_BEHAVIORS.get(pos);
        if (lamp.getPowered() && lamp.getLampConfig().realLight()) {
            if (existing == null) {
                MoonLampLineLightBehavior behavior = new MoonLampLineLightBehavior(pos);
                LAMP_BEHAVIORS.put(pos, behavior);
                MANAGER.add(behavior);
                behavior.hasChanged();
            }
            return;
        }
        if (existing != null) {
            MANAGER.remove(existing);
            LAMP_BEHAVIORS.remove(pos);
        }
    }

    public static void removeLampBehaviorAt(BlockPos pos) {
        POWERED_LAMP_POSITIONS.remove(pos);
        var existing = LAMP_BEHAVIORS.remove(pos);
        if (existing != null && MANAGER != null) {
            MANAGER.remove(existing);
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
