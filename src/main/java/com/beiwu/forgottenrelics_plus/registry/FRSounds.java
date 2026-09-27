package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 本移植自带的音效事件。
 *
 * <p>1.7.10 原版在 {@code assets/forgottenrelics/sounds.json} 里声明了 5 个自定义音效，
 * 对应的 ogg 就在原版 jar 的 {@code sounds/specific/} 下。这 5 个里：
 * <ul>
 *   <li>{@code lunarFlare} / {@code starfall} / {@code md_charge} 有实际调用点，见下；</li>
 *   <li>{@code directed}（原版事件名 {@code sound.meme112}）只被盖亚守护者反作弊系统使用，
 *       而该系统本移植不做。音效仍然搬了过来并注册，一是与原版 jar 对齐，二是将来若补那套
 *       系统可以直接用；</li>
 *   <li>{@code discharge} 在原版代码里从未被播放（死资源），不移植。</li>
 * </ul>
 *
 * <p>原版调用点与参数（音量会被 {@code SoundHelper} 统一缩放，见 FRConfig#SOUND_VOLUME_MULTIPLIER）：
 * <pre>
 *   lunarFlare  EntityLunarFlare.java:119   落点，音量 16、音调 0.8 + rand*0.2
 *   starfall    ItemLunarFlares.java:129    施法时每 4 tick 在玩家处，音量 2、音调 1 + rand*0.5
 *   md_charge   ItemVoidGrimoire.java:170   引导第一 tick，音量 4、音调 0.75
 * </pre>
 *
 * <p>注意音效 id 用的是 snake_case（{@code lunar_flare}），原版是 {@code sound.lunarFlare}；
 * 资源文件名也一并改成 snake_case，{@code sounds.json} 里的键必须与这里的注册名一致。
 */
public final class FRSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ForgottenRelics.MOD_ID);

    /** 月耀咒书的耀月之辉落地。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> LUNAR_FLARE = register("lunar_flare");

    /** 月耀咒书施法时的流星呼啸。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> STARFALL = register("starfall");

    /** 深渊之魔书开始引导。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> MD_CHARGE = register("md_charge");

    /** 盖亚守护者反作弊（本移植未实现，先注册备用）。原版事件名 {@code sound.meme112}。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> DIRECTED = register("directed");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    private FRSounds() {
    }
}
