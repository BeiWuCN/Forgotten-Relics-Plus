package com.beiwu.forgottenrelics_plus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置。
 *
 * <p>对应 1.12.2 原版的 {@code RelicsConfigHandler}。原版用的是 Forge 的 {@code Configuration}
 * 加一堆静态字段，NeoForge 1.21.1 换成了 {@link ModConfigSpec}：字段本身是「值的包装」，
 * 读取时要调用 {@code get()}。
 *
 * <p>键名与默认值都照抄原版，默认值后面的注释里标出原版所在的分类，方便对照老配置文件。
 * 原版按物品分了很多分类，这里按同样的思路 push 出子分类。
 */
public final class FRConfig {

    public static final ModConfigSpec SPEC;

    // ---- 通用 / Generic Config ----
    /** 采矿护符：挖掘速度加成倍率，1.0 表示 +100%。 */
    public static final ModConfigSpec.DoubleValue MINING_CHARM_BOOST;
    /** 采矿护符：方块触及距离增加（格）。 */
    public static final ModConfigSpec.DoubleValue MINING_CHARM_REACH;
    /** 以太采矿护符：挖掘速度加成倍率，3.0 表示 +300%。 */
    public static final ModConfigSpec.DoubleValue ADVANCED_MINING_CHARM_BOOST;
    /** 以太采矿护符：方块触及距离增加（格）。 */
    public static final ModConfigSpec.DoubleValue ADVANCED_MINING_CHARM_REACH;
    /** 维度魔镜是否允许跨维度传送。 */
    public static final ModConfigSpec.BooleanValue INTERDIMENSIONAL_MIRROR;

    // ---- 叠加之戒 / Superposition Ring ----
    /** 每次判定时的交换概率。 */
    public static final ModConfigSpec.DoubleValue SUPERPOSITION_RING_SWAP_CHANCE;
    /** 交换判定的间隔（tick）。 */
    public static final ModConfigSpec.IntValue SUPERPOSITION_RING_CHECK_INTERVAL;
    /** 传送之戒：伤害分摊比例的下限。 */
    public static final ModConfigSpec.DoubleValue SUPERPOSITION_RING_SPLIT_MIN;
    /** 传送之戒：伤害分摊比例的上限。 */
    public static final ModConfigSpec.DoubleValue SUPERPOSITION_RING_SPLIT_MAX;

    // ---- 经验之书 / XP Tome ----
    public static final ModConfigSpec.IntValue XP_TOME_TRANSFER_RATE;

    // ---- 符文天象石 / Weather Stone ----
    public static final ModConfigSpec.IntValue WEATHER_STONE_VIS_COST;
    public static final ModConfigSpec.DoubleValue WEATHER_STONE_VIS_MULT;
    public static final ModConfigSpec.IntValue WEATHER_STONE_CHANNEL_DURATION;
    public static final ModConfigSpec.IntValue WEATHER_STONE_COOLDOWN;

    // ---- 空间魔镜 / Dimensional Mirror ----
    public static final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_CHANNEL_DURATION;

    // ---- 音效 / Sound ----
    /** 全局音效音量倍率。原版音量偏大，这里统一压低。 */
    public static final ModConfigSpec.DoubleValue SOUND_VOLUME_MULTIPLIER;

    // ---- Vis 上限 / Vis ----
    public static final ModConfigSpec.IntValue WEATHER_STONE_MAX_CHARGE;
    public static final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_MAX_CHARGE;

    // ---- 远古之庇护 / Ancient Aegis ----
    /** 佩戴者受到的伤害减免比例，0.25 表示 25%。 */
    public static final ModConfigSpec.DoubleValue ANCIENT_AEGIS_DAMAGE_REDUCTION;
    public static final ModConfigSpec.DoubleValue ANCIENT_AEGIS_HEAL_AMOUNT;
    public static final ModConfigSpec.IntValue ANCIENT_AEGIS_HEAL_INTERVAL;
    /** 击退抗性加成（1.0 表示完全免疫击退）。 */
    public static final ModConfigSpec.DoubleValue ANCIENT_AEGIS_KNOCKBACK_RESISTANCE;

    // ---- 七阳之戒 / Ring of The Seven Suns ----
    /** 超过该数值的伤害会被完全抵消。 */
    public static final ModConfigSpec.DoubleValue DARK_SUN_RING_DAMAGE_CAP;
    public static final ModConfigSpec.DoubleValue DARK_SUN_RING_DEFLECT_CHANCE;
    /** 是否给「火焰伤害转化为治疗」加上冷却限制。 */
    public static final ModConfigSpec.BooleanValue DARK_SUN_RING_HEAL_LIMIT;
    public static final ModConfigSpec.IntValue DARK_SUN_RING_MAX_CHARGE;

    // ---- 神圣护身符 / Deific Amulet ----
    public static final ModConfigSpec.BooleanValue DEIFIC_AMULET_EFFECT_IMMUNITY;
    /** 免疫状态效果时是否只清除减益、保留增益。 */
    public static final ModConfigSpec.BooleanValue DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS;
    public static final ModConfigSpec.BooleanValue DEIFIC_AMULET_INVINCIBILITY;
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_INVINCIBILITY_EXTENSION;
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_INVINCIBILITY_COOLDOWN;
    /** 窒息时补充的氧气量（tick）。 */
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_AIR_SUPPLY;
    /** 每次补充氧气的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_VIS_COST;
    public static final ModConfigSpec.DoubleValue DEIFIC_AMULET_VIS_MULT;
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_MAX_CHARGE;

    // ---- 湮灭护符 / Amulet of The Oblivion ----
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_DAMAGE_RELEASE_CHANCE;
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_DAMAGE_CAP;
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_HIGH_DAMAGE_REDUCTION_CHANCE;
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_POTION_CHANCE;
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_DURATION_MIN;
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_DURATION_MAX;
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_LEVEL_MIN;
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_LEVEL_MAX;
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_WARP;
    /** 储存伤害时的 Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_VIS_MULT;
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_MAX_CHARGE;

    // ---- 日耀石 / Shiny Stone ----
    public static final ModConfigSpec.IntValue SHINY_STONE_CHECK_RATE;
    public static final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_2;
    public static final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_3;
    public static final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_4;
    /** 每次判定静止时累计值的增量。 */
    public static final ModConfigSpec.IntValue SHINY_STONE_STILL_INCREMENT;
    public static final ModConfigSpec.DoubleValue SHINY_STONE_HEAL_AMOUNT;

    // ---- 浑浊之核 / Nebulous Core（注册名 arcanum）----
    /** 被动生成 Vis 的概率倍率。 */
    public static final ModConfigSpec.DoubleValue ARCANUM_GEN_RATE;
    /** 每 tick 随机传送的概率。 */
    public static final ModConfigSpec.DoubleValue ARCANUM_TELEPORT_CHANCE;
    public static final ModConfigSpec.IntValue ARCANUM_TELEPORT_RANGE;
    public static final ModConfigSpec.DoubleValue ARCANUM_DORMANT_TRANSFORM_CHANCE;
    /** 休眠态寿命的最小值。 */
    public static final ModConfigSpec.IntValue ARCANUM_DORMANT_LIFE_MIN;
    /** 休眠态寿命的最大值。 */
    public static final ModConfigSpec.IntValue ARCANUM_DORMANT_LIFE_MAX;
    /** 提供的 Vis 折扣（百分比）。 */
    public static final ModConfigSpec.DoubleValue ARCANUM_VIS_DISCOUNT;
    /** 浑浊之核的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue ARCANUM_MAX_CHARGE;
    /** 佩戴浑浊之核时的受击闪避概率。 */
    public static final ModConfigSpec.DoubleValue NEBULOUS_CORE_DODGE_CHANCE;

    // ---- 休眠浑浊之核 / Dormant Nebulous Core ----
    /** 每 tick 唤醒消耗的 Vis。 */
    public static final ModConfigSpec.IntValue DORMANT_ARCANUM_VIS_COST_PER_TICK;
    /** 休眠浑浊之核的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue DORMANT_ARCANUM_MAX_CHARGE;
    /** 转化为休眠态时的寿命倍率。 */
    public static final ModConfigSpec.DoubleValue DORMANT_ARCANUM_VIS_MULT;

    // ---- 恐惧之冠 / Crown of Terror ----
    /** 挑拨怪物互相攻击的作用半径（格）。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_HAVOC_RANGE;
    /** 注视目标的最大距离（格）。 */
    public static final ModConfigSpec.DoubleValue TERROR_CROWN_SCAN_RANGE;
    /** 失明持续时间（tick）。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_BLINDNESS_DURATION;
    /** 凋零持续时间（tick）。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_WITHER_DURATION;
    /** 凋零等级。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_WITHER_LEVEL;
    /** 反胃持续时间（tick）。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_NAUSEA_DURATION;
    /** 反胃等级。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_NAUSEA_LEVEL;
    /** 缓慢持续时间（tick）。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_SLOWNESS_DURATION;
    /** 缓慢等级。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_SLOWNESS_LEVEL;
    /** 虚弱持续时间（tick）。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_WEAKNESS_DURATION;
    /** 虚弱等级。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_WEAKNESS_LEVEL;
    /** 每修复 1 点耐久消耗的魔力。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_MANA_COST;
    /** 恐惧之冠附带的扭曲值。 */
    public static final ModConfigSpec.IntValue TERROR_CROWN_WARP;

    /** 食尸鬼之颅：允许献祭所需的最低生命值。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_MIN_HEALTH;
    /** 食尸鬼之颅：怨魂冲击沿视线能到达的最大距离（格）。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_BURST_RANGE;
    /** 食尸鬼之颅：怨魂冲击的作用半径（格）。 */
    public static final ModConfigSpec.DoubleValue GHASTLY_SKULL_BURST_RADIUS;
    /** 食尸鬼之颅：每献祭 1 点生命造成的伤害。 */
    public static final ModConfigSpec.DoubleValue GHASTLY_SKULL_DAMAGE_MULT;
    /** 食尸鬼之颅：单次冲击的伤害上限。 */
    public static final ModConfigSpec.DoubleValue GHASTLY_SKULL_MAX_DAMAGE;
    /** 食尸鬼之颅：每命中一个目标回复的生命值。 */
    public static final ModConfigSpec.DoubleValue GHASTLY_SKULL_HEAL_PER_TARGET;
    /** 食尸鬼之颅：命中目标的凋零持续时间（tick）。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_WITHER_DURATION;
    /** 食尸鬼之颅：命中目标的凋零等级。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_WITHER_LEVEL;
    /** 食尸鬼之颅：每次发动消耗的 Vis。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_VIS_COST;
    /** 食尸鬼之颅：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_MAX_CHARGE;
    /** 食尸鬼之颅：发动后的冷却（tick）。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_COOLDOWN;
    /** 食尸鬼之颅：附带的扭曲值。 */
    public static final ModConfigSpec.IntValue GHASTLY_SKULL_WARP;

    /** 混沌之核：每 tick 触发随机状态效果的概率。 */
    public static final ModConfigSpec.DoubleValue CHAOS_CORE_CHANCE;
    /** 混沌之核：状态效果的最短持续时间（tick）。 */
    public static final ModConfigSpec.IntValue CHAOS_CORE_DURATION_MIN;
    /** 混沌之核：在最短持续时间之上再随机叠加的区间长度（tick）。 */
    public static final ModConfigSpec.IntValue CHAOS_CORE_DURATION_SPAN;
    /** 混沌之核：能随机到的最高等级（0 即 I 级）。 */
    public static final ModConfigSpec.IntValue CHAOS_CORE_MAX_AMPLIFIER;
    /** 混沌之核：附带的扭曲值。 */
    public static final ModConfigSpec.IntValue CHAOS_CORE_WARP;
    /** 混沌之核：攻击者携带时转嫁伤害的概率。 */
    public static final ModConfigSpec.DoubleValue CHAOS_CORE_REDIRECT_ATTACK;
    /** 混沌之核：受害者携带时转嫁伤害的概率。 */
    public static final ModConfigSpec.DoubleValue CHAOS_CORE_REDIRECT_DEFEND;
    /** 混沌之核：攻击者携带时，转嫁改为反弹给自己的概率。 */
    public static final ModConfigSpec.DoubleValue CHAOS_CORE_SELF_REFLECT;
    /** 混沌之核：转嫁量与随机系数的上限倍率（原版写死 2.0）。 */
    public static final ModConfigSpec.DoubleValue CHAOS_CORE_DAMAGE_MULT_MAX;
    /** 欧米伽之核：每 tick 给法杖的每个原初要素补充的 Vis。 */
    public static final ModConfigSpec.IntValue OMEGA_CORE_VIS_PER_TICK;
    /** 欧米伽之核：每 tick 给携带者身上每件遗物（FRRechargable）补充的充能。 */
    public static final ModConfigSpec.IntValue OMEGA_CORE_RECHARGE_PER_TICK;
    /** 欧米伽之核：是否免疫致死伤害。 */
    public static final ModConfigSpec.BooleanValue OMEGA_CORE_PREVENT_DEATH;
    /** 悖论之刃：伤害上限（目标所受与自身所受之和）。 */
    public static final ModConfigSpec.DoubleValue PARADOX_DAMAGE_CAP;
    /** 悖论之刃：附带的扭曲值。 */
    public static final ModConfigSpec.IntValue PARADOX_WARP;
    /** 虚伪审判：伤害转化后的倍率。 */
    public static final ModConfigSpec.DoubleValue FALSE_JUSTICE_DAMAGE_MULTIPLIER;
    /** 虚伪审判：是否阻止死亡（携带者与被携带者打死的目标）。 */
    public static final ModConfigSpec.BooleanValue FALSE_JUSTICE_PREVENT_DEATH;
    /** 虚伪审判：附带的扭曲值。 */
    public static final ModConfigSpec.IntValue FALSE_JUSTICE_WARP;

