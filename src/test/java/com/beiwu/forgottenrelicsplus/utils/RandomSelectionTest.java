package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RandomSelectionTest {
    @Test
    void samplesWithoutReplacementWithBoundedWork() {
        List<Integer> candidates =
                new ArrayList<>(IntStream.range(0, 10000).boxed().toList());
        int[] calls = {0};
        Random random = new Random(5);
        List<Integer> selected = RandomSelection.sample(candidates, 3, bound -> {
            calls[0]++;
            return random.nextInt(bound);
        });
        assertEquals(3, calls[0]);
        assertEquals(3, selected.size());
        assertEquals(3, new HashSet<>(selected).size());
        assertEquals(10000, new HashSet<>(candidates).size());
    }

    @Test
    void handlesSmallAndEmptyPoolsWithoutRandomCalls() {
        List<Integer> candidates = new ArrayList<>(List.of(1, 2));
        assertEquals(List.of(1, 2), RandomSelection.sample(candidates, 3, bound -> {
            throw new AssertionError("Unnecessary random call");
        }));
        assertTrue(RandomSelection.sample(candidates, 0, bound -> 0).isEmpty());
        assertTrue(RandomSelection.sample(new ArrayList<>(), 3, bound -> 0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> RandomSelection.sample(candidates, -1, bound -> 0));
    }

    @Test
    void everyCandidateCanBeSelected() {
        int[] hits = new int[10];
        Random random = new Random(9);
        for (int i = 0; i < 10000; i++) {
            List<Integer> candidates =
                    new ArrayList<>(IntStream.range(0, 10).boxed().toList());
            for (int selected : RandomSelection.sample(candidates, 3, random::nextInt)) {
                hits[selected]++;
            }
        }
        for (int count : hits) {
            assertTrue(count > 2600 && count < 3400);
        }
    }
}
