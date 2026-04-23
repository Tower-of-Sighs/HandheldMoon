package com.sighs.handheldmoon.lights;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MoonlightLampEntityHeartbeatCenter {
    private static final Map<ResourceKey<Level>, State> STATES = new HashMap<>();

    private MoonlightLampEntityHeartbeatCenter() {
    }

    public static void report(Level level, UUID uuid) {
        if (uuid == null) return;
        State state = stateFor(level);
        state.reportedThisTick.add(uuid);
    }

    public static boolean isAlive(Level level, UUID uuid) {
        if (uuid == null) return false;
        State state = stateFor(level);
        return state.aliveLastTick.contains(uuid) || state.reportedThisTick.contains(uuid);
    }

    private static State stateFor(Level level) {
        State state = STATES.computeIfAbsent(level.dimension(), ignored -> new State());
        long gameTime = level.getGameTime();
        if (state.lastPreparedTick != gameTime) {
            state.aliveLastTick = state.reportedThisTick;
            state.reportedThisTick = new HashSet<>();
            state.lastPreparedTick = gameTime;
        }
        return state;
    }

    private static final class State {
        private long lastPreparedTick = Long.MIN_VALUE;
        private Set<UUID> aliveLastTick = new HashSet<>();
        private Set<UUID> reportedThisTick = new HashSet<>();
    }
}
