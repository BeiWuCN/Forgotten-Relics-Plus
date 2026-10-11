package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class MirrorLocationDataTest {
    private CompoundTag location() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("IStoredX", 3);
        tag.putInt("IStoredY", 64);
        tag.putInt("IStoredZ", -5);
        tag.putString("IDimensionID", "minecraft:overworld");
        return tag;
    }

    @Test
    void ignoresUnrelatedDataAndRejectsIncompleteOrInvalidLocations() {
        CompoundTag unrelated = new CompoundTag();
        unrelated.putString("other_mod", "kept");
        assertFalse(MirrorLocationData.isValid(unrelated));
        CompoundTag tag = location();
        assertTrue(MirrorLocationData.isValid(tag));
        tag.putString("IDimensionID", "INVALID DIMENSION");
        assertFalse(MirrorLocationData.isValid(tag));
        tag = location();
        tag.remove("IStoredY");
        assertFalse(MirrorLocationData.isValid(tag));
        tag.putString("IStoredY", "64");
        assertFalse(MirrorLocationData.isValid(tag));
    }

    @Test
    void clearsOnlyOwnedFieldsAndSupportsLegacyOverworldCoordinates() {
        CompoundTag tag = location();
        tag.remove("IDimensionID");
        assertTrue(MirrorLocationData.isValid(tag));
        tag.putString("other_mod", "kept");
        MirrorLocationData.clear(tag);
        assertEquals("kept", tag.getString("other_mod"));
        assertEquals(1, tag.size());
        assertFalse(MirrorLocationData.isValid(tag));
    }
}
