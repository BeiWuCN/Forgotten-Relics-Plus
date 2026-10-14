package com.beiwu.forgottenrelicsplus.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TimedTargetStateStore {
    private final Map<UUID, TimedTargetState> states = new HashMap<>();

    public TimedTargetState getOrCreate(UUID playerId) {
        return states.computeIfAbsent(playerId, id -> new TimedTargetState());
    }

    public void tick(UUID playerId) {
        TimedTargetState state = states.get(playerId);
        if (state != null) {
            state.tick();
            removeIfInactive(playerId, state);
        }
    }

    public void clearTarget(UUID playerId) {
        TimedTargetState state = states.get(playerId);
        if (state != null) {
            state.clearTarget();
            removeIfInactive(playerId, state);
        }
    }

    public void remove(UUID playerId) {
        states.remove(playerId);
    }

    public void clear() {
        states.clear();
    }

    private void removeIfInactive(UUID playerId, TimedTargetState state) {
        if (state.isInactive()) {
            states.remove(playerId);
        }
    }
}