    /** 霹雳咒书：直接命中的伤害。 */
    public static final ModConfigSpec.DoubleValue THUNDERPEAL_DIRECT_DAMAGE;
    /** 霹雳咒书：范围与链式闪电的伤害。 */
    public static final ModConfigSpec.DoubleValue THUNDERPEAL_BOLT_DAMAGE;
    /** 霹雳咒书：每次施法的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue THUNDERPEAL_VIS_COST;
    /** 霹雳咒书：Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue THUNDERPEAL_VIS_MULT;
    /** 霹雳咒书：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue THUNDERPEAL_MAX_CHARGE;
    /** 霹雳咒书：使用后的冷却（tick）。 */
    public static final ModConfigSpec.IntValue THUNDERPEAL_COOLDOWN;

    /** 错位之典：每次施法的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue DISCORD_TOME_VIS_COST;
    /** 错位之典：Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue DISCORD_TOME_VIS_MULT;
    /** 错位之典：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue DISCORD_TOME_MAX_CHARGE;
    /** 错位之典：使用后的冷却（tick）。 */
    public static final ModConfigSpec.IntValue DISCORD_TOME_COOLDOWN;

    // ---- 虚空吞噬者 / Devourer of The Void ----
    /** 虚空吞噬者：给背包法杖补 Vis 的倍率（对应原版 {@code obeliskDrainerVisMult}）。 */
    public static final ModConfigSpec.DoubleValue DEVOURER_OF_THE_VOID_VIS_MULT;
    /** 虚空吞噬者：能感知到神秘方尖碑的最大距离（格）。 */
    public static final ModConfigSpec.DoubleValue DEVOURER_OF_THE_VOID_RANGE;
    /** 虚空吞噬者：两次抽取之间的间隔（tick）。 */
    public static final ModConfigSpec.IntValue DEVOURER_OF_THE_VOID_PULSE_INTERVAL;
    /** 虚空吞噬者：每次抽取回复的生命值。 */
    public static final ModConfigSpec.DoubleValue DEVOURER_OF_THE_VOID_HEAL;
    /** 虚空吞噬者：每次抽取补充的饥饿值。 */
    public static final ModConfigSpec.IntValue DEVOURER_OF_THE_VOID_HUNGER;
    /** 虚空吞噬者：附带的扭曲值。 */
    public static final ModConfigSpec.IntValue DEVOURER_OF_THE_VOID_WARP;

    // ---- 邪术之咒 / Eldritch Spell ----
    /** 邪术之咒：每次施法的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue ELDRITCH_SPELL_VIS_COST;
    /** 邪术之咒：Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue ELDRITCH_SPELL_VIS_MULT;
    /** 邪术之咒：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue ELDRITCH_SPELL_MAX_CHARGE;
    /** 邪术之咒：使用后的冷却（tick）。 */
    public static final ModConfigSpec.IntValue ELDRITCH_SPELL_COOLDOWN;
    /** 邪术之咒：普通维度下暗物质法球造成的伤害。 */
    public static final ModConfigSpec.DoubleValue ELDRITCH_SPELL_DAMAGE;
    /** 邪术之咒：外域（Outer Lands）中暗物质法球造成的伤害。 */
    public static final ModConfigSpec.DoubleValue ELDRITCH_SPELL_DAMAGE_EX;

    // ---- 腥红之咒 / Crimson Spell ----
    /** 腥红之咒：每次施法的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue CRIMSON_SPELL_VIS_COST;
    /** 腥红之咒：Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue CRIMSON_SPELL_VIS_MULT;
    /** 腥红之咒：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue CRIMSON_SPELL_MAX_CHARGE;
    /** 腥红之咒：使用后的冷却（tick）。 */
    public static final ModConfigSpec.IntValue CRIMSON_SPELL_COOLDOWN;
    /** 腥红之咒：法球伤害下限。 */
    public static final ModConfigSpec.DoubleValue CRIMSON_SPELL_DAMAGE_MIN;
    /** 腥红之咒：法球伤害上限。 */
    public static final ModConfigSpec.DoubleValue CRIMSON_SPELL_DAMAGE_MAX;
    /** 腥红之咒：附带的扭曲值。 */
    public static final ModConfigSpec.IntValue CRIMSON_SPELL_WARP;

    // ---- 原初混沌之典 / Tome of Primal Chaos ----
    /** 原初混沌之典：每次生成法球的 Vis 基础消耗（原版六项随机消耗的平均值 3）。 */
    public static final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_VIS_COST;
    /** 原初混沌之典：Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_PRIMAL_CHAOS_VIS_MULT;
    /** 原初混沌之典：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_MAX_CHARGE;
    /** 原初混沌之典：连续引导时两次生成法球的间隔（tick），原版硬编码为 2。 */
    public static final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_COOLDOWN;
    /** 原初混沌之典：原版 {@code chaosTomeDamageCap}，法球命中伤害的上限。 */
    public static final ModConfigSpec.DoubleValue CHAOS_TOME_DAMAGE_CAP;
    /** 原初混沌之典：附带的扭曲值。 */
    public static final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_WARP;

    // ---- 核子之怒 / Nuclear Fury ----
    /** 核子之怒：每秒的 Vis 基础消耗（原版每秒 10 颗 × 每颗 0.45 = 4.5，RE 取整为 5）。 */
    public static final ModConfigSpec.IntValue NUCLEAR_FURY_VIS_COST_PER_SECOND;
    /**
     * 核子之怒：Vis 消耗倍率。
     *
     * <p>原版配置 key 就叫 {@code nuclearFuryVisCost}（字段名却是 {@code nuclearFuryVisMult}），
     * 与其它物品的「基础值 + 倍率」同名 key 冲突一样，这里沿用 {@code nuclearFuryVisMult} 作为 key。
     */
    public static final ModConfigSpec.DoubleValue NUCLEAR_FURY_VIS_MULT;
    /** 核子之怒：最大 Vis 储量（正好是 100 秒的连续引导）。 */
    public static final ModConfigSpec.IntValue NUCLEAR_FURY_MAX_CHARGE;
    /**
     * 核子之怒：导弹的目标搜索半径。
     *
     * <p>key 照抄 RE 的 {@code nuclearFuryClearRange}（RE 用它做「左键清除 32 格内导弹」的范围）；
     * 1.7.10 的 {@code ItemMissileTome} 没有清弹功能，而同为 32 的这个数字在那边的实体里是
     * <b>目标搜索半径</b>（{@code double range = 32.0}），所以这里就把它用作搜索半径。
     */
    public static final ModConfigSpec.DoubleValue NUCLEAR_FURY_CLEAR_RANGE;
    /** 核子之怒：导弹伤害下限。 */
    public static final ModConfigSpec.DoubleValue NUCLEAR_FURY_DAMAGE_MIN;
    /** 核子之怒：导弹伤害上限。 */
    public static final ModConfigSpec.DoubleValue NUCLEAR_FURY_DAMAGE_MAX;
    /** 核子之怒：附带的扭曲值（原版 {@code getWarp} 返回 5）。 */
    public static final ModConfigSpec.IntValue NUCLEAR_FURY_WARP;

    // ---- 千咒之诫 / Edict of a Thousand Damned Souls ----
    /** 千咒之诫：每 4 tick 一次灵魂抽取的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue SOUL_TOME_VIS_COST;
    /** 千咒之诫：Vis 消耗倍率（原版配置 key 是 soulTomeVisCost，字段语义却是倍率）。 */
    public static final ModConfigSpec.DoubleValue SOUL_TOME_VIS_MULT;
    /** 千咒之诫：近距离击退每命中一个实体扣一次的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue SOUL_TOME_KNOCKBACK_VIS_COST;
    /** 千咒之诫：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue SOUL_TOME_MAX_CHARGE;
    /** 千咒之诫：灵魂抽取伤害的计算除数（原版 soulTomeDivisor）。 */
    public static final ModConfigSpec.DoubleValue SOUL_TOME_DIVISOR;
    /** 千咒之诫：引导预热时间（tick），期间不结算任何效果。 */
    public static final ModConfigSpec.IntValue SOUL_TOME_WARMUP_TICKS;
    /** 千咒之诫：灵魂抽取伤害下限。 */
    public static final ModConfigSpec.DoubleValue SOUL_TOME_DAMAGE_MIN;
    /** 千咒之诫：灵魂抽取伤害上限。 */
    public static final ModConfigSpec.DoubleValue SOUL_TOME_DAMAGE_MAX;
    /** 千咒之诫：近距离击退的真雷伤害下限。 */
    public static final ModConfigSpec.DoubleValue SOUL_TOME_KNOCKBACK_DAMAGE_MIN;
    /** 千咒之诫：近距离击退的真雷伤害上限。 */
    public static final ModConfigSpec.DoubleValue SOUL_TOME_KNOCKBACK_DAMAGE_MAX;
    /** 千咒之诫：附带的扭曲值（原版 {@code getWarp} 返回 3）。 */
    public static final ModConfigSpec.IntValue SOUL_TOME_WARP;

    // ---- 永恒放逐之诫 / Edict of Eternal Banishment ----
    /** 永恒放逐之诫：引导时每秒的 Vis 基础消耗（原版每 tick 18 厘 = 3.6 点/秒，整数取 4）。 */
    public static final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_VIS_COST;
    /** 永恒放逐之诫：Vis 消耗倍率（原版配置 key 是 overthrowerVisMult，语义就是倍率）。 */
    public static final ModConfigSpec.DoubleValue EDICT_OF_BANISHMENT_VIS_MULT;
    /** 永恒放逐之诫：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_MAX_CHARGE;
    /** 永恒放逐之诫：完整引导时长（tick，原版 150）。 */
    public static final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_CHANNEL_DURATION;
    /** 永恒放逐之诫：附带的扭曲值（原版 {@code getWarp} 返回 2）。 */
    public static final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_WARP;

    // ---- 深渊魔典 / Grimoire of The Abyss ----
    /** 深渊魔典：引导时每秒的 Vis 基础消耗（原版每 tick 秩序 9 + 混沌 16 = 25 厘 = 5 点/秒）。 */
    public static final ModConfigSpec.IntValue VOID_GRIMOIRE_VIS_COST;
    /** 深渊魔典：Vis 消耗倍率（原版配置 key 就是 voidGrimoireVisMult，默认 1.0）。 */
    public static final ModConfigSpec.DoubleValue VOID_GRIMOIRE_VIS_MULT;
    /** 深渊魔典：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue VOID_GRIMOIRE_MAX_CHARGE;
    /** 深渊魔典：完整引导时长（tick，原版 getMaxItemUseDuration 返回 100）。 */
    public static final ModConfigSpec.IntValue VOID_GRIMOIRE_CHANNEL_DURATION;
    /** 深渊魔典：引导结束后的共用冷却（tick，原版 setCasted 30）。 */
    public static final ModConfigSpec.IntValue VOID_GRIMOIRE_COOLDOWN;
    /** 深渊魔典：附带的扭曲值（原版 {@code getWarp} 返回 3）。 */
    public static final ModConfigSpec.IntValue VOID_GRIMOIRE_WARP;

    // ---- 预言之典 / Tome of Predestiny ----
    /** 预言之典：Vis 消耗倍率（原版配置 key 是 telekinesisTomeVisCost，语义却是倍率）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_PREDESTINY_VIS_MULT;
    /** 预言之典：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue TOME_OF_PREDESTINY_MAX_CHARGE;
    /** 预言之典：念力引导每秒的 Vis 基础消耗（原版每 tick 风 6 + 秩序 8 = 14 厘，即 2.8 点/秒）。 */
    public static final ModConfigSpec.IntValue TOME_OF_PREDESTINY_CONTROL_VIS_COST;
    /** 预言之典：闪电攻击的 Vis 基础消耗（原版风 80 + 秩序 50 + 火 200 = 330 厘，即 3.3 点）。 */
    public static final ModConfigSpec.IntValue TOME_OF_PREDESTINY_LIGHTNING_VIS_COST;
    /** 预言之典：潜行 + 左键「抛开」的 Vis 基础消耗（原版风 150 + 秩序 80 = 230 厘，即 2.3 点）。 */
    public static final ModConfigSpec.IntValue TOME_OF_PREDESTINY_SHOVE_VIS_COST;
    /** 预言之典：一次左键攻击后的共用冷却（tick，原版 setCasted 10）。 */
    public static final ModConfigSpec.IntValue TOME_OF_PREDESTINY_COOLDOWN;
    /** 预言之典：「抛开」后不能继续念力控制的时长（tick，原版把 ticksCooldown 写成 40）。 */
    public static final ModConfigSpec.IntValue TOME_OF_PREDESTINY_SHOVE_COOLDOWN;
    /** 预言之典：闪电攻击伤害下限（原版 telekinesisTomeDamageMIN，默认 16）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_PREDESTINY_DAMAGE_MIN;
    /** 预言之典：闪电攻击伤害上限（原版 telekinesisTomeDamageMAX，默认 40）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_PREDESTINY_DAMAGE_MAX;
    /** 预言之典：附带的扭曲值（原版 {@code getWarp} 返回 4）。 */
    public static final ModConfigSpec.IntValue TOME_OF_PREDESTINY_WARP;

