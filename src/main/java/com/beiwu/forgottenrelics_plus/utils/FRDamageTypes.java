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

    /** 真实伤害（原版 {@code DamageSourceTrueDamage}，带来源实体），虚伪审判把伤害「转化成」的就是它。 */
    public static final ResourceKey<DamageType> TRUE_DAMAGE = key("true_damage");

    /** 无来源的真实伤害（原版 {@code DamageSourceTrueDamageUndef}）。 */
    public static final ResourceKey<DamageType> TRUE_DAMAGE_UNDEF = key("true_damage_undef");

    /** 真雷伤害（原版 {@code DamageSourceTLightning}），霹雳咒书使用。 */
    public static final ResourceKey<DamageType> TRUE_LIGHTNING = key("true_lightning");

    /**
     * 暗物质伤害（原版 {@code DamageRegistryHandler.DamageSourceDarkMatter}，message_id = {@code attackDarkMatter}），
     * 邪术之咒的暗物质法球使用。
     *
     * <p>原版这个伤害源构造时调了 {@code setDamageBypassesArmor()} 与 {@code setExplosion}；
     * 1.21.1 里这两件事由 {@code tags/damage_type/*.json} 表达，本项目<b>暂不补任何 tag</b>
     * （是否补 armor bypass 待用户拍板，见 {@code Tools/HANDOVER.md} 第 5 节），
     * 所以这里的实际效果与原版有偏差：既不穿甲、也不带爆炸判定。
     *
     * <p>注意：原版 {@code SuperpositionHandler.isDamageTypeAbsolute} 的名单里<b>没有</b>暗物质，
     * 也就是它可以被七阳之戒、神圣护符等拦截或转嫁；这里同样不把它加进 {@link #isAbsolute}。
     */
    public static final ResourceKey<DamageType> DARK_MATTER = key("dark_matter");

    /**
     * 遗落魔法伤害（原版 {@code DamageRegistryHandler.DamageSourceMagic}，message_id = {@code forgottenMagic}），
     * 腥红之咒的猩红法球使用。
     *
     * <p>原版这个伤害源构造时调了 {@code setDamageBypassesArmor()}；1.21.1 里这件事由
     * {@code tags/damage_type/*.json} 表达，本项目<b>暂不补任何 tag</b>
     * （是否补 armor bypass 待用户拍板，见 {@code Tools/HANDOVER.md} 第 5 节），
     * 所以这里的实际效果与原版有偏差：不穿甲。
     *
     * <p>注意：原版 {@code SuperpositionHandler.isDamageTypeAbsolute} 的名单里没有魔法伤害，
     * 也就是它同样能被七阳之戒、神圣护符等拦截或转嫁；这里也不把它加进 {@link #isAbsolute}。
     */
    public static final ResourceKey<DamageType> FORGOTTEN_MAGIC = key("forgotten_magic");

    /**
     * 命运伤害（原版 {@code DamageRegistryHandler.DamageSourceFate}，message_id = {@code attackFate}），
     * 破碎的命运巨著「同时携带多本」时的自毁惩罚使用。
     *
     * <p>原版这个伤害源构造时调了 {@code setMagicDamage} + {@code setDamageIsAbsolute} +
     * {@code setDamageBypassesArmor} + {@code setDamageAllowedInCreativeMode}。
     * 其中「穿甲 / 创造模式可伤」在 1.21.1 里由 {@code tags/damage_type/*.json} 表达，
     * 本项目<b>暂不补任何 tag</b>（是否补 armor bypass 待用户拍板，见 {@code Tools/HANDOVER.md} 第 5 节），
     * 所以这里的实际效果与原版有偏差：不穿甲、创造模式玩家不受影响。
     * 而 {@code setDamageIsAbsolute} 在本项目里由 {@link #isAbsolute} 的名单承担，已一并加入——
     * 命运伤害不会被七阳之戒、虚伪审判等拦截或转嫁（原版就是这样）。
     */
    public static final ResourceKey<DamageType> FATE = key("fate");

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
     * 湮灭 / 超维 / 夺魂 / 真伤 / 命运伤害（命运类型是随破碎的命运巨著一起补上的）。
     *
     * <p>这张表同时承担<b>递归保护</b>的职责：虚伪审判把伤害取消后重新以真伤结算，如果真伤不在表里，
     * 那次重结算会被自己再拦一次，无限递归；传送之戒的分摊也是同一个道理。
     */
    public static boolean isAbsolute(DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.STARVE)
                || source.is(OBLIVION)
                || source.is(SUPERPOSITION)
                || source.is(SUPERPOSITION_DEFINED)
                || source.is(SOUL_DRAIN)
                || source.is(TRUE_DAMAGE)
                || source.is(TRUE_DAMAGE_UNDEF)
                || source.is(FATE);
    }

    private FRDamageTypes() {
    }
}
