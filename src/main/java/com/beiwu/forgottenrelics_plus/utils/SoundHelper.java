package com.beiwu.forgottenrelics_plus.utils;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * 音效播放辅助。
 *
 * <p>存在的唯一理由：统一压低本移植音效的音量。
 *
 * <p>RE（1.12.2）各处 {@code playSound} 的音量参数是硬编码的，而且普遍偏大 ——
 * 最高的几处写到了 {@code 8.0F}（{@code EntityBabylonWeapon}）和 {@code 4.0F}
 * （{@code ItemVoidGrimoire}），即便是普通物品也有 {@code 2.0F}。
 *
 * <p>关于音量参数的实际语义（对照 1.21.1 的 {@code SoundEngine} 反编译结果）：
 * <ul>
 *   <li>真正的响度是 {@code Mth.clamp(volume * 频道音量, 0, 1)}（{@code SoundEngine.calculateVolume}），
 *       所以音量超过 1 的部分不会更响，最高就是满响度；</li>
 *   <li>音量大于 1 只会让声音传得更远 —— {@code SoundEngine} 用
 *       {@code Math.max(volume, 1.0F) * 衰减距离} 算可听半径。</li>
 * </ul>
 *
 * <p>因此<b>必须先把原始音量夹到 [0,1] 再乘倍率</b>。
 * 若直接乘倍率，RE 那些 {@code 8.0F} / {@code 4.0F} 的调用点会得到 {@code 3.6} / {@code 1.8}，
 * 交给游戏后又被夹回满响度，等于完全没降 —— 这是很容易踩的坑，所以在此显式夹紧。
 * 副作用是那些原本"传得很远"的音效可听半径会缩回默认的 16 格，这与"把音量降下来"的意图一致。
 *
 * <p>处理办法是不去逐个改调用点的常量，而是把播放动作收敛到这里，
 * 由 {@link FRConfig#SOUND_VOLUME_MULTIPLIER} 统一缩放。
 * 这样音量大小只在一处可调，也避免以后补物品时又漏掉几处。
 *
 * <p>只缩放音量，不改音调 —— 音调是音色的一部分，改动会改变音色。
 */
public final class SoundHelper {

    private SoundHelper() {
    }

    /**
     * 在指定坐标播放音效，音量按配置缩放。
     *
     * @param level  所在世界
     * @param x      X 坐标
     * @param y      Y 坐标
     * @param z      Z 坐标
     * @param sound  音效事件
     * @param source 音效频道
     * @param volume 原始音量（会乘以配置倍率）
     * @param pitch  音调（不缩放）
     */
    public static void play(Level level, double x, double y, double z,
                            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, x, y, z, sound, source, scale(volume), pitch);
    }

    /**
     * 在指定方块位置播放音效，音量按配置缩放。
     *
     * @param level  所在世界
     * @param pos    方块位置
     * @param sound  音效事件
     * @param source 音效频道
     * @param volume 原始音量（会乘以配置倍率）
     * @param pitch  音调（不缩放）
     */
    public static void play(Level level, BlockPos pos,
                            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, pos, sound, source, scale(volume), pitch);
    }

    /**
     * 按配置倍率缩放音量。
     *
     * @param volume 原始音量
     * @return 缩放后的音量
     */
    private static float scale(float volume) {
        // 先夹到 [0,1]：游戏里的响度就是 clamp(volume, 0, 1)，
        // 大于 1 的部分只是扩大可听半径。不夹的话，8.0F 这类原始音量
        // 乘完倍率仍大于 1，会被游戏夹回满响度，等于没降音量。
        float loudness = Mth.clamp(volume, 0.0F, 1.0F);
        return (float) (loudness * FRConfig.SOUND_VOLUME_MULTIPLIER.get());
    }
}
