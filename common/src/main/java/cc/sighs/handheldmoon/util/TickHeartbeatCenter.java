package cc.sighs.handheldmoon.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TickHeartbeatCenter<K, I> {
    private final Map<K, State<I>> states = new HashMap<>();

    public void report(K scope, long tick, I id) {
        if (id != null) {
            stateFor(scope, tick).reportedThisTick.add(id);
        }
    }

    public boolean isAlive(K scope, long tick, I id) {
        if (id == null) {
            return false;
        }
        State<I> state = stateFor(scope, tick);
        return state.aliveLastTick.contains(id) || state.reportedThisTick.contains(id);
    }

    private State<I> stateFor(K scope, long tick) {
        State<I> state = states.get(scope);
        if (state == null) {
            state = new State<>();
            states.put(scope, state);
        }
        if (state.lastPreparedTick != tick) {
            state.aliveLastTick = state.reportedThisTick;
            state.reportedThisTick = new HashSet<>();
            state.lastPreparedTick = tick;
        }
        return state;
    }

    private static final class State<I> {
        private long lastPreparedTick = Long.MIN_VALUE;
        private Set<I> aliveLastTick = new HashSet<>();
        private Set<I> reportedThisTick = new HashSet<>();
    }
}
