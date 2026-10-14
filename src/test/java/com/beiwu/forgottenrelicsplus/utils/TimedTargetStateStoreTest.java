package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TimedTargetStateStoreTest {
    @Test
    void expiredTargetIsEvictedOnlyAfterCooldownEnds() {
        TimedTargetStateStore store = new TimedTargetStateStore();
        UUID id = UUID.randomUUID();
        TimedTargetState state = store.getOrCreate(id);
        state.target = 12;
        state.ticksTillExpire = 1;
        state.ticksCooldown = 2;
        store.tick(id);
        assertSame(state, store.getOrCreate(id));
        assertEquals(-1, state.target);
        assertEquals(1, state.ticksCooldown);
        store.tick(id);
        assertNotSame(state, store.getOrCreate(id));
    }

    @Test
    void clearingTargetPreservesCooldownAndOtherPlayers() {
        TimedTargetStateStore store = new TimedTargetStateStore();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        TimedTargetState state = store.getOrCreate(first);
        TimedTargetState other = store.getOrCreate(second);
        state.target = 12;
        state.ticksTillExpire = 5;
        state.ticksCooldown = 40;
        other.target = 18;
        store.clearTarget(first);
        assertSame(state, store.getOrCreate(first));
        assertEquals(-1, state.target);
        assertEquals(40, state.ticksCooldown);
        assertSame(other, store.getOrCreate(second));
        assertEquals(18, other.target);
    }

    @Test
    void logoutAndServerStopDiscardState() {
        TimedTargetStateStore store = new TimedTargetStateStore();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        TimedTargetState state = store.getOrCreate(first);
        TimedTargetState other = store.getOrCreate(second);
        store.remove(first);
        assertNotSame(state, store.getOrCreate(first));
        assertSame(other, store.getOrCreate(second));
        store.clear();
        assertNotSame(other, store.getOrCreate(second));
        store.tick(UUID.randomUUID());
        store.clearTarget(UUID.randomUUID());
    }

    @Test
    void clearingTargetWithoutCooldownEvictsState() {
        TimedTargetStateStore store = new TimedTargetStateStore();
        UUID id = UUID.randomUUID();
        TimedTargetState state = store.getOrCreate(id);
        state.target = 12;
        state.ticksTillExpire = 5;
        store.clearTarget(id);
        assertNotSame(state, store.getOrCreate(id));
    }
}