    // ---- 月耀咒书 / Tome of Lunar Flares ----
    /** 月耀咒书：每发一颗耀月之辉的 Vis 基础消耗（原版 35 + 50 + 65 = 150 厘 = 1.5 点，就近取 2）。 */
    public static final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_VIS_COST;
    /** 月耀咒书：Vis 消耗倍率（原版配置 key 是 lunarFlaresVisCost，语义就是倍率）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_LUNAR_FLARES_VIS_MULT;
    /** 月耀咒书：最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_MAX_CHARGE;
    /** 月耀咒书：连续引导时两次发射之间的间隔（tick），原版硬编码为 2。 */
    public static final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_COOLDOWN;
    /** 月耀咒书：耀月之辉直击命中的伤害（原版配置 damageLunarFlareDirect，默认 72）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_LUNAR_FLARES_DIRECT_DAMAGE;
    /** 月耀咒书：耀月之辉爆发的范围伤害（原版配置 damageLunarFlareImpact，默认 40）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_LUNAR_FLARES_IMPACT_DAMAGE;
    /** 月耀咒书：附带的扭曲值（原版 {@code getWarp} 返回 3）。 */
    public static final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_WARP;

    // ---- 神化 / Apotheosis ----
    /** 神化：每次召唤的 Vis 基础消耗（原版地 30 + 火 60 + 秩序 50 + 混沌 75 = 215 厘 = 2.15 点，就近取 2）。 */
    public static final ModConfigSpec.IntValue APOTHEOSIS_VIS_COST;
    /** 神化：Vis 消耗倍率（原版配置 key 就是 apotheosisVisMult，默认 1.0）。 */
    public static final ModConfigSpec.DoubleValue APOTHEOSIS_VIS_MULT;
    /** 神化：最大 Vis 储量（按满法杖的混沌瓶颈 133 次召唤 × 2 取整到 300）。 */
    public static final ModConfigSpec.IntValue APOTHEOSIS_MAX_CHARGE;
    /**
     * 神化：连续引导时两次召唤之间的间隔（tick）。
     *
     * <p>原版在 {@code onUsingTick} 里写的是 {@code count % 2 == 0}，物品本身<b>没有任何施法后冷却</b>，
     * 所以这一项不接 {@code CooldownHelper}（与月耀咒书同一处理）。
     */
    public static final ModConfigSpec.IntValue APOTHEOSIS_COOLDOWN;
    /** 神化：巴比伦武器直击的伤害（原版配置 damageApotheosisDirect，默认 100）。 */
    public static final ModConfigSpec.DoubleValue APOTHEOSIS_DIRECT_DAMAGE;
    /** 神化：巴比伦武器爆炸的范围伤害（原版配置 damageApotheosisImpact，默认 75）。 */
    public static final ModConfigSpec.DoubleValue APOTHEOSIS_IMPACT_DAMAGE;
    /** 神化：附带的扭曲值（原版 {@code getWarp} 返回 5）。 */
    public static final ModConfigSpec.IntValue APOTHEOSIS_WARP;

    // ---- 破碎的命运巨著 / Tome of Broken Fates ----
    /** 破碎的命运巨著：每次免死的 Vis 基础消耗（原版六大原初要素各 10000 厘 = 100 点，合计 600 点）。 */
    public static final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_VIS_COST;
    /** 破碎的命运巨著：Vis 消耗倍率（原版配置 key 是 fateTomeVisMult，默认 1.0）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_VIS_MULT;
    /** 破碎的命运巨著：最大 Vis 储量（沿用 1.12.2 移植版的 fateTomeMaxCharge，默认 600）。 */
    public static final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_MAX_CHARGE;
    /** 破碎的命运巨著：免死冷却下限（秒，原版 fateTomeCooldownMIN，默认 30）。 */
    public static final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_COOLDOWN_MIN;
    /** 破碎的命运巨著：免死冷却上限（秒，原版 fateTomeCooldownMAX，默认 90；设为 0 即完全关闭冷却）。 */
    public static final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_COOLDOWN_MAX;
    /** 破碎的命运巨著：同时携带多本时每 tick 引爆的概率（原版 fateTomeMultiHeldChance，默认 1.6E-5）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_MULTI_HELD_CHANCE;
    /** 破碎的命运巨著：自毁惩罚扫描活体的半径（原版硬编码 ±64 格）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_MULTI_HELD_RANGE;
    /** 破碎的命运巨著：自毁惩罚对每个活体造成的命运伤害（原版 fateTomeDamage，默认 40000.0）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_DAMAGE;
    /** 破碎的命运巨著：自毁惩罚中每个活体处的爆炸半径（原版 fateTomeExplosionRadius，默认 16.0）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_EXPLOSION_RADIUS;
    /** 破碎的命运巨著：自毁惩罚结束时携带者处的大爆炸半径（原版 fateTomeBigExplosionRadius，默认 100.0）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_BIG_EXPLOSION_RADIUS;
    /** 破碎的命运巨著：免死时施加增益（而非减益）的概率（原版 fateTomeBuffChance，默认 0.75）。 */
    public static final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_BUFF_CHANCE;
    /** 破碎的命运巨著：附带的扭曲值（原版 {@code getWarp} 返回 7，全模组第二高）。 */
    public static final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_WARP;

    // ---- 湮灭之钥 / Keystone of The Oblivion ----
    /** 湮灭之钥：可绑定条目数的硬上限（原版 {@code oblivionStoneHardCap}，默认 64）。 */
    public static final ModConfigSpec.IntValue OBLIVION_STONE_HARD_CAP;
    /** 湮灭之钥：Ctrl 清单全量展开的条数上限（原版 {@code oblivionStoneSoftCap}，默认 28）。 */
    public static final ModConfigSpec.IntValue OBLIVION_STONE_SOFT_CAP;
    /** 湮灭之钥：附带的扭曲值（原版 {@code getWarp} 返回 2）。 */
    public static final ModConfigSpec.IntValue OBLIVION_STONE_WARP;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("通用配置（原版分类 Generic Config）").push("generic");
        MINING_CHARM_BOOST = builder
                .comment("Mining speed boost for Mining Charm. 1.0 means that it is boosted by 100%.",
                         "采矿护符的挖掘速度加成倍率。1.0 表示提升 100%。")
                .defineInRange("miningCharmBoost", 1.0D, 0.0D, 32000.0D);
        MINING_CHARM_REACH = builder
                .comment("Block reach increase for Mining Charm.",
                         "采矿护符增加的方块触及距离。")
                .defineInRange("miningCharmReach", 2.0D, 0.0D, 32.0D);
        ADVANCED_MINING_CHARM_BOOST = builder
                .comment("Mining speed boost for Ethereal Mining Charm. 3.0 means that it is boosted by 300%.",
                         "以太采矿护符的挖掘速度加成倍率。3.0 表示提升 300%。")
                .defineInRange("advancedMiningCharmBoost", 3.0D, 0.0D, 32000.0D);
        ADVANCED_MINING_CHARM_REACH = builder
                .comment("Block reach increase for Ethereal Mining Charm.",
                         "以太采矿护符增加的方块触及距离。")
                .defineInRange("advancedMiningCharmReach", 4.0D, 0.0D, 32.0D);
        INTERDIMENSIONAL_MIRROR = builder
                .comment("Whether or not Dimensional Mirror should be capable of teleporting player across dimensions.",
                         "空间魔镜是否允许跨维度传送。设为 false 时，玩家必须身处目标维度才能传送过去。")
                .define("interdimensionalMirror", true);
        builder.pop();

        builder.comment("叠加之戒（原版分类 Superposition Ring）").push("superposition_ring");
        SUPERPOSITION_RING_SWAP_CHANCE = builder
                .comment("Chance per check for Ring of Superposition to swap positions.",
                         "叠加之戒每次判定时的位置交换概率。")
                .defineInRange("superpositionRingSwapChance", 0.025D, 0.0D, 1.0D);
        SUPERPOSITION_RING_CHECK_INTERVAL = builder
                .comment("Check interval in ticks for Ring of Superposition.",
                         "叠加之戒的交换判定间隔（tick）。")
                .defineInRange("superpositionRingCheckInterval", 600, 1, 32768);
        SUPERPOSITION_RING_SPLIT_MIN = builder
                .comment("Lower bound of the fraction of incoming damage split among other wearers.",
                         "受到伤害时，分摊给其他佩戴者的比例下限。原版是 0.12。")
                .defineInRange("superpositionRingSplitMin", 0.12D, 0.0D, 1.0D);
        SUPERPOSITION_RING_SPLIT_MAX = builder
                .comment("Upper bound of the fraction of incoming damage split among other wearers.",
                         "分摊比例的上限。原版是 0.74（即 0.12 + 随机 0.62）。")
                .defineInRange("superpositionRingSplitMax", 0.74D, 0.0D, 1.0D);
        builder.pop();

        builder.comment("经验之书（原版分类 XP Tome）").push("xp_tome");
        XP_TOME_TRANSFER_RATE = builder
                .comment("Experience points transferred per tick by Tome of Experience.",
                         "经验之书每 tick 转移的经验点数。")
                .defineInRange("xpTomeTransferRate", 5, 1, 32768);
        builder.pop();

        builder.comment("符文天象石（原版分类 Weather Stone）").push("weather_stone");
        WEATHER_STONE_VIS_COST = builder
                .comment("Base Vis cost per cast for Runic Stone.",
                         "符文天象石每次施法的 Vis 基础消耗。")
                .defineInRange("weatherStoneVisCost", 25, 0, 32768);
        WEATHER_STONE_VIS_MULT = builder
                .comment("Vis cost multiplier for Runic Stone.",
                         "符文天象石的 Vis 消耗倍率。")
                .defineInRange("weatherStoneVisMult", 1.0D, 0.0D, 1024.0D);
        WEATHER_STONE_CHANNEL_DURATION = builder
                .comment("Channel duration in ticks for Runic Stone.",
                         "符文天象石的引导时长（tick）。")
                .defineInRange("weatherStoneChannelDuration", 60, 1, 32768);
        WEATHER_STONE_COOLDOWN = builder
                .comment("Cooldown in ticks after using Runic Stone.",
                         "符文天象石使用后的冷却（tick）。")
                .defineInRange("weatherStoneCooldown", 100, 0, 32768);
        builder.pop();

        builder.comment("空间魔镜（原版分类 Dimensional Mirror）").push("dimensional_mirror");
        DIMENSIONAL_MIRROR_CHANNEL_DURATION = builder
                .comment("Channel duration in ticks for Dimensional Mirror.",
                         "空间魔镜的引导时长（tick）。")
                .defineInRange("dimensionalMirrorChannelDuration", 80, 1, 32768);
        builder.pop();

        // 原版没有这一项：1.12.2 各处音量是写死的，最响的几处到了 8.0F，听感很吵。
        // 这里新增一个总开关，把所有音效（含以后补的物品）统一压低。
        builder.comment("音效（原版无此配置，为本模组新增）").push("sound");
        SOUND_VOLUME_MULTIPLIER = builder
                .comment("Global volume multiplier applied to every Forgotten Relics sound effect.",
                         "本模组所有音效的音量倍率。原版音量偏大，1.6.2 玩家实测仍嫌响，默认降到 40%；",
                         "设为 1.0 即恢复原版音量。注意：真实响度 = clamp(原始音量,0,1) * 本倍率 * 玩家音量滑条。")
                .defineInRange("soundVolumeMultiplier", 0.4D, 0.0D, 1.0D);
        builder.pop();

        builder.comment("Vis 储量上限（原版分类 Vis）").push("vis");
        WEATHER_STONE_MAX_CHARGE = builder
                .comment("Max Vis charge for Runic Stone.", "符文天象石的最大 Vis 储量。")
                .defineInRange("weatherStoneMaxCharge", 100, 0, 32768);
        DIMENSIONAL_MIRROR_MAX_CHARGE = builder
                .comment("Max Vis charge for Dimensional Mirror.", "空间魔镜的最大 Vis 储量。")
                .defineInRange("dimensionalMirrorMaxCharge", 100, 0, 32768);
        builder.pop();

        builder.comment("远古之庇护（原版分类 Generic Config / Ancient Aegis）").push("ancient_aegis");
        ANCIENT_AEGIS_DAMAGE_REDUCTION = builder
                .comment("Damage reduction for Ancient Aegis. 1.0 equals 100% reduction.",
                         "远古之庇护为佩戴者提供的伤害减免比例。0.25 表示 25%。")
                .defineInRange("ancientAegisDamageReduction", 0.25D, 0.0D, 1.0D);
        ANCIENT_AEGIS_HEAL_AMOUNT = builder
                .comment("Heal amount per interval for Ancient Aegis.",
                         "远古之庇护每次治疗回复的生命值。")
                .defineInRange("ancientAegisHealAmount", 1.0D, 0.0D, 32768.0D);
        ANCIENT_AEGIS_HEAL_INTERVAL = builder
                .comment("Heal interval in ticks for Ancient Aegis.",
                         "远古之庇护的治疗判定间隔（tick）。")
                .defineInRange("ancientAegisHealInterval", 20, 1, 32768);
        ANCIENT_AEGIS_KNOCKBACK_RESISTANCE = builder
                .comment("Knockback resistance granted by Ancient Aegis.",
                         "远古之庇护提供的击退抗性。1.0 表示完全免疫击退。")
                .defineInRange("ancientAegisKnockbackResistance", 1.0D, 0.0D, 1.0D);
        builder.pop();

