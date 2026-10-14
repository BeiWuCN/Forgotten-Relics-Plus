package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class ControlTomesConfig {
    final ModConfigSpec.IntValue SOUL_TOME_VIS_COST;
    final ModConfigSpec.DoubleValue SOUL_TOME_VIS_MULT;
    final ModConfigSpec.IntValue SOUL_TOME_KNOCKBACK_VIS_COST;
    final ModConfigSpec.IntValue SOUL_TOME_MAX_CHARGE;
    final ModConfigSpec.DoubleValue SOUL_TOME_DIVISOR;
    final ModConfigSpec.IntValue SOUL_TOME_WARMUP_TICKS;
    final ModConfigSpec.DoubleValue SOUL_TOME_DAMAGE_MIN;
    final ModConfigSpec.DoubleValue SOUL_TOME_DAMAGE_MAX;
    final ModConfigSpec.DoubleValue SOUL_TOME_KNOCKBACK_DAMAGE_MIN;
    final ModConfigSpec.DoubleValue SOUL_TOME_KNOCKBACK_DAMAGE_MAX;
    final ModConfigSpec.IntValue SOUL_TOME_WARP;
    final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_VIS_COST;
    final ModConfigSpec.DoubleValue EDICT_OF_BANISHMENT_VIS_MULT;
    final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_MAX_CHARGE;
    final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_CHANNEL_DURATION;
    final ModConfigSpec.IntValue EDICT_OF_BANISHMENT_WARP;
    final ModConfigSpec.IntValue VOID_GRIMOIRE_VIS_COST;
    final ModConfigSpec.DoubleValue VOID_GRIMOIRE_VIS_MULT;
    final ModConfigSpec.IntValue VOID_GRIMOIRE_MAX_CHARGE;
    final ModConfigSpec.IntValue VOID_GRIMOIRE_CHANNEL_DURATION;
    final ModConfigSpec.IntValue VOID_GRIMOIRE_COOLDOWN;
    final ModConfigSpec.IntValue VOID_GRIMOIRE_WARP;

    ControlTomesConfig(ModConfigSpec.Builder builder) {
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
    }
}
