package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class CharmConfig {
    final ModConfigSpec.DoubleValue SUPERPOSITION_RING_SWAP_CHANCE;
    final ModConfigSpec.IntValue SUPERPOSITION_RING_CHECK_INTERVAL;
    final ModConfigSpec.DoubleValue SUPERPOSITION_RING_SPLIT_MIN;
    final ModConfigSpec.DoubleValue SUPERPOSITION_RING_SPLIT_MAX;
    final ModConfigSpec.DoubleValue ANCIENT_AEGIS_DAMAGE_REDUCTION;
    final ModConfigSpec.DoubleValue ANCIENT_AEGIS_HEAL_AMOUNT;
    final ModConfigSpec.IntValue ANCIENT_AEGIS_HEAL_INTERVAL;
    final ModConfigSpec.DoubleValue ANCIENT_AEGIS_KNOCKBACK_RESISTANCE;
    final ModConfigSpec.DoubleValue DARK_SUN_RING_DAMAGE_CAP;
    final ModConfigSpec.DoubleValue DARK_SUN_RING_DEFLECT_CHANCE;
    final ModConfigSpec.BooleanValue DARK_SUN_RING_HEAL_LIMIT;
    final ModConfigSpec.IntValue DARK_SUN_RING_MAX_CHARGE;

    CharmConfig(ModConfigSpec.Builder builder) {
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
    }
}
