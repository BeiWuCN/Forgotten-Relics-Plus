package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class OffensiveTomesConfig {
    final ModConfigSpec.IntValue DISCORD_TOME_VIS_COST;
    final ModConfigSpec.DoubleValue DISCORD_TOME_VIS_MULT;
    final ModConfigSpec.IntValue DISCORD_TOME_MAX_CHARGE;
    final ModConfigSpec.IntValue DISCORD_TOME_COOLDOWN;
    final ModConfigSpec.DoubleValue DEVOURER_OF_THE_VOID_VIS_MULT;
    final ModConfigSpec.DoubleValue DEVOURER_OF_THE_VOID_RANGE;
    final ModConfigSpec.IntValue DEVOURER_OF_THE_VOID_PULSE_INTERVAL;
    final ModConfigSpec.DoubleValue DEVOURER_OF_THE_VOID_HEAL;
    final ModConfigSpec.IntValue DEVOURER_OF_THE_VOID_HUNGER;
    final ModConfigSpec.IntValue DEVOURER_OF_THE_VOID_WARP;
    final ModConfigSpec.IntValue ELDRITCH_SPELL_VIS_COST;
    final ModConfigSpec.DoubleValue ELDRITCH_SPELL_VIS_MULT;
    final ModConfigSpec.IntValue ELDRITCH_SPELL_MAX_CHARGE;
    final ModConfigSpec.IntValue ELDRITCH_SPELL_COOLDOWN;
    final ModConfigSpec.DoubleValue ELDRITCH_SPELL_DAMAGE;
    final ModConfigSpec.DoubleValue ELDRITCH_SPELL_DAMAGE_EX;
    final ModConfigSpec.IntValue CRIMSON_SPELL_VIS_COST;
    final ModConfigSpec.DoubleValue CRIMSON_SPELL_VIS_MULT;
    final ModConfigSpec.IntValue CRIMSON_SPELL_MAX_CHARGE;
    final ModConfigSpec.IntValue CRIMSON_SPELL_COOLDOWN;
    final ModConfigSpec.DoubleValue CRIMSON_SPELL_DAMAGE_MIN;
    final ModConfigSpec.DoubleValue CRIMSON_SPELL_DAMAGE_MAX;
    final ModConfigSpec.IntValue CRIMSON_SPELL_WARP;
    final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_VIS_COST;
    final ModConfigSpec.DoubleValue TOME_OF_PRIMAL_CHAOS_VIS_MULT;
    final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_MAX_CHARGE;
    final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_COOLDOWN;
    final ModConfigSpec.DoubleValue CHAOS_TOME_DAMAGE_CAP;
    final ModConfigSpec.IntValue TOME_OF_PRIMAL_CHAOS_WARP;
    final ModConfigSpec.IntValue NUCLEAR_FURY_VIS_COST_PER_SECOND;
    final ModConfigSpec.DoubleValue NUCLEAR_FURY_VIS_MULT;
    final ModConfigSpec.IntValue NUCLEAR_FURY_MAX_CHARGE;
    final ModConfigSpec.DoubleValue NUCLEAR_FURY_CLEAR_RANGE;
    final ModConfigSpec.DoubleValue NUCLEAR_FURY_DAMAGE_MIN;
    final ModConfigSpec.DoubleValue NUCLEAR_FURY_DAMAGE_MAX;
    final ModConfigSpec.IntValue NUCLEAR_FURY_WARP;

    OffensiveTomesConfig(ModConfigSpec.Builder builder) {
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
    }
}