        builder.comment("七阳之戒（原版分类 Generic Config / Dark Sun Ring）").push("dark_sun_ring");
        DARK_SUN_RING_DAMAGE_CAP = builder
                .comment("Damage cap for Ring of The Seven Suns. Attacks exceeding this are negated.",
                         "七阳之戒的伤害上限。超过该数值的攻击会被完全抵消。")
                .defineInRange("darkSunRingDamageCap", 100.0D, 0.0D, 32768.0D);
        DARK_SUN_RING_DEFLECT_CHANCE = builder
                .comment("Chance to deflect an attack back to its source.",
                         "把攻击反弹给攻击者的概率。")
                .defineInRange("darkSunRingDeflectChance", 0.2D, 0.0D, 1.0D);
        DARK_SUN_RING_HEAL_LIMIT = builder
                .comment("Enables a cooldown on the fire-damage-to-healing conversion.",
                         "是否给「火焰伤害转化为治疗」加上冷却限制，避免站在火里瞬间回满血。")
                .define("darkSunRingHealLimit", false);
        DARK_SUN_RING_MAX_CHARGE = builder
                .comment("Max Vis charge for Ring of The Seven Suns.", "七阳之戒的最大 Vis 储量。")
                .defineInRange("darkSunRingMaxCharge", 500, 0, 32768);
        builder.pop();

        builder.comment("神圣护身符（原版分类 Deific Amulet）").push("deific_amulet");
        DEIFIC_AMULET_EFFECT_IMMUNITY = builder
                .comment("Whether Deific Amulet grants immunity to status effects.",
                         "神圣护身符是否免疫状态效果。")
                .define("deificAmuletEffectImmunity", true);
        DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS = builder
                .comment("When true, only debuffs are removed and buffs are kept.",
                         "开启后只清除减益效果，保留增益效果。")
                .define("deificAmuletOnlyNegatesDebuffs", false);
        DEIFIC_AMULET_INVINCIBILITY = builder
                .comment("Whether Deific Amulet extends invincibility frames.",
                         "神圣护身符是否延长无敌帧。")
                .define("deificAmuletInvincibility", true);
        DEIFIC_AMULET_INVINCIBILITY_EXTENSION = builder
                .comment("Invincibility frame extension in ticks.",
                         "延长后的无敌帧时长（tick）。")
                .defineInRange("deificAmuletInvincibilityExtension", 40, 0, 32768);
        DEIFIC_AMULET_INVINCIBILITY_COOLDOWN = builder
                .comment("Cooldown in ticks for the invincibility frame extension.",
                         "无敌帧延长效果的冷却（tick）。")
                .defineInRange("deificAmuletInvincibilityCooldown", 32, 0, 32768);
        DEIFIC_AMULET_AIR_SUPPLY = builder
                .comment("Air supply in ticks restored when the wearer is suffocating.",
                         "佩戴者窒息时补充的氧气量（tick），原版换算自 deificAmuletFireDuration。")
                .defineInRange("deificAmuletAirSupply", 300, 0, 32768);
        DEIFIC_AMULET_VIS_COST = builder
                .comment("Base Vis cost for Deific Amulet suffocation prevention.",
                         "神圣护身符补充氧气的 Vis 基础消耗。")
                .defineInRange("deificAmuletFireVisCost", 10, 0, 32768);
        DEIFIC_AMULET_VIS_MULT = builder
                .comment("Vis cost multiplier for Deific Amulet.", "神圣护身符的 Vis 消耗倍率。")
                .defineInRange("deificAmuletVisCost", 1.0D, 0.0D, 1024.0D);
        DEIFIC_AMULET_MAX_CHARGE = builder
                .comment("Max Vis charge for Deific Amulet.", "神圣护身符的最大 Vis 储量。")
                .defineInRange("deificAmuletMaxCharge", 200, 0, 32768);
        builder.pop();

        builder.comment("湮灭护符（原版分类 Oblivion Amulet）").push("oblivion_amulet");
        OBLIVION_AMULET_DAMAGE_RELEASE_CHANCE = builder
                .comment("Chance per tick to release stored damage.",
                         "湮灭护符每 tick 释放已储存伤害的概率。")
                .defineInRange("oblivionAmuletDamageReleaseChance", 0.0008D, 0.0D, 1.0D);
        OBLIVION_AMULET_DAMAGE_CAP = builder
                .comment("Upper bound for a single damage release.",
                         "单次释放伤害的上限。")
                .defineInRange("oblivionAmuletDamageCap", 100.0D, 0.0D, 32768.0D);
        OBLIVION_AMULET_HIGH_DAMAGE_REDUCTION_CHANCE = builder
                .comment("Chance to clamp an over-cap release to a random value below the cap.",
                         "释放伤害超过上限时，改为按上限随机取值的概率。")
                .defineInRange("oblivionAmuletHighDamageReductionChance", 0.9D, 0.0D, 1.0D);
        OBLIVION_AMULET_POTION_CHANCE = builder
                .comment("Chance per tick to apply a random debuff.",
                         "湮灭护符每 tick 施加随机负面效果的概率。")
                .defineInRange("oblivionAmuletPotionChance", 0.0004D, 0.0D, 1.0D);
        OBLIVION_AMULET_POTION_DURATION_MIN = builder
                .comment("Minimum duration of the random debuff.", "随机负面效果的最短持续时间（tick）。")
                .defineInRange("oblivionAmuletPotionDurationMin", 100, 0, 32768);
        OBLIVION_AMULET_POTION_DURATION_MAX = builder
                .comment("Maximum duration of the random debuff.", "随机负面效果的最长持续时间（tick）。")
                .defineInRange("oblivionAmuletPotionDurationMax", 2100, 0, 32768);
        OBLIVION_AMULET_POTION_LEVEL_MIN = builder
                .comment("Minimum amplifier of the random debuff.", "随机负面效果的最低等级（0 表示 I 级）。")
                .defineInRange("oblivionAmuletPotionLevelMin", 0, 0, 255);
        OBLIVION_AMULET_POTION_LEVEL_MAX = builder
                .comment("Maximum amplifier of the random debuff.", "随机负面效果的最高等级。")
                .defineInRange("oblivionAmuletPotionLevelMax", 3, 0, 255);
        OBLIVION_AMULET_WARP = builder
                .comment("Warp granted by Amulet of The Oblivion.", "湮灭护符附带的扭曲值。")
                .defineInRange("oblivionAmuletWarp", 4, 0, 32768);
        OBLIVION_AMULET_VIS_MULT = builder
                .comment("Vis cost multiplier for Amulet of The Oblivion.", "湮灭护符储存伤害的 Vis 消耗倍率。")
                .defineInRange("oblivionAmuletVisCost", 1.0D, 0.0D, 1024.0D);
        OBLIVION_AMULET_MAX_CHARGE = builder
                .comment("Max Vis charge for Amulet of The Oblivion.", "湮灭护符的最大 Vis 储量。")
                .defineInRange("oblivionAmuletMaxCharge", 400, 0, 32768);
        builder.pop();

        builder.comment("日耀石（原版分类 Shiny Stone）").push("shiny_stone");
        SHINY_STONE_CHECK_RATE = builder
                .comment("Interval in ticks between stillness checks.",
                         "日耀石的静止判定间隔（tick）。")
                .defineInRange("shinyStoneCheckrate", 4, 1, 32768);
        SHINY_STONE_THRESHOLD_2 = builder
                .comment("Still-tick threshold for the second heal rate.",
                         "静止累计达到该值时进入第 2 档回血速度。")
                .defineInRange("shinyStoneStillThreshold2", 40, 0, 32768);
        SHINY_STONE_THRESHOLD_3 = builder
                .comment("Still-tick threshold for the third heal rate.",
                         "静止累计达到该值时进入第 3 档回血速度。")
                .defineInRange("shinyStoneStillThreshold3", 80, 0, 32768);
        SHINY_STONE_THRESHOLD_4 = builder
                .comment("Still-tick threshold for the fourth heal rate.",
                         "静止累计达到该值时进入第 4 档回血速度。")
                .defineInRange("shinyStoneStillThreshold4", 200, 0, 32768);
        SHINY_STONE_STILL_INCREMENT = builder
                .comment("Increment added to the still counter on each successful check.",
                         "每次判定静止时，累计值的增量。")
                .defineInRange("shinyStoneStillIncrement", 4, 0, 32768);
        SHINY_STONE_HEAL_AMOUNT = builder
                .comment("Health restored per heal tick.", "每次回血回复的生命值。")
                .defineInRange("shinyStoneHealAmount", 1.0D, 0.0D, 32768.0D);
        builder.pop();

        builder.comment("浑浊之核与休眠态（原版分类 Nebulous Core）").push("arcanum");
        ARCANUM_GEN_RATE = builder
                .comment("Multiplier applied to the 2.5% per-tick chance of generating Vis.",
                         "浑浊之核被动生成 Vis 的概率倍率（基础概率 2.5%）。")
                .defineInRange("arcanumGenRate", 1.0D, 0.0D, 1024.0D);
        ARCANUM_TELEPORT_CHANCE = builder
                .comment("Chance per tick to teleport the wearer at random.",
                         "浑浊之核每 tick 随机传送佩戴者的概率。")
                .defineInRange("arcanumTeleportChance", 0.000208D, 0.0D, 1.0D);
        ARCANUM_TELEPORT_RANGE = builder
                .comment("Maximum teleport distance in blocks.", "随机传送的最大距离（格）。")
                .defineInRange("arcanumTeleportRange", 32, 1, 256);
        ARCANUM_DORMANT_TRANSFORM_CHANCE = builder
                .comment("Chance per tick to fall dormant.",
                         "浑浊之核每 tick 转化为休眠态的概率。")
                .defineInRange("arcanumDormantTransformChance", 0.000027D, 0.0D, 1.0D);
        ARCANUM_DORMANT_LIFE_MIN = builder
                .comment("Minimum dormant lifetime.", "休眠态寿命的最小值。")
                .defineInRange("arcanumDormantLifeMin", 12, 0, 32768);
        ARCANUM_DORMANT_LIFE_MAX = builder
                .comment("Maximum dormant lifetime.", "休眠态寿命的最大值。")
                .defineInRange("arcanumDormantLifeMax", 72, 0, 32768);
        ARCANUM_VIS_DISCOUNT = builder
                .comment("Vis discount percentage granted by Nebulous Core.",
                         "浑浊之核提供的 Vis 折扣（百分比）。")
                .defineInRange("arcanumVisDiscount", 35.0D, 0.0D, 100.0D);
        ARCANUM_MAX_CHARGE = builder
                .comment("Max Vis charge for Nebulous Core.", "浑浊之核的最大 Vis 储量。")
                .defineInRange("arcanumMaxCharge", 500, 0, 32768);
        NEBULOUS_CORE_DODGE_CHANCE = builder
                .comment("Chance to dodge an incoming attack while wearing Nebulous Core.",
                         "佩戴浑浊之核时闪避一次攻击的概率。")
                .defineInRange("nebulousCoreDodgeChance", 0.4D, 0.0D, 1.0D);
        DORMANT_ARCANUM_VIS_COST_PER_TICK = builder
                .comment("Vis consumed per tick while dormant.",
                         "休眠浑浊之核每 tick 唤醒消耗的 Vis。")
                .defineInRange("dormantArcanumVisCostPerTick", 3, 0, 32768);
        DORMANT_ARCANUM_MAX_CHARGE = builder
                .comment("Max Vis charge for Dormant Nebulous Core.", "休眠浑浊之核的最大 Vis 储量。")
                .defineInRange("dormantArcanumMaxCharge", 300, 0, 32768);
        DORMANT_ARCANUM_VIS_MULT = builder
                .comment("Lifetime multiplier applied when falling dormant.",
                         "转化为休眠态时的寿命倍率。")
                .defineInRange("dormantArcanumVisMult", 1.0D, 0.0D, 1024.0D);
        builder.pop();

        builder.comment("恐惧之冠（原版分类 Crown of Terror）").push("terror_crown");
        TERROR_CROWN_HAVOC_RANGE = builder
                .comment("Radius in blocks for Crown of Terror to turn mobs against each other.",
                         "恐惧之冠挑拨怪物互相攻击的作用半径（格）。")
                .defineInRange("terrorCrownHavocRange", 24, 0, 256);
        TERROR_CROWN_SCAN_RANGE = builder
                .comment("Maximum distance in blocks for the gaze debuff.",
                         "恐惧之冠注视目标的最大距离（格）。")
                .defineInRange("terrorCrownScanRange", 32.0D, 0.0D, 256.0D);
        TERROR_CROWN_BLINDNESS_DURATION = builder
                .comment("Blindness duration in ticks.", "注视目标的失明持续时间（tick）。")
                .defineInRange("terrorCrownBlindnessDuration", 100, 0, 32768);
        TERROR_CROWN_WITHER_DURATION = builder
                .comment("Wither duration in ticks.", "凋零持续时间（tick）。")
                .defineInRange("terrorCrownWitherDuration", 40, 0, 32768);
        TERROR_CROWN_WITHER_LEVEL = builder
                .comment("Wither amplifier.", "凋零等级。")
                .defineInRange("terrorCrownWitherLevel", 0, 0, 255);
        TERROR_CROWN_NAUSEA_DURATION = builder
                .comment("Nausea duration in ticks.", "反胃持续时间（tick）。")
                .defineInRange("terrorCrownNauseaDuration", 100, 0, 32768);
        TERROR_CROWN_NAUSEA_LEVEL = builder
                .comment("Nausea amplifier.", "反胃等级。")
                .defineInRange("terrorCrownNauseaLevel", 1, 0, 255);
        TERROR_CROWN_SLOWNESS_DURATION = builder
                .comment("Slowness duration in ticks.", "缓慢持续时间（tick）。")
                .defineInRange("terrorCrownSlownessDuration", 30, 0, 32768);
        TERROR_CROWN_SLOWNESS_LEVEL = builder
                .comment("Slowness amplifier.", "缓慢等级。")
                .defineInRange("terrorCrownSlownessLevel", 1, 0, 255);
        TERROR_CROWN_WEAKNESS_DURATION = builder
                .comment("Weakness duration in ticks.", "虚弱持续时间（tick）。")
                .defineInRange("terrorCrownWeaknessDuration", 80, 0, 32768);
        TERROR_CROWN_WEAKNESS_LEVEL = builder
                .comment("Weakness amplifier.", "虚弱等级。")
                .defineInRange("terrorCrownWeaknessLevel", 2, 0, 255);
        TERROR_CROWN_MANA_COST = builder
                .comment("Mana consumed per point of durability repaired.",
                         "恐惧之冠每修复 1 点耐久消耗的魔力。")
                .defineInRange("terrorCrownManaCost", 200, 0, 32768);
        TERROR_CROWN_WARP = builder
                .comment("Warp granted by Crown of Terror.", "恐惧之冠附带的扭曲值。")
                .defineInRange("terrorCrownWarp", 3, 0, 32768);
        builder.pop();

