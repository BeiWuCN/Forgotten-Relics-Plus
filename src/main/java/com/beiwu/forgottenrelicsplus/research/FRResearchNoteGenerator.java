package com.beiwu.forgottenrelicsplus.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class FRResearchNoteGenerator {
    private FRResearchNoteGenerator() {}

    public static ResearchNoteData generate(
            ResourceLocation entry, int index, AspectList anchors, int complexity, RandomSource random) {
        if (complexity < 2 || complexity > 8) {
            throw new IllegalArgumentException("Research note complexity must be between 2 and 8");
        }
        int radius = Math.min(4, complexity + 1);
        Map<HexGrid.Hex, ResearchNoteData.Cell> cells = new LinkedHashMap<>();
        for (HexGrid.Hex hex : HexGrid.disk(radius)) {
            cells.put(hex, new ResearchNoteData.Cell(hex, ResearchNoteData.TYPE_BLANK, Optional.empty()));
        }
        List<AspectInstance> tags = anchors.entries();
        List<HexGrid.Hex> ring = HexGrid.distributeRing(radius, tags.size(), random);
        for (int i = 0; i < tags.size(); i++) {
            HexGrid.Hex hex = ring.get(i);
            cells.put(
                    hex,
                    new ResearchNoteData.Cell(
                            hex,
                            ResearchNoteData.TYPE_ROOT,
                            Optional.of(tags.get(i).aspect())));
        }
        List<HexGrid.Hex> candidates = new ArrayList<>();
        for (ResearchNoteData.Cell cell : cells.values()) {
            if (cell.type() == ResearchNoteData.TYPE_BLANK) candidates.add(cell.hex());
        }
        int holes = complexity * 2;
        while (holes > 0 && !candidates.isEmpty()) {
            HexGrid.Hex candidate = candidates.remove(random.nextInt(candidates.size()));
            ResearchNoteData.Cell removed = cells.remove(candidate);
            if (isConnected(cells) && rootsHaveNeighbours(cells)) {
                holes--;
            } else {
                cells.put(candidate, removed);
            }
        }
        return new ResearchNoteData(
                entry, index, NoteGenerator.primaryColor(anchors), false, 0, List.copyOf(cells.values()));
    }

    public static boolean isConnected(Map<HexGrid.Hex, ResearchNoteData.Cell> cells) {
        if (cells.isEmpty()) return false;
        Set<HexGrid.Hex> visited = new HashSet<>();
        ArrayDeque<HexGrid.Hex> queue = new ArrayDeque<>();
        HexGrid.Hex start = cells.keySet().iterator().next();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            HexGrid.Hex current = queue.remove();
            for (int dir = 0; dir < 6; dir++) {
                HexGrid.Hex next = current.neighbour(dir);
                if (cells.containsKey(next) && visited.add(next)) queue.add(next);
            }
        }
        return visited.size() == cells.size();
    }

    private static boolean rootsHaveNeighbours(Map<HexGrid.Hex, ResearchNoteData.Cell> cells) {
        for (ResearchNoteData.Cell cell : cells.values()) {
            if (cell.type() != ResearchNoteData.TYPE_ROOT) continue;
            int neighbours = 0;
            for (int dir = 0; dir < 6; dir++) if (cells.containsKey(cell.hex().neighbour(dir))) neighbours++;
            if (neighbours < 2) return false;
        }
        return true;
    }
}
