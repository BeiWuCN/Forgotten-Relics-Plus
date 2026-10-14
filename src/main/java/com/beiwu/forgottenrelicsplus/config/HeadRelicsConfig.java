package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class HeadRelicsConfig {
    final ModConfigSpec.IntValue TERROR_CROWN_HAVOC_RANGE;
    final ModConfigSpec.IntValue TERROR_CROWN_HAVOC_INTERVAL;
    final ModConfigSpec.DoubleValue TERROR_CROWN_SCAN_RANGE;
    final ModConfigSpec.IntValue TERROR_CROWN_BLINDNESS_DURATION;
    final ModConfigSpec.IntValue TERROR_CROWN_WITHER_DURATION;
    final ModConfigSpec.IntValue TERROR_CROWN_WITHER_LEVEL;
    final ModConfigSpec.IntValue TERROR_CROWN_NAUSEA_DURATION;
    final ModConfigSpec.IntValue TERROR_CROWN_NAUSEA_LEVEL;
    final ModConfigSpec.IntValue TERROR_CROWN_SLOWNESS_DURATION;
    final ModConfigSpec.IntValue TERROR_CROWN_SLOWNESS_LEVEL;
    final ModConfigSpec.IntValue TERROR_CROWN_WEAKNESS_DURATION;
    final ModConfigSpec.IntValue TERROR_CROWN_WEAKNESS_LEVEL;
    final ModConfigSpec.IntValue TERROR_CROWN_MANA_COST;
    final ModConfigSpec.IntValue TERROR_CROWN_WARP;
    final ModConfigSpec.IntValue GHASTLY_SKULL_MIN_HEALTH;
    final ModConfigSpec.IntValue GHASTLY_SKULL_BURST_RANGE;
    final ModConfigSpec.DoubleValue GHASTLY_SKULL_BURST_RADIUS;
    final ModConfigSpec.DoubleValue GHASTLY_SKULL_DAMAGE_MULT;
    final ModConfigSpec.DoubleValue GHASTLY_SKULL_MAX_DAMAGE;
    final ModConfigSpec.DoubleValue GHASTLY_SKULL_HEAL_PER_TARGET;
    final ModConfigSpec.IntValue GHASTLY_SKULL_WITHER_DURATION;
    final ModConfigSpec.IntValue GHASTLY_SKULL_WITHER_LEVEL;
    final ModConfigSpec.IntValue GHASTLY_SKULL_VIS_COST;
    final ModConfigSpec.IntValue GHASTLY_SKULL_MAX_CHARGE;
    final ModConfigSpec.IntValue GHASTLY_SKULL_COOLDOWN;
    final ModConfigSpec.IntValue GHASTLY_SKULL_WARP;

    HeadRelicsConfig(ModConfigSpec.Builder builder) {
        builder.comment("恐惧之冠（原版分类 Crown of Terror）").push("terror_crown");
        TERROR_CROWN_HAVOC_INTERVAL = builder.defineInRange("terrorCrownHavocInterval", 5, 1, 200);
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
    }
}