        builder.comment("食尸鬼之颅（原创补完物品：1.7.10 原版只有未写完的骨架）").push("ghastly_skull");
        GHASTLY_SKULL_MIN_HEALTH = builder
                .comment("Minimum health required to sacrifice. Below this the skull refuses to fire.",
                         "允许献祭所需的最低生命值，低于此值则拒绝发动（避免变成自杀）。")
                .defineInRange("ghastlySkullMinHealth", 6, 2, 32768);
        GHASTLY_SKULL_BURST_RANGE = builder
                .comment("Maximum distance in blocks the spectral burst reaches along the line of sight.",
                         "怨魂冲击沿视线能到达的最大距离（格）。原版那段残代码里写死的是 16。")
                .defineInRange("ghastlySkullBurstRange", 16, 1, 256);
        GHASTLY_SKULL_BURST_RADIUS = builder
                .comment("Radius in blocks of the spectral burst.", "怨魂冲击的作用半径（格）。")
                .defineInRange("ghastlySkullBurstRadius", 5.0D, 0.0D, 64.0D);
        GHASTLY_SKULL_DAMAGE_MULT = builder
                .comment("Damage dealt per point of sacrificed health.", "每献祭 1 点生命造成的伤害。")
                .defineInRange("ghastlySkullDamageMult", 1.5D, 0.0D, 1024.0D);
        GHASTLY_SKULL_MAX_DAMAGE = builder
                .comment("Upper limit of a single burst's damage.", "单次怨魂冲击的伤害上限。")
                .defineInRange("ghastlySkullMaxDamage", 30.0D, 0.0D, 32768.0D);
        GHASTLY_SKULL_HEAL_PER_TARGET = builder
                .comment("Health restored per entity hit. The total never exceeds the health sacrificed.",
                         "每命中一个目标回复的生命值；总量不会超过献祭掉的生命。")
                .defineInRange("ghastlySkullHealPerTarget", 2.0D, 0.0D, 32768.0D);
        GHASTLY_SKULL_WITHER_DURATION = builder
                .comment("Wither duration in ticks applied to hit entities.", "命中目标的凋零持续时间（tick）。")
                .defineInRange("ghastlySkullWitherDuration", 100, 0, 32768);
        GHASTLY_SKULL_WITHER_LEVEL = builder
                .comment("Wither amplifier applied to hit entities.", "命中目标的凋零等级。")
                .defineInRange("ghastlySkullWitherLevel", 1, 0, 255);
        GHASTLY_SKULL_VIS_COST = builder
                .comment("Vis consumed per use.", "每次发动消耗的 Vis。")
                .defineInRange("ghastlySkullVisCost", 100, 0, 32768);
        GHASTLY_SKULL_MAX_CHARGE = builder
                .comment("Max Vis charge for the Ghastly Skull.", "食尸鬼之颅的最大 Vis 储量。")
                .defineInRange("ghastlySkullMaxCharge", 300, 0, 32768);
        GHASTLY_SKULL_COOLDOWN = builder
                .comment("Cooldown in ticks after each use.", "每次发动后的冷却（tick）。")
                .defineInRange("ghastlySkullCooldown", 200, 0, 32768);
        GHASTLY_SKULL_WARP = builder
                .comment("Warp granted by the Ghastly Skull.", "食尸鬼之颅附带的扭曲值。")
                .defineInRange("ghastlySkullWarp", 3, 0, 32768);
        builder.pop();

        builder.comment("混沌之核（第四波：核心类）").push("chaos_core");
        CHAOS_CORE_CHANCE = builder
                .comment("Chance per tick to apply a random potion effect while carried.",
                         "随身携带时，每 tick 施加随机状态效果的概率。原版写死 2.08E-4。")
                .defineInRange("chaosCoreChance", 2.08E-4D, 0.0D, 1.0D);
        CHAOS_CORE_DURATION_MIN = builder
                .comment("Minimum effect duration in ticks.", "状态效果的最短持续时间（tick）。原版是 100。")
                .defineInRange("chaosCoreDurationMin", 100, 0, 32768);
        CHAOS_CORE_DURATION_SPAN = builder
                .comment("Random span added on top of the minimum duration, in ticks.",
                         "在最短持续时间之上再随机叠加的区间长度（tick）。原版是 2400。")
                .defineInRange("chaosCoreDurationSpan", 2400, 0, 32768);
        CHAOS_CORE_MAX_AMPLIFIER = builder
                .comment("Highest amplifier that can be rolled (0 means level I).",
                         "能随机到的最高等级（0 即 I 级）。原版是 0~2。")
                .defineInRange("chaosCoreMaxAmplifier", 2, 0, 255);
        CHAOS_CORE_WARP = builder
                .comment("Warp granted by the Chaos Core.", "混沌之核附带的扭曲值。原版是 2。")
                .defineInRange("chaosCoreWarp", 2, 0, 32768);
        CHAOS_CORE_REDIRECT_ATTACK = builder
                .comment("Chance to redirect damage when the carrier is the attacker.",
                         "携带者是攻击者时，把伤害转嫁给 16 格内随机实体的概率。原版是 0.45。")
                .defineInRange("chaosCoreRedirectAttack", 0.45D, 0.0D, 1.0D);
        CHAOS_CORE_REDIRECT_DEFEND = builder
                .comment("Chance to redirect damage when the carrier is the victim.",
                         "携带者是受害者时，把伤害转嫁给 16 格内随机实体的概率。原版是 0.42。")
                .defineInRange("chaosCoreRedirectDefend", 0.42D, 0.0D, 1.0D);
        CHAOS_CORE_SELF_REFLECT = builder
                .comment("Chance for the redirected damage to bounce back onto the attacker instead.",
                         "转嫁时改为反弹给攻击者自己的概率。原版是 0.15。")
                .defineInRange("chaosCoreSelfReflect", 0.15D, 0.0D, 1.0D);
        CHAOS_CORE_DAMAGE_MULT_MAX = builder
                .comment("Upper bound of the random multiplier applied to redirected damage and incoming damage.",
                         "转嫁量与「受到的伤害随机系数」的上限倍率。原版写死 2.0（即 0~2 倍）。")
                .defineInRange("chaosCoreDamageMultMax", 2.0D, 0.0D, 1024.0D);
        builder.pop();

        builder.comment("欧米伽之核（第四波：原版无配方与研究的创造模式物品）").push("omega_core");
        OMEGA_CORE_VIS_PER_TICK = builder
                .comment("Vis added to each primal aspect of each wand per tick while carried.",
                         "随身携带时，每 tick 给法杖的每个原初要素补充的 Vis。原版是 1。")
                .defineInRange("omegaCoreVisPerTick", 1, 0, 32768);
        OMEGA_CORE_RECHARGE_PER_TICK = builder
                .comment("Charge added per tick to every Forgotten Relics rechargeable item the carrier holds.",
                         "随身携带时，每 tick 给身上每件遗物（物品自身充能）补充的点数。"
                                 + "1.21.1 没有玩家 Vis 池，遗物的 Vis 存在物品充能里，所以欧米伽之核必须同时给它们充能；"
                                 + "0 表示关闭这一支（只补法杖）。")
                .defineInRange("omegaCoreRechargePerTick", 1, 0, 32768);
        OMEGA_CORE_PREVENT_DEATH = builder
                .comment("Whether carrying the Omega Core cancels lethal damage and leaves the holder at 1 HP.",
                         "携带欧米伽之核时是否免死并把持有者留在 1 点生命。")
                .define("omegaCorePreventDeath", true);
        builder.pop();

        builder.comment("悖论之刃（第四波：武器）").push("paradox");
        PARADOX_DAMAGE_CAP = builder
                .comment("Upper bound of the Paradox's damage. The damage dealt to the target and the damage",
                         "dealt back to the wielder always add up to this value.",
                         "悖论之刃的伤害上限。目标所受与自身所受之和恒等于该值（原版默认 200）。")
                .defineInRange("paradoxDamageCap", 200.0D, 0.0D, 32000.0D);
        PARADOX_WARP = builder
                .comment("Warp granted by the Paradox.", "悖论之刃附带的扭曲值。原版是 8，全模组最高。")
                .defineInRange("paradoxWarp", 8, 0, 32768);
        builder.pop();

