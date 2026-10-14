package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class WeaponConfig {
    final ModConfigSpec.DoubleValue PARADOX_DAMAGE_CAP;
    final ModConfigSpec.IntValue PARADOX_WARP;
    final ModConfigSpec.DoubleValue FALSE_JUSTICE_DAMAGE_MULTIPLIER;
    final ModConfigSpec.BooleanValue FALSE_JUSTICE_PREVENT_DEATH;
    final ModConfigSpec.IntValue FALSE_JUSTICE_WARP;
    final ModConfigSpec.DoubleValue THUNDERPEAL_DIRECT_DAMAGE;
    final ModConfigSpec.DoubleValue THUNDERPEAL_BOLT_DAMAGE;
    final ModConfigSpec.IntValue THUNDERPEAL_VIS_COST;
    final ModConfigSpec.DoubleValue THUNDERPEAL_VIS_MULT;
    final ModConfigSpec.IntValue THUNDERPEAL_MAX_CHARGE;
    final ModConfigSpec.IntValue THUNDERPEAL_COOLDOWN;

    WeaponConfig(ModConfigSpec.Builder builder) {
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
    }
}
