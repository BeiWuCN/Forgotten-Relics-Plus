package com.beiwu.forgottenrelics_re.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置。
 *
 * <p>对应 1.12.2 原版的 {@code RelicsConfigHandler}。原版用的是 Forge 的 {@code Configuration}
 * 加一堆静态字段，NeoForge 1.21.1 换成了 {@link ModConfigSpec}：字段本身是「值的包装」，
 * 读取时要调用 {@code get()}。
 *
 * <p>键名与默认值都照抄原版，默认值后面的注释里标出原版所在的分类，方便对照老配置文件。
 * 原版按物品分了很多分类，这里按同样的思路 push 出子分类。
 */
public final class FRConfig {

    public static final ModConfigSpec SPEC;

    // ---- 通用 / Generic Config ----
    /** 采矿护符：挖掘速度加成倍率，1.0 表示 +100%。 */
    public static final ModConfigSpec.DoubleValue MINING_CHARM_BOOST;
    /** 采矿护符：方块触及距离增加（格）。 */
    public static final ModConfigSpec.DoubleValue MINING_CHARM_REACH;
    /** 以太采矿护符：挖掘速度加成倍率，3.0 表示 +300%。 */
    public static final ModConfigSpec.DoubleValue ADVANCED_MINING_CHARM_BOOST;
    /** 以太采矿护符：方块触及距离增加（格）。 */
    public static final ModConfigSpec.DoubleValue ADVANCED_MINING_CHARM_REACH;
    /** 维度魔镜是否允许跨维度传送。 */
    public static final ModConfigSpec.BooleanValue INTERDIMENSIONAL_MIRROR;

    // ---- 叠加之戒 / Superposition Ring ----
    /** 每次判定时的交换概率。 */
    public static final ModConfigSpec.DoubleValue SUPERPOSITION_RING_SWAP_CHANCE;
    /** 交换判定的间隔（tick）。 */
    public static final ModConfigSpec.IntValue SUPERPOSITION_RING_CHECK_INTERVAL;

    // ---- 经验之书 / XP Tome ----
    /** 每 tick 转移的经验点数。 */
    public static final ModConfigSpec.IntValue XP_TOME_TRANSFER_RATE;

    // ---- 符文天象石 / Weather Stone ----
    /** 每次施法的 Vis 基础消耗。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_VIS_COST;
    /** Vis 消耗倍率。 */
    public static final ModConfigSpec.DoubleValue WEATHER_STONE_VIS_MULT;
    /** 引导时长（tick）。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_CHANNEL_DURATION;
    /** 使用后的冷却（tick）。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_COOLDOWN;

    // ---- 空间魔镜 / Dimensional Mirror ----
    /** 引导时长（tick）。 */
    public static final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_CHANNEL_DURATION;

    // ---- 音效 / Sound ----
    /** 全局音效音量倍率。原版音量偏大，这里统一压低。 */
    public static final ModConfigSpec.DoubleValue SOUND_VOLUME_MULTIPLIER;

    // ---- Vis 上限 / Vis ----
    /** 符文天象石的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue WEATHER_STONE_MAX_CHARGE;
    /** 空间魔镜的最大 Vis 储量。 */
    public static final ModConfigSpec.IntValue DIMENSIONAL_MIRROR_MAX_CHARGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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

        builder.comment("叠加之戒（原版分类 Superposition Ring）").push("superposition_ring");
        SUPERPOSITION_RING_SWAP_CHANCE = builder
                .comment("Chance per check for Ring of Superposition to swap positions.",
                         "叠加之戒每次判定时的位置交换概率。")
                .defineInRange("superpositionRingSwapChance", 0.025D, 0.0D, 1.0D);
        SUPERPOSITION_RING_CHECK_INTERVAL = builder
                .comment("Check interval in ticks for Ring of Superposition.",
                         "叠加之戒的交换判定间隔（tick）。")
                .defineInRange("superpositionRingCheckInterval", 600, 1, 32768);
        builder.pop();

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

        // 原版没有这一项：1.12.2 各处音量是写死的，最响的几处到了 8.0F，听感很吵。
        // 这里新增一个总开关，把所有音效（含以后补的物品）统一压低。
        builder.comment("音效（原版无此配置，为本模组新增）").push("sound");
        SOUND_VOLUME_MULTIPLIER = builder
                .comment("Global volume multiplier applied to every Forgotten Relics sound effect.",
                         "本模组所有音效的音量倍率。原版音量偏大，默认降到 45%；设为 1.0 即恢复原版音量。")
                .defineInRange("soundVolumeMultiplier", 0.45D, 0.0D, 1.0D);
        builder.pop();

        builder.comment("Vis 储量上限（原版分类 Vis）").push("vis");
        WEATHER_STONE_MAX_CHARGE = builder
                .comment("Max Vis charge for Runic Stone.", "符文天象石的最大 Vis 储量。")
                .defineInRange("weatherStoneMaxCharge", 100, 0, 32768);
        DIMENSIONAL_MIRROR_MAX_CHARGE = builder
                .comment("Max Vis charge for Dimensional Mirror.", "空间魔镜的最大 Vis 储量。")
                .defineInRange("dimensionalMirrorMaxCharge", 100, 0, 32768);
        builder.pop();

        SPEC = builder.build();
    }

    private FRConfig() {
    }
}
