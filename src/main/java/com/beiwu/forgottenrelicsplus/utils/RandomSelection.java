package com.beiwu.forgottenrelicsplus.utils;

import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

public final class RandomSelection {
    private RandomSelection() {}

    public static <T> List<T> sample(List<T> candidates, int limit, IntUnaryOperator nextInt) {
        if (limit < 0) {
            throw new IllegalArgumentException("Negative sample limit");
        }
        int size = Math.min(limit, candidates.size());
        if (size == candidates.size()) {
            return candidates;
        }
        for (int i = 0; i < size; i++) {
            Collections.swap(candidates, i, i + nextInt.applyAsInt(candidates.size() - i));
        }
        return candidates.subList(0, size);
    }
}
