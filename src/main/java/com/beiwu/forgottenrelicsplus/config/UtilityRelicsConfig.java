package com.beiwu.forgottenrelicsplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

final class UtilityRelicsConfig {
    final ModConfigSpec.IntValue XP_TOME_TRANSFER_RATE;
    final ModConfigSpec.IntValue WEATHER_STONE_VIS_COST;
    final ModConfigSpec.DoubleValue WEATHER_STONE_VIS_MULT;
    final ModConfigSpec.IntValue WEATHER_STONE_CHANNEL_DURATION;
    final ModConfigSpec.IntValue WEATHER_STONE_COOLDOWN;
    final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_CHANNEL_DURATION;
    final ModConfigSpec.IntValue SHINY_STONE_CHECK_RATE;
    final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_2;
    final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_3;
    final ModConfigSpec.IntValue SHINY_STONE_THRESHOLD_4;
    final ModConfigSpec.IntValue SHINY_STONE_STILL_INCREMENT;
    final ModConfigSpec.DoubleValue SHINY_STONE_HEAL_AMOUNT;
    final ModConfigSpec.IntValue OBLIVION_STONE_HARD_CAP;
    final ModConfigSpec.IntValue OBLIVION_STONE_SOFT_CAP;
    final ModConfigSpec.IntValue OBLIVION_STONE_WARP;

    UtilityRelicsConfig(ModConfigSpec.Builder builder) {
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

        builder.comment("湮灭之钥（第七波：合成栏绑定物品、背包按模式吞噬）").push("oblivion_stone");
        OBLIVION_STONE_HARD_CAP = builder
                .comment("How many items a single Keystone of The Oblivion can bind before it refuses more.",
                         "单把湮灭之钥能绑定的物品条数上限，超过后合成栏不再接受新样本。"
                                 + "原版配置 key 是 oblivionStoneHardCap，默认 64（用于防止超长清单带来性能问题）。")
                .defineInRange("oblivionStoneHardCap", 64, 0, 2048);
        OBLIVION_STONE_SOFT_CAP = builder
                .comment("How many entries the Ctrl tooltip lists before it switches to a random sample.",
                         "Ctrl 清单全量展开的条数上限，超过后只随机显示这么多条（避免清单长到看不清）。"
                                 + "原版配置 key 是 oblivionStoneSoftCap，默认 28。")
                .defineInRange("oblivionStoneSoftCap", 28, 0, 2048);
        OBLIVION_STONE_WARP = builder
                .comment("Warp granted by the Keystone of The Oblivion. Original getWarp returns 2.",
                         "湮灭之钥附带的扭曲值。原版 ItemOblivionStone#getWarp 返回 2。")
                .defineInRange("oblivionStoneWarp", 2, 0, 32768);
        builder.pop();
    }
}
