package com.beiwu.forgottenrelicsplus.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class MirrorLocationData {
    private MirrorLocationData() {}

    public static boolean isValid(CompoundTag tag) {
        return tag.contains("IStoredX", Tag.TAG_INT)
                && tag.contains("IStoredY", Tag.TAG_INT)
                && tag.contains("IStoredZ", Tag.TAG_INT)
                && (!tag.contains("IDimensionID")
                        || tag.contains("IDimensionID", Tag.TAG_STRING)
                                && ResourceLocation.tryParse(tag.getString("IDimensionID")) != null);
    }

    public static void clear(CompoundTag tag) {
        tag.remove("IStoredX");
        tag.remove("IStoredY");
        tag.remove("IStoredZ");
        tag.remove("IDimensionID");
    }
}
