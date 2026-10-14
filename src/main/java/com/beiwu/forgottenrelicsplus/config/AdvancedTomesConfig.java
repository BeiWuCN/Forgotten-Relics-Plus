package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class AdvancedTomesConfig {
    final ModConfigSpec.DoubleValue TOME_OF_PREDESTINY_VIS_MULT;
    final ModConfigSpec.IntValue TOME_OF_PREDESTINY_MAX_CHARGE;
    final ModConfigSpec.IntValue TOME_OF_PREDESTINY_CONTROL_VIS_COST;
    final ModConfigSpec.IntValue TOME_OF_PREDESTINY_LIGHTNING_VIS_COST;
    final ModConfigSpec.IntValue TOME_OF_PREDESTINY_SHOVE_VIS_COST;
    final ModConfigSpec.IntValue TOME_OF_PREDESTINY_COOLDOWN;
    final ModConfigSpec.IntValue TOME_OF_PREDESTINY_SHOVE_COOLDOWN;
    final ModConfigSpec.DoubleValue TOME_OF_PREDESTINY_DAMAGE_MIN;
    final ModConfigSpec.DoubleValue TOME_OF_PREDESTINY_DAMAGE_MAX;
    final ModConfigSpec.IntValue TOME_OF_PREDESTINY_WARP;
    final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_VIS_COST;
    final ModConfigSpec.DoubleValue TOME_OF_LUNAR_FLARES_VIS_MULT;
    final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_MAX_CHARGE;
    final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_COOLDOWN;
    final ModConfigSpec.DoubleValue TOME_OF_LUNAR_FLARES_DIRECT_DAMAGE;
    final ModConfigSpec.DoubleValue TOME_OF_LUNAR_FLARES_IMPACT_DAMAGE;
    final ModConfigSpec.IntValue TOME_OF_LUNAR_FLARES_WARP;
    final ModConfigSpec.IntValue APOTHEOSIS_VIS_COST;
    final ModConfigSpec.DoubleValue APOTHEOSIS_VIS_MULT;
    final ModConfigSpec.IntValue APOTHEOSIS_MAX_CHARGE;
    final ModConfigSpec.IntValue APOTHEOSIS_COOLDOWN;
    final ModConfigSpec.DoubleValue APOTHEOSIS_DIRECT_DAMAGE;
    final ModConfigSpec.DoubleValue APOTHEOSIS_IMPACT_DAMAGE;
    final ModConfigSpec.IntValue APOTHEOSIS_WARP;
    final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_VIS_COST;
    final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_VIS_MULT;
    final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_MAX_CHARGE;
    final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_COOLDOWN_MIN;
    final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_COOLDOWN_MAX;
    final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_MULTI_HELD_CHANCE;
    final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_MULTI_HELD_RANGE;
    final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_DAMAGE;
    final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_EXPLOSION_RADIUS;
    final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_BIG_EXPLOSION_RADIUS;
    final ModConfigSpec.DoubleValue TOME_OF_BROKEN_FATES_BUFF_CHANCE;
    final ModConfigSpec.IntValue TOME_OF_BROKEN_FATES_WARP;

    AdvancedTomesConfig(ModConfigSpec.Builder builder) {
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
    }
}
