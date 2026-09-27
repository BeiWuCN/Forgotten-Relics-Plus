package com.beiwu.forgottenrelics_plus.utils;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;

/**
 * 模组自有的伤害类型。
 *
 * <p>1.12.2 里要造一种新伤害就写一个 {@code DamageSource} 子类（原版 {@code DamageRegistryHandler}
 * 里就有七八个）；1.21.1 改成了数据包注册表：先在 {@code data/forgotten_relics_plus/damage_type/}
 * 下放一个 JSON 定义伤害类型，代码里只保留一个 {@link ResourceKey}，实际取用时从世界的注册表里查。
 *
 * <p>死亡提示的翻译键由 JSON 里的 {@code message_id} 决定，所以这里的路径名刻意与
 * 原版 {@code DamageRegistryHandler} 里的语义一一对应，好让沿用下来的
 * {@code death.attack.*} 语言键继续生效。
 */
public final class FRDamageTypes {

    /** 湮灭伤害（原版 {@code DamageRegistryHandler.DamageSourceOblivion}），湮灭护符反噬自身时使用。 */
    public static final ResourceKey<DamageType> OBLIVION = key("oblivion");

    /** 超维伤害（原版 {@code DamageSourceSuperposition}），叠加之戒分摊伤害时使用。 */
    public static final ResourceKey<DamageType> SUPERPOSITION = key("superposition");

    /** 由某个实体造成的超维伤害（原版 {@code DamageSourceSuperpositionDefined}）。 */
    public static final ResourceKey<DamageType> SUPERPOSITION_DEFINED = key("superposition_defined");

    /** 夺魂伤害（原版 {@code DamageRegistryHandler.DamageSourceSoulDrain}），食尸鬼之颅的怨魂冲击使用。 */
    public static final ResourceKey<DamageType> SOUL_DRAIN = key("soul_drain");

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, path));
    }

    /**
     * 按 {@link ResourceKey} 造一个 DamageSource。
     *
     * <p>{@code owner} 为 {@code null} 时等价于原版那些无来源的 {@code DamageSource} 子类。
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().holderOrThrow(type));
    }

    /**
     * 带来源实体的版本，对应原版那些需要 owner 的 {@code DamageSource} 子类
     * （例如由某位玩家造成的超维伤害）。
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> type, net.minecraft.world.entity.Entity owner) {
        return new DamageSource(level.registryAccess().holderOrThrow(type), owner);
    }

    /**
     * 判断一种伤害是否「绝对伤害」——即不该被本模组的各种减伤/转移逻辑拦截的伤害。
     *
     * <p>对应原版 {@code SuperpositionHandler.isDamageTypeAbsolute}：虚空、饥饿，以及模组自己的
     * 湮灭 / 超维 / 夺魂伤害。原版还列了命运、真伤等类型，那些物品尚未移植，等移植时再补。
     */
    public static boolean isAbsolute(DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.STARVE)
                || source.is(OBLIVION)
                || source.is(SUPERPOSITION)
                || source.is(SUPERPOSITION_DEFINED)
                || source.is(SOUL_DRAIN);
    }

    private FRDamageTypes() {
    }
}
