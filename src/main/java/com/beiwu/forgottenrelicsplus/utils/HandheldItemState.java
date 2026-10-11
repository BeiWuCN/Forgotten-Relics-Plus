package com.beiwu.forgottenrelicsplus.utils;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;

public final class HandheldItemState {
    private HandheldItemState() {}

    public static boolean sameVisibleComponents(DataComponentMap oldComponents, DataComponentMap newComponents,
                                                 Predicate<DataComponentType<?>> visible) {
        if (oldComponents == newComponents) return true;
        for (DataComponentType<?> type : oldComponents.keySet()) {
            if (visible.test(type) && !Objects.equals(oldComponents.get(type), newComponents.get(type))) return false;
        }
        for (DataComponentType<?> type : newComponents.keySet()) {
            if (visible.test(type) && !oldComponents.has(type)) return false;
        }
        return true;
    }
}
