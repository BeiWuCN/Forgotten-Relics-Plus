package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.junit.jupiter.api.Test;

class HandheldItemStateTest {
    private static final DataComponentType<Integer> CHARGE = DataComponentType.<Integer>builder().persistent(Codec.INT).build();
    private static final DataComponentType<Integer> APPEARANCE = DataComponentType.<Integer>builder().persistent(Codec.INT).build();

    private static boolean same(DataComponentMap oldData, DataComponentMap newData) {
        return HandheldItemState.sameVisibleComponents(oldData, newData, type -> type != CHARGE);
    }

    @Test
    void spendingAndRefillingChargeDoNotChangeHeldAppearance() {
        var before = DataComponentMap.builder().set(CHARGE, 100).set(APPEARANCE, 1).build();
        var casting = DataComponentMap.builder().set(CHARGE, 80).set(APPEARANCE, 1).build();
        var charging = DataComponentMap.builder().set(CHARGE, 120).set(APPEARANCE, 1).build();
        assertTrue(same(before, casting));
        assertTrue(same(casting, charging));
    }

    @Test
    void internalComponentAdditionAndRemovalDoNotResetHeldItem() {
        var visible = DataComponentMap.builder().set(APPEARANCE, 1).build();
        var withCharge = DataComponentMap.builder().set(APPEARANCE, 1).set(CHARGE, 10).build();
        assertTrue(same(visible, withCharge));
        assertTrue(same(withCharge, visible));
    }

    @Test
    void visibleComponentChangesStillTriggerAnimation() {
        var before = DataComponentMap.builder().set(APPEARANCE, 1).build();
        var after = DataComponentMap.builder().set(APPEARANCE, 2).build();
        assertFalse(same(before, after));
        assertFalse(same(DataComponentMap.EMPTY, before));
        assertFalse(same(before, DataComponentMap.EMPTY));
    }
}
