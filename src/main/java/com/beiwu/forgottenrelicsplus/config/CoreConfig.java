package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class CoreConfig {
    final ModConfigSpec.DoubleValue ARCANUM_GEN_RATE;
    final ModConfigSpec.DoubleValue ARCANUM_TELEPORT_CHANCE;
    final ModConfigSpec.IntValue ARCANUM_TELEPORT_RANGE;
    final ModConfigSpec.DoubleValue ARCANUM_DORMANT_TRANSFORM_CHANCE;
    final ModConfigSpec.IntValue ARCANUM_DORMANT_LIFE_MIN;
    final ModConfigSpec.IntValue ARCANUM_DORMANT_LIFE_MAX;
    final ModConfigSpec.DoubleValue ARCANUM_VIS_DISCOUNT;
    final ModConfigSpec.IntValue ARCANUM_MAX_CHARGE;
    final ModConfigSpec.DoubleValue NEBULOUS_CORE_DODGE_CHANCE;
    final ModConfigSpec.IntValue DORMANT_ARCANUM_VIS_COST_PER_TICK;
    final ModConfigSpec.IntValue DORMANT_ARCANUM_MAX_CHARGE;
    final ModConfigSpec.DoubleValue DORMANT_ARCANUM_VIS_MULT;
    final ModConfigSpec.DoubleValue CHAOS_CORE_CHANCE;
    final ModConfigSpec.IntValue CHAOS_CORE_DURATION_MIN;
    final ModConfigSpec.IntValue CHAOS_CORE_DURATION_SPAN;
    final ModConfigSpec.IntValue CHAOS_CORE_MAX_AMPLIFIER;
    final ModConfigSpec.IntValue CHAOS_CORE_WARP;
    final ModConfigSpec.DoubleValue CHAOS_CORE_REDIRECT_ATTACK;
    final ModConfigSpec.DoubleValue CHAOS_CORE_REDIRECT_DEFEND;
    final ModConfigSpec.DoubleValue CHAOS_CORE_SELF_REFLECT;
    final ModConfigSpec.DoubleValue CHAOS_CORE_DAMAGE_MULT_MAX;
    final ModConfigSpec.BooleanValue OMEGA_CORE_ENABLED;
    final ModConfigSpec.IntValue OMEGA_CORE_VIS_PER_TICK;
    final ModConfigSpec.IntValue OMEGA_CORE_RECHARGE_PER_TICK;
    final ModConfigSpec.BooleanValue OMEGA_CORE_PREVENT_DEATH;

    CoreConfig(ModConfigSpec.Builder builder) {
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
        OMEGA_CORE_ENABLED = builder
                .comment("Enable Omega Core research and crafting when Re:Avaritia is installed. Restart required.",
                        "是否启用欧米伽之核研究与合成；仍需安装 Re:Avaritia。修改后需重启游戏或服务器。")
                .worldRestart()
                .define("enabled", true);
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
                .comment("Protect the carrier from lethal health writes, death methods and destructive entity removal.",
                         "携带欧米伽之核时是否免疫致死改血量（含 setHealth）、死亡调用及致死实体移除，生命最低为 1。")
                .define("omegaCorePreventDeath", true);
        builder.pop();
    }
}