        builder.comment("虚伪审判（第四波：携带生效的转化与免死）").push("false_justice");
        FALSE_JUSTICE_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier applied when damage is converted into true damage.",
                         "伤害转化为真实伤害时的倍率。原版是 2.0。")
                .defineInRange("falseJusticeDamageMultiplier", 2.0D, 0.0D, 1024.0D);
        FALSE_JUSTICE_PREVENT_DEATH = builder
                .comment("Whether carrying False Justice prevents the carrier and their victims from dying.",
                         "携带虚伪审判时是否阻止死亡（携带者本人，以及被携带者打死的目标）。")
                .define("falseJusticePreventDeath", true);
        FALSE_JUSTICE_WARP = builder
                .comment("Warp granted by False Justice.", "虚伪审判附带的扭曲值。原版是 4。")
                .defineInRange("falseJusticeWarp", 4, 0, 32768);
        builder.pop();

        builder.comment("霹雳咒书（第六波：弹射物书籍）").push("thunderpeal");
        THUNDERPEAL_DIRECT_DAMAGE = builder
                .comment("Damage dealt to the entity the orb hits directly.",
                         "直接命中实体的伤害。原版 key 是 damageThunderpealDirect，默认 24。")
                .defineInRange("thunderpealDirectDamage", 24.0D, 0.0D, 32768.0D);
        THUNDERPEAL_BOLT_DAMAGE = builder
                .comment("Damage dealt to every entity in the blast, and halved again for chained targets.",
                         "范围伤害；链式闪电按它的一半结算。原版 key 是 damageThunderpealBolt，默认 16。")
                .defineInRange("thunderpealBoltDamage", 16.0D, 0.0D, 32768.0D);
        THUNDERPEAL_VIS_COST = builder
                .comment("Base Vis cost per cast for Thunderpeal.",
                         "霹雳咒书每次施法的 Vis 基础消耗。原版是风 1.35 + 火 0.85 = 2.2，充能为整数故取 2。")
                .defineInRange("thunderpealVisCost", 2, 0, 32768);
        THUNDERPEAL_VIS_MULT = builder
                .comment("Vis cost multiplier for Thunderpeal.",
                         "霹雳咒书的 Vis 消耗倍率。")
                .defineInRange("thunderpealVisMult", 1.0D, 0.0D, 1024.0D);
        THUNDERPEAL_MAX_CHARGE = builder
                .comment("Max Vis charge for Thunderpeal.", "霹雳咒书的最大 Vis 储量。")
                .defineInRange("thunderpealMaxCharge", 100, 0, 32768);
        THUNDERPEAL_COOLDOWN = builder
                .comment("Cooldown in ticks after casting Thunderpeal.",
                         "霹雳咒书使用后的冷却（tick）。原版是 30。")
                .defineInRange("thunderpealCooldown", 30, 0, 32768);
        builder.pop();

        builder.comment("错位之典（第七波：三种传送模式的法术典籍）").push("tome_of_discord");
        DISCORD_TOME_VIS_COST = builder
                .comment("Base Vis cost per cast for the Tome of Discord.",
                         "错位之典每次施法的 Vis 基础消耗。原版是风 1.60 + 秩序 2.40 + 混沌 2.40 = 6.4，充能为整数故取 6。")
                .defineInRange("discordTomeVisCost", 6, 0, 32768);
        DISCORD_TOME_VIS_MULT = builder
                .comment("Vis cost multiplier for the Tome of Discord.",
                         "错位之典的 Vis 消耗倍率。")
                .defineInRange("discordTomeVisMult", 1.0D, 0.0D, 1024.0D);
        DISCORD_TOME_MAX_CHARGE = builder
                .comment("Max Vis charge for the Tome of Discord.", "错位之典的最大 Vis 储量。")
                .defineInRange("discordTomeMaxCharge", 100, 0, 32768);
        DISCORD_TOME_COOLDOWN = builder
                .comment("Cooldown in ticks after casting the Tome of Discord.",
                         "错位之典使用后的冷却（tick）。原版是 20。")
                .defineInRange("discordTomeCooldown", 20, 0, 32768);
        builder.pop();

        builder.comment("虚空吞噬者（第七波：神秘方尖碑引导）").push("devourer_of_the_void");
        DEVOURER_OF_THE_VOID_VIS_MULT = builder
                .comment("Multiplier applied to the Vis the Devourer of The Void grants to wands. Original key: obeliskDrainerVisMult.",
                         "虚空吞噬者给背包法杖补 Vis 的倍率。原版配置 key 就是 obeliskDrainerVisMult，默认 1.0。")
                .defineInRange("devourerOfTheVoidVisMult", 1.0D, 0.0D, 1024.0D);
        DEVOURER_OF_THE_VOID_RANGE = builder
                .comment("Radius in blocks within which the Devourer of The Void can sense an Eldritch Obelisk.",
                         "虚空吞噬者能感知到神秘方尖碑的最大距离（格）。原版写死 16。")
                .defineInRange("devourerOfTheVoidRange", 16.0D, 1.0D, 128.0D);
        DEVOURER_OF_THE_VOID_PULSE_INTERVAL = builder
                .comment("Interval in ticks between two drain pulses.",
                         "虚空吞噬者两次抽取之间的间隔（tick）。原版写死 30。")
                .defineInRange("devourerOfTheVoidPulseInterval", 30, 1, 32768);
        DEVOURER_OF_THE_VOID_HEAL = builder
                .comment("Health restored per drain pulse.",
                         "虚空吞噬者每次抽取回复的生命值。原版写死 4.0。")
                .defineInRange("devourerOfTheVoidHeal", 4.0D, 0.0D, 32768.0D);
        DEVOURER_OF_THE_VOID_HUNGER = builder
                .comment("Hunger restored per drain pulse.",
                         "虚空吞噬者每次抽取补充的饥饿值。原版写死 2。")
                .defineInRange("devourerOfTheVoidHunger", 2, 0, 20);
        DEVOURER_OF_THE_VOID_WARP = builder
                .comment("Warp granted by the Devourer of The Void.",
                         "虚空吞噬者附带的扭曲值。原版 ItemObeliskDrainer#getWarp 返回 4。")
                .defineInRange("devourerOfTheVoidWarp", 4, 0, 32768);
        builder.pop();

        builder.comment("邪术之咒（第七波：暗物质法球）").push("eldritch_spell");
        ELDRITCH_SPELL_VIS_COST = builder
                .comment("Base Vis cost per cast for the Eldritch Spell.",
                         "邪术之咒每次施法的 Vis 基础消耗。原版是混沌（Perditio）400 厘 Vis，即 4 点。")
                .defineInRange("eldritchSpellVisCost", 4, 0, 32768);
        ELDRITCH_SPELL_VIS_MULT = builder
                .comment("Vis cost multiplier for the Eldritch Spell.",
                         "邪术之咒的 Vis 消耗倍率。")
                .defineInRange("eldritchSpellVisMult", 1.0D, 0.0D, 1024.0D);
        ELDRITCH_SPELL_MAX_CHARGE = builder
                .comment("Max Vis charge for the Eldritch Spell.", "邪术之咒的最大 Vis 储量。")
                .defineInRange("eldritchSpellMaxCharge", 100, 0, 32768);
        ELDRITCH_SPELL_COOLDOWN = builder
                .comment("Cooldown in ticks after casting the Eldritch Spell.",
                         "邪术之咒使用后的冷却（tick）。原版是 20。")
                .defineInRange("eldritchSpellCooldown", 20, 0, 32768);
        ELDRITCH_SPELL_DAMAGE = builder
                .comment("Damage dealt by the Dark Matter Orb outside the Outer Lands.",
                         "暗物质法球在普通维度造成的伤害。原版是 eldritchSpellDamage，默认 32.5。")
                .defineInRange("eldritchSpellDamage", 32.5D, 0.0D, 32768.0D);
        ELDRITCH_SPELL_DAMAGE_EX = builder
                .comment("Damage dealt by the Dark Matter Orb inside the Outer Lands.",
                         "暗物质法球在外域（Outer Lands）造成的伤害。原版是 eldritchSpellDamageEx，默认 100。")
                .defineInRange("eldritchSpellDamageEx", 100.0D, 0.0D, 32768.0D);
        builder.pop();

        builder.comment("腥红之咒（第七波：猩红法球）").push("crimson_spell");
        CRIMSON_SPELL_VIS_COST = builder
                .comment("Base Vis cost per cast for the Crimson Spell.",
                         "腥红之咒每次施法的 Vis 基础消耗。原版是火 4.8 + 混沌 3.6 = 8.4，充能为整数故取 8。")
                .defineInRange("crimsonSpellVisCost", 8, 0, 32768);
        CRIMSON_SPELL_VIS_MULT = builder
                .comment("Vis cost multiplier for the Crimson Spell.",
                         "腥红之咒的 Vis 消耗倍率。注意：原版此处的代码误乘了 chaosTomeVisMult，这里按配置本意使用本项。")
                .defineInRange("crimsonSpellVisMult", 1.0D, 0.0D, 1024.0D);
        CRIMSON_SPELL_MAX_CHARGE = builder
                .comment("Max Vis charge for the Crimson Spell.", "腥红之咒的最大 Vis 储量。")
                .defineInRange("crimsonSpellMaxCharge", 100, 0, 32768);
        CRIMSON_SPELL_COOLDOWN = builder
                .comment("Cooldown in ticks after casting the Crimson Spell.",
                         "腥红之咒使用后的冷却（tick）。原版是 30。")
                .defineInRange("crimsonSpellCooldown", 30, 0, 32768);
        CRIMSON_SPELL_DAMAGE_MIN = builder
                .comment("Minimal damage dealt by the Crimson Orbs.",
                         "猩红法球能造成的伤害下限。原版 key 是 crimsonSpellDamageMIN，默认 42。")
                .defineInRange("crimsonSpellDamageMIN", 42.0D, 0.0D, 32768.0D);
        CRIMSON_SPELL_DAMAGE_MAX = builder
                .comment("Maximal damage dealt by the Crimson Orbs.",
                         "猩红法球能造成的伤害上限。原版 key 是 crimsonSpellDamageMAX，默认 100。")
                .defineInRange("crimsonSpellDamageMAX", 100.0D, 0.0D, 32768.0D);
        CRIMSON_SPELL_WARP = builder
                .comment("Warp granted by the Crimson Spell.",
                         "腥红之咒附带的扭曲值。原版 ItemCrimsonSpell#getWarp 返回 3。")
                .defineInRange("crimsonSpellWarp", 3, 0, 32768);
        builder.pop();

        builder.comment("原初混沌之典（第七波：原初能量法球）").push("tome_of_primal_chaos");
        TOME_OF_PRIMAL_CHAOS_VIS_COST = builder
                .comment("Base Vis cost for spawning one Primal Orb.",
                         "每生成一颗原初能量法球的 Vis 基础消耗。原版是六大原初要素各随机抽 0~100 厘 Vis，"
                                 + "合计平均 300 厘 = 3 点，充能为整数故取 3。")
                .defineInRange("tomeOfPrimalChaosVisCost", 3, 0, 32768);
        TOME_OF_PRIMAL_CHAOS_VIS_MULT = builder
                .comment("Vis cost multiplier for the Tome of Primal Chaos.",
                         "原初混沌之典的 Vis 消耗倍率。原版对应 chaosTomeVisMult。")
                .defineInRange("tomeOfPrimalChaosVisMult", 1.0D, 0.0D, 1024.0D);
        TOME_OF_PRIMAL_CHAOS_MAX_CHARGE = builder
                .comment("Max Vis charge for the Tome of Primal Chaos.", "原初混沌之典的最大 Vis 储量。")
                .defineInRange("tomeOfPrimalChaosMaxCharge", 100, 0, 32768);
        TOME_OF_PRIMAL_CHAOS_COOLDOWN = builder
                .comment("Ticks between two Primal Orbs while channelling.",
                         "连续引导时两次生成法球的间隔（tick）。原版硬编码 count % 2 == 0，即 2；"
                                 + "这不是施法后的冷却（原版没有冷却）。")
                .defineInRange("tomeOfPrimalChaosCooldown", 2, 1, 32768);
        CHAOS_TOME_DAMAGE_CAP = builder
                .comment("Maximal damage dealt by Primal Orbs on hit (original chaosTomeDamageCap).",
                         "原初能量法球命中伤害的上限。原版 key 是 chaosTomeDamageCap，默认 100。")
                .defineInRange("chaosTomeDamageCap", 100.0D, 0.0D, 32768.0D);
        TOME_OF_PRIMAL_CHAOS_WARP = builder
                .comment("Warp granted by the Tome of Primal Chaos.",
                         "原初混沌之典附带的扭曲值。原版 ItemChaosTome#getWarp 返回 4。")
                .defineInRange("tomeOfPrimalChaosWarp", 4, 0, 32768);
        builder.pop();

        builder.comment("核子之怒（第六波：追踪导弹法书）").push("nuclear_fury");
        NUCLEAR_FURY_VIS_COST_PER_SECOND = builder
                .comment("Base Vis cost per second while channelling Nuclear Fury.",
                         "核子之怒每秒的 Vis 基础消耗。原版每颗法球从法杖抽火 0.20 + 秩序 0.10 + 混沌 0.15 = 0.45 点，"
                                 + "每秒 10 颗即 4.5 点，RE 折算为 5；充能为整数，故沿用 5。")
                .defineInRange("nuclearFuryVisCostPerSecond", 5, 0, 32768);
        NUCLEAR_FURY_VIS_MULT = builder
                .comment("Vis cost multiplier for Nuclear Fury.",
                         "核子之怒的 Vis 消耗倍率。原版配置 key 是 nuclearFuryVisCost，字段名叫 nuclearFuryVisMult。")
                .defineInRange("nuclearFuryVisMult", 1.0D, 0.0D, 1024.0D);
        NUCLEAR_FURY_MAX_CHARGE = builder
                .comment("Max Vis charge for Nuclear Fury.",
                         "核子之怒的最大 Vis 储量。RE 的折算：500 点正好支持 100 秒连续引导。")
                .defineInRange("nuclearFuryMaxCharge", 500, 0, 32768);
        NUCLEAR_FURY_CLEAR_RANGE = builder
                .comment("Target search radius in blocks for the Rageous Missiles. Original 1.7.10 hardcodes 32;"
                                 + " RE reuses the same 32 as its nuclearFuryClearRange.",
                         "导弹的目标搜索半径（格）。1.7.10 的实体里写死的就是 32；RE 的同名配置 nuclearFuryClearRange"
                                 + "（用于左键清弹，1.7.10 没有该功能）也是 32，这里沿用该 key 作为搜索半径。")
                .defineInRange("nuclearFuryClearRange", 32.0D, 1.0D, 128.0D);
        NUCLEAR_FURY_DAMAGE_MIN = builder
                .comment("Minimal damage dealt by a Rageous Missile.",
                         "每颗导弹能造成的伤害下限。原版 key 是 nuclearFuryDamageMIN，默认 24。")
                .defineInRange("nuclearFuryDamageMIN", 24.0D, 0.0D, 32768.0D);
        NUCLEAR_FURY_DAMAGE_MAX = builder
                .comment("Maximal damage dealt by a Rageous Missile.",
                         "每颗导弹能造成的伤害上限。原版 key 是 nuclearFuryDamageMAX，默认 32。")
                .defineInRange("nuclearFuryDamageMAX", 32.0D, 0.0D, 32768.0D);
        NUCLEAR_FURY_WARP = builder
                .comment("Warp granted by Nuclear Fury.",
                         "核子之怒附带的扭曲值。原版 ItemMissileTome#getWarp 返回 5。")
                .defineInRange("nuclearFuryWarp", 5, 0, 32768);
        builder.pop();

        builder.comment("千咒之诫（第七波：拉弓持续引导的范围灵魂汲取）").push("soul_tome");
        SOUL_TOME_VIS_COST = builder
                .comment("Base Vis cost for each soul drain pulse (one pulse every 4 ticks)."
                                 + " Original: Earth 25 + Air 20 + Fire 35 + Entropy 50 = 130 centivis = 1.30 vis,"
                                 + " rounded to 1 since the item charge is an integer.",
                         "每 4 tick 一次灵魂抽取的 Vis 基础消耗。原版是土（Terra）25 + 风（Aer）20 + 火（Ignis）35"
                                 + " + 混沌（Perditio）50 厘 Vis = 1.30 点；充能是整数，故取 1。")
                .defineInRange("soulTomeVisCost", 1, 0, 32768);
        SOUL_TOME_VIS_MULT = builder
                .comment("Vis cost multiplier for the Edict of a Thousand Damned Souls.",
                         "千咒之诫的 Vis 消耗倍率。注意：原版配置的 key 就叫 soulTomeVisCost（默认 1.0），"
                                 + "语义却是倍率，属于和核子之怒 nuclearFuryVisCost 同一类命名冲突；"
                                 + "这里按本项目惯例把 key 定为 soulTomeVisMult。")
                .defineInRange("soulTomeVisMult", 1.0D, 0.0D, 1024.0D);
        SOUL_TOME_KNOCKBACK_VIS_COST = builder
                .comment("Base Vis cost charged per entity pushed back inside the knockback range."
                                 + " Original: Fire 150 + Entropy 120 = 270 centivis = 2.70 vis, rounded to 2 (same as RE).",
                         "近距离击退每命中一个实体扣一次的 Vis 基础消耗。原版是火（Ignis）150 + 混沌（Perditio）120"
                                 + " = 2.70 点，充能为整数故取 2（与 1.12.2 RE 的取值一致）。")
                .defineInRange("soulTomeKnockbackVisCost", 2, 0, 32768);
        SOUL_TOME_MAX_CHARGE = builder
                .comment("Max Vis charge for the Edict of a Thousand Damned Souls."
                                 + " RE uses 1000, which is exactly 200 seconds of soul draining at 1 vis per 4 ticks.",
                         "千咒之诫的最大 Vis 储量。沿用 RE 的 1000：每 4 tick 抽 1 点，正好支撑 200 秒连续灵魂抽取。")
                .defineInRange("soulTomeMaxCharge", 1000, 0, 32768);
        SOUL_TOME_DIVISOR = builder
                .comment("Divisor used during damage calculations by the Edict of a Thousand Damned Souls."
                                 + " Setting this to 10 basically means that most of the time it drains 1/10 of the target's max health per attack.",
                         "千咒之诫灵魂抽取伤害的计算除数：伤害 = 目标最大生命 / 该值。原版配置 key 就是 soulTomeDivisor，"
                                 + "默认 10，即每次抽取大约抽掉目标最大生命的 1/10。")
                .defineInRange("soulTomeDivisor", 10.0D, 0.0D, 32768.0D);
        SOUL_TOME_WARMUP_TICKS = builder
                .comment("Warm-up ticks after starting to channel before any effect applies. Original hardcodes 20.",
                         "开始引导后的预热时间（tick），期间不结算任何效果。原版硬编码 20。")
                .defineInRange("soulTomeWarmupTicks", 20, 0, 32768);
        SOUL_TOME_DAMAGE_MIN = builder
                .comment("Minimal soul drain damage per pulse. Original hardcodes 1.0.",
                         "每次灵魂抽取的伤害下限。原版硬编码 1.0。")
                .defineInRange("soulTomeDamageMin", 1.0D, 0.0D, 32768.0D);
        SOUL_TOME_DAMAGE_MAX = builder
                .comment("Maximal soul drain damage per pulse. Original hardcodes 20.0.",
                         "每次灵魂抽取的伤害上限。原版硬编码 20.0。")
                .defineInRange("soulTomeDamageMax", 20.0D, 0.0D, 32768.0D);
        SOUL_TOME_KNOCKBACK_DAMAGE_MIN = builder
                .comment("Minimal true lightning damage dealt to an entity pushed back. Original: 20.0.",
                         "近距离击退造成的真雷伤害下限。原版是 20.0。")
                .defineInRange("soulTomeKnockbackDamageMin", 20.0D, 0.0D, 32768.0D);
        SOUL_TOME_KNOCKBACK_DAMAGE_MAX = builder
                .comment("Maximal true lightning damage dealt to an entity pushed back. Original: 20.0 + 80.0 * random, i.e. 100.0.",
                         "近距离击退造成的真雷伤害上限。原版是 20.0 + 80.0 × 随机，即 100.0。")
                .defineInRange("soulTomeKnockbackDamageMax", 100.0D, 0.0D, 32768.0D);
        SOUL_TOME_WARP = builder
                .comment("Warp granted by the Edict of a Thousand Damned Souls. Original getWarp returns 3.",
                         "千咒之诫附带的扭曲值。原版 getWarp 返回 3。")
                .defineInRange("soulTomeWarp", 3, 0, 32768);
        builder.pop();

        builder.comment("永恒放逐之诫（第七波：拉弓引导把目标放逐到下界）").push("edict_of_banishment");
        EDICT_OF_BANISHMENT_VIS_COST = builder
                .comment("Base Vis cost per second while channelling the Edict of Eternal Banishment."
                                 + " Original: Fire 8 + Order 5 + Entropy 5 centivis per tick = 0.18 vis/tick,"
                                 + " i.e. 3.6 vis per second, rounded up to 4 since the item charge is an integer.",
                         "引导永恒放逐之诫时每秒的 Vis 基础消耗。原版是每 tick 火（Ignis）8 + 秩序（Ordo）5"
                                 + " + 混沌（Perditio）5 厘 Vis = 0.18 点/tick，即 3.6 点/秒；充能是整数，"
                                 + "故按核子之怒的先例向上取整为 4。")
                .defineInRange("edictOfBanishmentVisCost", 4, 0, 32768);
        EDICT_OF_BANISHMENT_VIS_MULT = builder
                .comment("Vis cost multiplier for the Edict of Eternal Banishment. Original key: overthrowerVisMult.",
                         "永恒放逐之诫的 Vis 消耗倍率。原版配置 key 就是 overthrowerVisMult，默认 1.0。")
                .defineInRange("edictOfBanishmentVisMult", 1.0D, 0.0D, 1024.0D);
        EDICT_OF_BANISHMENT_MAX_CHARGE = builder
                .comment("Max Vis charge for the Edict of Eternal Banishment."
                                 + " One full 150-tick channel costs 8 x 4 = 32 vis, so 100 is a little over three channels.",
                         "永恒放逐之诫的最大 Vis 储量。一次完整引导（150 tick）扣 8 次 × 4 = 32 点，"
                                 + "100 点可支撑三次多一点。")
                .defineInRange("edictOfBanishmentMaxCharge", 100, 0, 32768);
        EDICT_OF_BANISHMENT_CHANNEL_DURATION = builder
                .comment("Full channel duration in ticks. Original getMaxItemUseDuration returns 150.",
                         "完整引导时长（tick）。原版 getMaxItemUseDuration 返回 150，即 7.5 秒。")
                .defineInRange("edictOfBanishmentChannelDuration", 150, 1, 32768);
        EDICT_OF_BANISHMENT_WARP = builder
                .comment("Warp granted by the Edict of Eternal Banishment. Original getWarp returns 2.",
                         "永恒放逐之诫附带的扭曲值。原版 ItemOverthrower#getWarp 返回 2。")
                .defineInRange("edictOfBanishmentWarp", 2, 0, 32768);
        builder.pop();

        builder.comment("深渊魔典（第七波：拉弓引导把目标放逐进虚空）").push("void_grimoire");
        VOID_GRIMOIRE_VIS_COST = builder
                .comment("Base Vis cost per second while channelling the Grimoire of The Abyss."
                                 + " Original: Order 9 + Entropy 16 centivis per tick = 0.25 vis/tick,"
                                 + " i.e. exactly 5 vis per second.",
                         "引导深渊魔典时每秒的 Vis 基础消耗。原版是每 tick 秩序（Ordo）9 + 混沌（Perditio）16"
                                 + " 厘 Vis = 0.25 点/tick，即恰好 5 点/秒。")
                .defineInRange("voidGrimoireVisCost", 5, 0, 32768);
        VOID_GRIMOIRE_VIS_MULT = builder
                .comment("Vis cost multiplier for the Grimoire of The Abyss. Original key: voidGrimoireVisMult.",
                         "深渊魔典的 Vis 消耗倍率。原版配置 key 就是 voidGrimoireVisMult，默认 1.0。")
                .defineInRange("voidGrimoireVisMult", 1.0D, 0.0D, 1024.0D);
        VOID_GRIMOIRE_MAX_CHARGE = builder
                .comment("Max Vis charge for the Grimoire of The Abyss."
                                 + " One full 100-tick channel costs 5 x 5 = 25 vis, so 100 is exactly four channels.",
                         "深渊魔典的最大 Vis 储量。一次完整引导（100 tick）扣 5 次 × 5 = 25 点，"
                                 + "100 点正好够四次。")
                .defineInRange("voidGrimoireMaxCharge", 100, 0, 32768);
        VOID_GRIMOIRE_CHANNEL_DURATION = builder
                .comment("Full channel duration in ticks. Original getMaxItemUseDuration returns 100.",
                         "完整引导时长（tick）。原版 getMaxItemUseDuration 返回 100，即 5 秒。")
                .defineInRange("voidGrimoireChannelDuration", 100, 1, 32768);
        VOID_GRIMOIRE_COOLDOWN = builder
                .comment("Shared cooldown in ticks after a completed channel."
                                 + " Original SuperpositionHandler.setCasted(player, 30, false).",
                         "一次完整引导结束后的共用冷却（tick）。原版 SuperpositionHandler.setCasted(player, 30, false)。")
                .defineInRange("voidGrimoireCooldown", 30, 0, 32768);
        VOID_GRIMOIRE_WARP = builder
                .comment("Warp granted by the Grimoire of The Abyss. Original getWarp returns 3.",
                         "深渊魔典附带的扭曲值。原版 ItemVoidGrimoire#getWarp 返回 3。")
                .defineInRange("voidGrimoireWarp", 3, 0, 32768);
        builder.pop();

        builder.comment("预言之典（第七波：念力控制 + 闪电攻击）").push("tome_of_predestiny");
        TOME_OF_PREDESTINY_VIS_MULT = builder
                .comment("Vis cost multiplier for the Tome of Predestiny. Original key: telekinesisTomeVisCost.",
                         "预言之典的 Vis 消耗倍率。原版配置 key 就叫 telekinesisTomeVisCost（默认 1.0），"
                                 + "语义却是倍率，属于和核子之怒 nuclearFuryVisCost 同一类命名冲突；"
                                 + "这里按本项目惯例把 key 定为 tomeOfPredestinyVisMult。")
                .defineInRange("tomeOfPredestinyVisMult", 1.0D, 0.0D, 1024.0D);
        TOME_OF_PREDESTINY_MAX_CHARGE = builder
                .comment("Max Vis charge for the Tome of Predestiny.", "预言之典的最大 Vis 储量。")
                .defineInRange("tomeOfPredestinyMaxCharge", 100, 0, 32768);
        TOME_OF_PREDESTINY_CONTROL_VIS_COST = builder
                .comment("Base Vis cost per second while telekinetically controlling a target."
                                 + " Original: Air 6 + Order 8 centivis per tick = 0.14 vis/tick,"
                                 + " i.e. 2.8 vis per second, rounded up to 3 since the item charge is an integer.",
                         "念力引导每秒的 Vis 基础消耗。原版是每 tick 风（Aer）6 + 秩序（Ordo）8"
                                 + " 厘 Vis = 0.14 点/tick，即 2.8 点/秒；充能是整数，故向上取整为 3。")
                .defineInRange("tomeOfPredestinyControlVisCost", 3, 0, 32768);
        TOME_OF_PREDESTINY_LIGHTNING_VIS_COST = builder
                .comment("Base Vis cost per lightning attack."
                                 + " Original: Air 80 + Order 50 + Fire 200 centivis = 3.3 vis, rounded to 3.",
                         "闪电攻击的 Vis 基础消耗。原版是风（Aer）80 + 秩序（Ordo）50 + 火（Ignis）200"
                                 + " 厘 Vis = 3.3 点，充能为整数故就近取 3。")
                .defineInRange("tomeOfPredestinyLightningVisCost", 3, 0, 32768);
        TOME_OF_PREDESTINY_SHOVE_VIS_COST = builder
                .comment("Base Vis cost of the sneak + left-click shove."
                                 + " Original: Air 150 + Order 80 centivis = 2.3 vis, rounded to 2.",
                         "潜行 + 左键「抛开」的 Vis 基础消耗。原版是风（Aer）150 + 秩序（Ordo）80"
                                 + " 厘 Vis = 2.3 点，充能为整数故就近取 2。")
                .defineInRange("tomeOfPredestinyShoveVisCost", 2, 0, 32768);
        TOME_OF_PREDESTINY_COOLDOWN = builder
                .comment("Shared cooldown in ticks after a lightning attack."
                                 + " Original SuperpositionHandler.setCasted(player, 10, true).",
                         "一次闪电攻击后的共用冷却（tick）。原版 SuperpositionHandler.setCasted(player, 10, true)。")
                .defineInRange("tomeOfPredestinyCooldown", 10, 0, 32768);
        TOME_OF_PREDESTINY_SHOVE_COOLDOWN = builder
                .comment("Ticks during which telekinetic control is disabled after a shove."
                                 + " Original writes ticksCooldown = 40 in the shove branch.",
                         "「抛开」之后不能再用念力控制的时长（tick）。原版在抛开分支里把 ticksCooldown 写成 40。")
                .defineInRange("tomeOfPredestinyShoveCooldown", 40, 0, 32768);
        TOME_OF_PREDESTINY_DAMAGE_MIN = builder
                .comment("Minimal lightning attack damage. Original key: telekinesisTomeDamageMIN, default 16.",
                         "闪电攻击的伤害下限。原版 key 是 telekinesisTomeDamageMIN，默认 16。")
                .defineInRange("tomeOfPredestinyDamageMIN", 16.0D, 0.0D, 32768.0D);
        TOME_OF_PREDESTINY_DAMAGE_MAX = builder
                .comment("Maximal lightning attack damage. Original key: telekinesisTomeDamageMAX, default 40.",
                         "闪电攻击的伤害上限。原版 key 是 telekinesisTomeDamageMAX，默认 40。")
                .defineInRange("tomeOfPredestinyDamageMAX", 40.0D, 0.0D, 32768.0D);
        TOME_OF_PREDESTINY_WARP = builder
                .comment("Warp granted by the Tome of Predestiny. Original getWarp returns 4.",
                         "预言之典附带的扭曲值。原版 ItemTelekinesisTome#getWarp 返回 4。")
                .defineInRange("tomeOfPredestinyWarp", 4, 0, 32768);
        builder.pop();

        builder.comment("月耀咒书（第七波：从天而降的耀月之辉）").push("tome_of_lunar_flares");
        TOME_OF_LUNAR_FLARES_VIS_COST = builder
                .comment("Base Vis cost per Lunar Flare."
                                 + " Original: Air 35 + Fire 50 + Order 65 centivis = 1.5 vis, rounded to 2.",
                         "每发一颗耀月之辉的 Vis 基础消耗。原版是风（Aer）35 + 火（Ignis）50 + 秩序（Ordo）65"
                                 + " 厘 Vis = 1.5 点，充能为整数故就近取 2。")
                .defineInRange("tomeOfLunarFlaresVisCost", 2, 0, 32768);
        TOME_OF_LUNAR_FLARES_VIS_MULT = builder
                .comment("Vis cost multiplier for the Tome of Lunar Flares. Original key: lunarFlaresVisCost.",
                         "月耀咒书的 Vis 消耗倍率。原版配置 key 就叫 lunarFlaresVisCost（默认 1.0），"
                                 + "语义却是倍率，属于和核子之怒 nuclearFuryVisCost 同一类命名冲突；"
                                 + "这里按本项目惯例把 key 定为 tomeOfLunarFlaresVisMult。")
                .defineInRange("tomeOfLunarFlaresVisMult", 1.0D, 0.0D, 1024.0D);
        TOME_OF_LUNAR_FLARES_MAX_CHARGE = builder
                .comment("Max Vis charge for the Tome of Lunar Flares."
                                 + " 300 = 150 flares (one full wand's worth of the bottleneck Order aspect).",
                         "月耀咒书的最大 Vis 储量。原版三项消耗里秩序（Ordo）是瓶颈，一本标准满 Vis 的"
                                 + "法杖约能支撑 150 发，故取 150 x 2 = 300。")
                .defineInRange("tomeOfLunarFlaresMaxCharge", 300, 0, 32768);
        TOME_OF_LUNAR_FLARES_COOLDOWN = builder
                .comment("Interval in ticks between two Lunar Flares while channelling."
                                 + " Original writes count % 2 == 0 in onUsingTick; the item has no post-cast cooldown.",
                         "连续引导时两次发射之间的间隔（tick）。原版在 onUsingTick 里写的是 count % 2 == 0，"
                                 + "物品本身没有任何施法后冷却，所以这一项不接 CooldownHelper。")
                .defineInRange("tomeOfLunarFlaresCooldown", 2, 1, 32768);
        TOME_OF_LUNAR_FLARES_DIRECT_DAMAGE = builder
                .comment("Damage of a direct hit by a Lunar Flare."
                                 + " Original key: damageLunarFlareDirect, default 72.",
                         "耀月之辉直击命中的伤害。原版 key 是 damageLunarFlareDirect，默认 72。")
                .defineInRange("tomeOfLunarFlaresDirectDamage", 72.0D, 0.0D, 32768.0D);
        TOME_OF_LUNAR_FLARES_IMPACT_DAMAGE = builder
                .comment("Damage taken by entities inside the impact zone."
                                 + " Original key: damageLunarFlareImpact, default 40.",
                         "耀月之辉爆发的范围伤害。原版 key 是 damageLunarFlareImpact，默认 40。")
                .defineInRange("tomeOfLunarFlaresImpactDamage", 40.0D, 0.0D, 32768.0D);
        TOME_OF_LUNAR_FLARES_WARP = builder
                .comment("Warp granted by the Tome of Lunar Flares. Original getWarp returns 3.",
                         "月耀咒书附带的扭曲值。原版 ItemLunarFlares#getWarp 返回 3。")
                .defineInRange("tomeOfLunarFlaresWarp", 3, 0, 32768);
        builder.pop();

        builder.comment("神化（第七波：召唤成排的巴比伦武器）").push("apotheosis");
        APOTHEOSIS_VIS_COST = builder
                .comment("Base Vis cost per summoned Babylon Weapon."
                                 + " Original: Earth 30 + Fire 60 + Order 50 + Entropy 75 centivis = 2.15 vis, rounded to 2.",
                         "每次召唤一把巴比伦武器的 Vis 基础消耗。原版是地（Terra）30 + 火（Ignis）60 + 秩序（Ordo）50"
                                 + " + 混沌（Perditio）75 厘 Vis = 2.15 点，充能为整数故就近取 2。")
                .defineInRange("apotheosisVisCost", 2, 0, 32768);
        APOTHEOSIS_VIS_MULT = builder
                .comment("Vis cost multiplier for Apotheosis. Original key: apotheosisVisMult, default 1.0.",
                         "神化的 Vis 消耗倍率。原版配置 key 就是 apotheosisVisMult，默认 1.0。")
                .defineInRange("apotheosisVisMult", 1.0D, 0.0D, 1024.0D);
        APOTHEOSIS_MAX_CHARGE = builder
                .comment("Max Vis charge for Apotheosis."
                                 + " The original bottleneck is Entropy: a full 100-vis wand sustains"
                                 + " 10000 / 75 = 133 summons, times the rounded cost 2 gives 266, rounded up to 300.",
                         "神化的最大 Vis 储量。原版四项消耗里混沌（Perditio）是瓶颈，一把每要素满 100 的标准法杖"
                                 + "约支撑 10000 / 75 = 133 次召唤，乘取整后的 2 得 266，向上取整到 300（约 150 次）。")
                .defineInRange("apotheosisMaxCharge", 300, 0, 32768);
        APOTHEOSIS_COOLDOWN = builder
                .comment("Interval in ticks between two summoned Babylon Weapons while channelling."
                                 + " Original writes count % 2 == 0 in onUsingTick; the item has no post-cast cooldown.",
                         "连续引导时两次召唤之间的间隔（tick）。原版在 onUsingTick 里写的是 count % 2 == 0，"
                                 + "物品本身没有任何施法后冷却，所以这一项不接 CooldownHelper。")
                .defineInRange("apotheosisCooldown", 2, 1, 32768);
        APOTHEOSIS_DIRECT_DAMAGE = builder
                .comment("Damage of a Babylon Weapon's direct hit."
                                 + " Original key: damageApotheosisDirect, default 100.",
                         "巴比伦武器直击命中的伤害。原版 key 是 damageApotheosisDirect，默认 100。")
                .defineInRange("damageApotheosisDirect", 100.0D, 0.0D, 32768.0D);
        APOTHEOSIS_IMPACT_DAMAGE = builder
                .comment("Damage taken by entities inside a Babylon Weapon's impact zone."
                                 + " Original key: damageApotheosisImpact, default 75.",
                         "巴比伦武器爆炸时范围内活体受到的伤害。原版 key 是 damageApotheosisImpact，默认 75。")
                .defineInRange("damageApotheosisImpact", 75.0D, 0.0D, 32768.0D);
        APOTHEOSIS_WARP = builder
                .comment("Warp granted by Apotheosis. Original getWarp returns 5.",
                         "神化附带的扭曲值。原版 ItemApotheosis#getWarp 返回 5。")
                .defineInRange("apotheosisWarp", 5, 0, 32768);
        builder.pop();

        builder.comment("破碎的命运巨著（第七波：随身携带的免死典籍）").push("tome_of_broken_fates");
        TOME_OF_BROKEN_FATES_VIS_COST = builder
                .comment("Base Vis cost per death-prevention trigger for the Tome of Broken Fates."
                                 + " Original: each of the six primal aspects pays 10000 centivis = 100 vis, 600 vis in total.",
                         "破碎的命运巨著每次免死的 Vis 基础消耗。原版是六大原初要素各扣 10000 厘 = 100 点，合计 600 点。")
                .defineInRange("tomeOfBrokenFatesVisCost", 600, 0, 32768);
        TOME_OF_BROKEN_FATES_VIS_MULT = builder
                .comment("Vis cost multiplier for the Tome of Broken Fates. Original key: fateTomeVisMult, default 1.0.",
                         "破碎的命运巨著的 Vis 消耗倍率。原版配置 key 是 fateTomeVisMult，默认 1.0。")
                .defineInRange("tomeOfBrokenFatesVisMult", 1.0D, 0.0D, 1024.0D);
        TOME_OF_BROKEN_FATES_MAX_CHARGE = builder
                .comment("Max Vis charge for the Tome of Broken Fates. 600 comes from the 1.12.2 port's"
                                 + " fateTomeMaxCharge and equals exactly one full 600-vis trigger.",
                         "破碎的命运巨著的最大 Vis 储量。沿用 1.12.2 移植版的 fateTomeMaxCharge 默认 600："
                                 + "正好支撑一次 600 点的免死，之后需要重新从周围灵气充满。")
                .defineInRange("tomeOfBrokenFatesMaxCharge", 600, 0, 32768);
        TOME_OF_BROKEN_FATES_COOLDOWN_MIN = builder
                .comment("Minimal possible cooldown in seconds for the death-prevention effect."
                                 + " Original key: fateTomeCooldownMIN, default 30.",
                         "免死效果的冷却下限（秒）。原版配置 key 是 fateTomeCooldownMIN，默认 30。")
                .defineInRange("tomeOfBrokenFatesCooldownMIN", 30, 0, 32768);
        TOME_OF_BROKEN_FATES_COOLDOWN_MAX = builder
                .comment("Maximal possible cooldown in seconds for the death-prevention effect."
                                 + " Original key: fateTomeCooldownMAX, default 90. Setting this to 0 disables the cooldown entirely.",
                         "免死效果的冷却上限（秒）。原版配置 key 是 fateTomeCooldownMAX，默认 90；设为 0 即完全关闭冷却。")
                .defineInRange("tomeOfBrokenFatesCooldownMAX", 90, 0, 32768);
        TOME_OF_BROKEN_FATES_MULTI_HELD_CHANCE = builder
                .comment("Chance per tick to trigger the disastrous consequence while carrying more than one tome."
                                 + " Original key: fateTomeMultiHeldChance, default 1.6E-5.",
                         "同时携带多本巨著时，每 tick 触发自毁惩罚的概率。原版配置 key 是 fateTomeMultiHeldChance，默认 1.6E-5。")
                .defineInRange("tomeOfBrokenFatesMultiHeldChance", 1.6E-5D, 0.0D, 1.0D);
        TOME_OF_BROKEN_FATES_MULTI_HELD_RANGE = builder
                .comment("Radius in blocks of the disastrous consequence's entity sweep. Original hardcodes 64.",
                         "自毁惩罚扫描活体的半径（格）。原版硬编码 64，即取以玩家为中心的立方体 ±64。")
                .defineInRange("tomeOfBrokenFatesMultiHeldRange", 64.0D, 1.0D, 256.0D);
        TOME_OF_BROKEN_FATES_DAMAGE = builder
                .comment("Damage dealt to every living entity caught in the disastrous consequence."
                                 + " Original key: fateTomeDamage, default 40000.0.",
                         "自毁惩罚对范围内每个活体造成的命运伤害。原版配置 key 是 fateTomeDamage，默认 40000.0。")
                .defineInRange("tomeOfBrokenFatesDamage", 40000.0D, 0.0D, 1000000.0D);
        TOME_OF_BROKEN_FATES_EXPLOSION_RADIUS = builder
                .comment("Explosion radius spawned at every entity caught in the disastrous consequence."
                                 + " Original key: fateTomeExplosionRadius, default 16.0.",
                         "自毁惩罚在每个被命中的活体处生成的爆炸半径。原版配置 key 是 fateTomeExplosionRadius，默认 16.0。")
                .defineInRange("tomeOfBrokenFatesExplosionRadius", 16.0D, 0.0D, 256.0D);
        TOME_OF_BROKEN_FATES_BIG_EXPLOSION_RADIUS = builder
                .comment("Final explosion radius at the carrier."
                                 + " Original key: fateTomeBigExplosionRadius, default 100.0.",
                         "自毁惩罚结束时在携带者处生成的大爆炸半径。原版配置 key 是 fateTomeBigExplosionRadius，默认 100.0。")
                .defineInRange("tomeOfBrokenFatesBigExplosionRadius", 100.0D, 0.0D, 256.0D);
        TOME_OF_BROKEN_FATES_BUFF_CHANCE = builder
                .comment("Chance for the death-prevention to apply buffs instead of debuffs."
                                 + " Original key: fateTomeBuffChance, default 0.75.",
                         "免死时施加增益（而不是减益）的概率。原版配置 key 是 fateTomeBuffChance，默认 0.75。")
                .defineInRange("tomeOfBrokenFatesBuffChance", 0.75D, 0.0D, 1.0D);
        TOME_OF_BROKEN_FATES_WARP = builder
                .comment("Warp granted by the Tome of Broken Fates."
                                 + " Original getWarp returns 7, the second highest value in the mod.",
                         "破碎的命运巨著附带的扭曲值。原版 getWarp 返回 7，全模组第二高（仅次于悖论之刃的 8）。")
                .defineInRange("tomeOfBrokenFatesWarp", 7, 0, 32768);
        builder.pop();

        builder.comment("湮灭之钥（第七波：合成栏绑定物品、背包按模式吞噬）").push("oblivion_stone");
        OBLIVION_STONE_HARD_CAP = builder
                .comment("How many items a single Keystone of The Oblivion can bind before it refuses more.",
                         "单把湮灭之钥能绑定的物品条数上限，超过后合成栏不再接受新样本。"
                                 + "原版配置 key 是 oblivionStoneHardCap，默认 64（用于防止超长清单带来性能问题）。")
                .defineInRange("oblivionStoneHardCap", 64, 0, 2048);
        OBLIVION_STONE_SOFT_CAP = builder
                .comment("How many entries the Ctrl tooltip lists before it switches to a random sample.",
                         "Ctrl 清单全量展开的条数上限，超过后只随机显示这么多条（避免清单长到看不清）。"
                                 + "原版配置 key 是 oblivionStoneSoftCap，默认 28。")
                .defineInRange("oblivionStoneSoftCap", 28, 0, 2048);
        OBLIVION_STONE_WARP = builder
                .comment("Warp granted by the Keystone of The Oblivion. Original getWarp returns 2.",
                         "湮灭之钥附带的扭曲值。原版 ItemOblivionStone#getWarp 返回 2。")
                .defineInRange("oblivionStoneWarp", 2, 0, 32768);
        builder.pop();

        SPEC = builder.build();
    }

    private FRConfig() {
    }
}
