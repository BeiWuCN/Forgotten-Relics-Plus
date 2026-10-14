package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class GeneralConfig {
    final ModConfigSpec.DoubleValue MINING_CHARM_BOOST;
    final ModConfigSpec.DoubleValue MINING_CHARM_REACH;
    final ModConfigSpec.DoubleValue ADVANCED_MINING_CHARM_BOOST;
    final ModConfigSpec.DoubleValue ADVANCED_MINING_CHARM_REACH;
    final ModConfigSpec.BooleanValue INTERDIMENSIONAL_MIRROR;
    final ModConfigSpec.DoubleValue SOUND_VOLUME_MULTIPLIER;
    final ModConfigSpec.IntValue WEATHER_STONE_MAX_CHARGE;
    final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_MAX_CHARGE;

    GeneralConfig(ModConfigSpec.Builder builder) {
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
    }
}
