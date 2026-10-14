package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class AmuletConfig {
    final ModConfigSpec.BooleanValue DEIFIC_AMULET_EFFECT_IMMUNITY;
    final ModConfigSpec.BooleanValue DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS;
    final ModConfigSpec.BooleanValue DEIFIC_AMULET_INVINCIBILITY;
    final ModConfigSpec.IntValue DEIFIC_AMULET_INVINCIBILITY_EXTENSION;
    final ModConfigSpec.IntValue DEIFIC_AMULET_INVINCIBILITY_COOLDOWN;
    final ModConfigSpec.IntValue DEIFIC_AMULET_AIR_SUPPLY;
    final ModConfigSpec.IntValue DEIFIC_AMULET_VIS_COST;
    final ModConfigSpec.DoubleValue DEIFIC_AMULET_VIS_MULT;
    final ModConfigSpec.IntValue DEIFIC_AMULET_MAX_CHARGE;
    final ModConfigSpec.DoubleValue OBLIVION_AMULET_DAMAGE_RELEASE_CHANCE;
    final ModConfigSpec.DoubleValue OBLIVION_AMULET_DAMAGE_CAP;
    final ModConfigSpec.DoubleValue OBLIVION_AMULET_HIGH_DAMAGE_REDUCTION_CHANCE;
    final ModConfigSpec.DoubleValue OBLIVION_AMULET_POTION_CHANCE;
    final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_DURATION_MIN;
    final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_DURATION_MAX;
    final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_LEVEL_MIN;
    final ModConfigSpec.IntValue OBLIVION_AMULET_POTION_LEVEL_MAX;
    final ModConfigSpec.IntValue OBLIVION_AMULET_WARP;
    final ModConfigSpec.DoubleValue OBLIVION_AMULET_VIS_MULT;
    final ModConfigSpec.IntValue OBLIVION_AMULET_MAX_CHARGE;

    AmuletConfig(ModConfigSpec.Builder builder) {
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
    }
}
