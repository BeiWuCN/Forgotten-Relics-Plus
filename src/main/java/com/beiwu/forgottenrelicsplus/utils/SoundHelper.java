package com.beiwu.forgottenrelicsplus.utils;

import com.beiwu.forgottenrelicsplus.config.FRConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class SoundHelper {

    private SoundHelper() {
    }

    public static void play(Level level, double x, double y, double z,
                            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, x, y, z, sound, source, scale(volume), pitch);
    }

    public static void play(Level level, BlockPos pos,
                            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, pos, sound, source, scale(volume), pitch);
    }

    private static float scale(float volume) {

        float loudness = Mth.clamp(volume, 0.0F, 1.0F);
        return (float) (loudness * FRConfig.SOUND_VOLUME_MULTIPLIER.get());
    }
}
