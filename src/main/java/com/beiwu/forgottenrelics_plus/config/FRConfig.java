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
    /** 每 tick 转移的经验点数。 */
    public static final ModConfigSpec.IntValue XP_TOME_TRANSFER_RATE;

    // ---- 符文天象石 / Weather Stone ----
    /** 每次施法的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_VIS_COST;
    /** Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue WEATHER_STONE_VIS_MULT;
    /** 引导时长（tick）。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_CHANNEL_DURATION;
    /** 使用后的冷却（tick）。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_COOLDOWN;

    // ---- 空间魔镜 / Dimensional Mirror ----
    /** 引导时长（tick）。 */
    public static final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_CHANNEL_DURATION;

    // ---- 音效 / Sound ----
    /** 全局音效音量倍率。原版音量偏大，这里统一压低。 */
    public static final ModConfigSpec.DoubleValue SOUND_VOLUME_MULTIPLIER;

    // ---- Vis 上限 / Vis ----
    /** 符文天象石的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_MAX_CHARGE;
    /** 空间魔镜的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_MAX_CHARGE;

    // ---- 远古之庇护 / Ancient Aegis ----
    /** 佩戴者受到的伤害减免比例，0.25 表示 25%。 */
    public static final ModConfigSpec.DoubleValue ANCIENT_AEGIS_DAMAGE_REDUCTION;
    /** 每次治疗回复的生命值。 */
    public static final ModConfigSpec.DoubleValue ANCIENT_AEGIS_HEAL_AMOUNT;
    /** 治疗判定间隔（tick）。 */
    public static final ModConfigSpec.IntValue ANCIENT_AEGIS_HEAL_INTERVAL;
    /** 击退抗性加成（1.0 表示完全免疫击退）。 */
    public static final ModConfigSpec.DoubleValue ANCIENT_AEGIS_KNOCKBACK_RESISTANCE;

    // ---- 七阳之戒 / Ring of The Seven Suns ----
    /** 超过该数值的伤害会被完全抵消。 */
    public static final ModConfigSpec.DoubleValue DARK_SUN_RING_DAMAGE_CAP;
    /** 把攻击反弹给攻击者的概率。 */
    public static final ModConfigSpec.DoubleValue DARK_SUN_RING_DEFLECT_CHANCE;
    /** 是否给「火焰伤害转化为治疗」加上冷却限制。 */
    public static final ModConfigSpec.BooleanValue DARK_SUN_RING_HEAL_LIMIT;
    /** 七阳之戒的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue DARK_SUN_RING_MAX_CHARGE;

    // ---- 神圣护身符 / Deific Amulet ----
    /** 是否免疫状态效果。 */
    public static final ModConfigSpec.BooleanValue DEIFIC_AMULET_EFFECT_IMMUNITY;
    /** 免疫状态效果时是否只清除减益、保留增益。 */
    public static final ModConfigSpec.BooleanValue DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS;
    /** 是否延长无敌帧。 */
    public static final ModConfigSpec.BooleanValue DEIFIC_AMULET_INVINCIBILITY;
    /** 延长后的无敌帧时长（tick）。 */
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_INVINCIBILITY_EXTENSION;
    /** 无敌帧延长效果的冷却（tick）。 */
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_INVINCIBILITY_COOLDOWN;
    /** 窒息时补充的氧气量（tick）。 */
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_AIR_SUPPLY;
    /** 每次补充氧气的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_VIS_COST;
    /** Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue DEIFIC_AMULET_VIS_MULT;
    /** 神圣护身符的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue DEIFIC_AMULET_MAX_CHARGE;

    // ---- 湮灭护符 / Amulet of The Oblivion ----
    /** 每 tick 释放已储存伤害的概率。 */
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_DAMAGE_RELEASE_CHANCE;
    /** 单次释放伤害的上限。 */
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_DAMAGE_CAP;
    /** 释放伤害超过上限时，改为按上限随机取值的概率。 */
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_HIGH_DAMAGE_REDUCTION_CHANCE;
    /** 每 tick 施加随机负面效果的概率。 */
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_POTION_CHANCE;
    /** 随机负面效果的最短持续时间（tick）。 */
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_DURATION_MIN;
    /** 随机负面效果的最长持续时间（tick）。 */
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_DURATION_MAX;
    /** 随机负面效果的最低等级（0 表示 I 级）。 */
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_LEVEL_MIN;
    /** 随机负面效果的最高等级。 */
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_LEVEL_MAX;
    /** 湮灭护符附带的扭曲值。 */
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_WARP;
    /** 储存伤害时的 Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue OBLIVION_AMULET_VIS_MULT;
    /** 湮灭护符的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue OBLIVION_AMULET_MAX_CHARGE;

    // ---- 日耀石 / Shiny Stone ----
    /** 静止判定间隔（tick）。 */
    public static final ModConfigSpec.IntValue SHINY_STONE_CHECK_RATE;
    /** 静止累计达到该值时进入第 2 档回血速度。 */
    public static final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_2;
    /** 静止累计达到该值时进入第 3 档回血速度。 */
    public static final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_3;
    /** 静止累计达到该值时进入第 4 档回血速度。 */
    public static final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_4;
    /** 每次判定静止时累计值的增量。 */
    public static final ModConfigSpec.IntValue SHINY_STONE_STILL_INCREMENT;
    /** 每次回血回复的生命值。 */
    public static final ModConfigSpec.DoubleValue SHINY_STONE_HEAL_AMOUNT;

    // ---- 浑浊之核 / Nebulous Core（注册名 arcanum）----
    /** 被动生成 Vis 的概率倍率。 */
    public static final ModConfigSpec.DoubleValue ARCANUM_GEN_RATE;
    /** 每 tick 随机传送的概率。 */
    public static final ModConfigSpec.DoubleValue ARCANUM_TELEPORT_CHANCE;
    /** 随机传送的最大距离（格）。 */
    public static final ModConfigSpec.IntValue ARCANUM_TELEPORT_RANGE;
    /** 每 tick 转化为休眠态的概率。 */
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
                         "本模组所有音效的音量倍率。原版音量偏大，默认降到 45%；设为 1.0 即恢复原版音量。")
                .defineInRange("soundVolumeMultiplier", 0.45D, 0.0D, 1.0D);
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

        SPEC = builder.build();
    }

    private FRConfig() {
    }
}
