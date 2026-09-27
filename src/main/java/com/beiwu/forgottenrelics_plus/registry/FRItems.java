package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.items.ItemAdvancedMiningCharm;
import com.beiwu.forgottenrelics_plus.items.ItemAncientAegis;
import com.beiwu.forgottenrelics_plus.items.ItemApotheosis;
import com.beiwu.forgottenrelics_plus.items.ItemDarkSunRing;
import com.beiwu.forgottenrelics_plus.items.ItemArcanum;
import com.beiwu.forgottenrelics_plus.items.ItemDeificAmulet;
import com.beiwu.forgottenrelics_plus.items.ItemDormantArcanum;
import com.beiwu.forgottenrelics_plus.items.ItemDevourerOfTheVoid;
import com.beiwu.forgottenrelics_plus.items.ItemEldritchSpell;
import com.beiwu.forgottenrelics_plus.items.ItemChaosCore;
import com.beiwu.forgottenrelics_plus.items.ItemChaosTome;
import com.beiwu.forgottenrelics_plus.items.ItemCrimsonSpell;
import com.beiwu.forgottenrelics_plus.items.ItemDimensionalMirror;
import com.beiwu.forgottenrelics_plus.items.ItemFalseJustice;
import com.beiwu.forgottenrelics_plus.items.ItemFateTome;
import com.beiwu.forgottenrelics_plus.items.ItemGhastlySkull;
import com.beiwu.forgottenrelics_plus.items.ItemLunarFlares;
import com.beiwu.forgottenrelics_plus.items.ItemMiningCharm;
import com.beiwu.forgottenrelics_plus.items.ItemNuclearFury;
import com.beiwu.forgottenrelics_plus.items.ItemOmegaCore;
import com.beiwu.forgottenrelics_plus.items.ItemOblivionAmulet;
import com.beiwu.forgottenrelics_plus.items.ItemOblivionStone;
import com.beiwu.forgottenrelics_plus.items.ItemOverthrower;
import com.beiwu.forgottenrelics_plus.items.ItemParadox;
import com.beiwu.forgottenrelics_plus.items.ItemRingOfDiscord;
import com.beiwu.forgottenrelics_plus.items.ItemShinyStone;
import com.beiwu.forgottenrelics_plus.items.ItemSoulTome;
import com.beiwu.forgottenrelics_plus.items.ItemSuperpositionRing;
import com.beiwu.forgottenrelics_plus.items.ItemTelekinesisTome;
import com.beiwu.forgottenrelics_plus.items.ItemTeleportationTome;
import com.beiwu.forgottenrelics_plus.items.ItemTerrorCrown;
import com.beiwu.forgottenrelics_plus.items.ItemThunderpeal;
import com.beiwu.forgottenrelics_plus.items.ItemVoidGrimoire;
import com.beiwu.forgottenrelics_plus.items.ItemWeatherStone;
import com.beiwu.forgottenrelics_plus.items.ItemXPTome;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册表。
 *
 * <p>对应 1.12.2 原版 {@code CommonProxy} 里那一长串静态物品字段与
 * {@code registerItems(RegistryEvent.Register<Item>)}。NeoForge 1.21.1 换成
 * {@link DeferredRegister}：物品在类加载时声明，注册表实例挂到模组事件总线上统一提交。
 *
 * <p>1.12.2 里每个物品的注册名写在物品构造器里（{@code setRegistryName("forgotten_relics", name)}），
 * 这里由注册键决定。<b>物品的注册键一律沿用原版的注册名</b>（{@code mining_charm}、{@code shiny_stone} 等），
 * 所以贴图名、模型路径与语言键的后半段都不用改；
 * 改变的只有命名空间这一层（{@code forgotten_relics} -> {@code forgotten_relics_plus}）。
 *
 * <p>稀有度在 1.12.2 是覆写 {@code Item#getRarity}，1.21.1 改为在 {@code Item.Properties} 上声明，
 * 所以这里会看到 {@code .rarity(...)}；数值取自原版各物品的 {@code EnumRarity}。
 */
public final class FRItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ForgottenRelics.MOD_ID);

    // ---- 原版起始研究解锁的 5 件物品 ----

    /** 采矿护符（Mining Charm）。原版稀有度 UNCOMMON，RING 槽（戒指）。 */
    public static final DeferredItem<ItemMiningCharm> MINING_CHARM =
            ITEMS.registerItem("mining_charm", ItemMiningCharm::new,
                    new Item.Properties().rarity(Rarity.UNCOMMON));

    /** 以太采矿护符（Ethereal Mining Charm）。原版稀有度 EPIC，RING 槽，由采矿护符灌注升级而来。 */
    public static final DeferredItem<ItemAdvancedMiningCharm> ADVANCED_MINING_CHARM =
            ITEMS.registerItem("advanced_mining_charm", ItemAdvancedMiningCharm::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 空间魔镜（Dimensional Mirror）。原版稀有度 EPIC。 */
    public static final DeferredItem<ItemDimensionalMirror> DIMENSIONAL_MIRROR =
            ITEMS.registerItem("dimensional_mirror", ItemDimensionalMirror::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 叠加之戒（Ring of Superposition）。原版稀有度 EPIC，RING 槽。 */
    public static final DeferredItem<ItemSuperpositionRing> SUPERPOSITION_RING =
            ITEMS.registerItem("superposition_ring", ItemSuperpositionRing::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 符文天象石（Runic Stone，注册名 weather_stone）。原版稀有度 EPIC。 */
    public static final DeferredItem<ItemWeatherStone> WEATHER_STONE =
            ITEMS.registerItem("weather_stone", ItemWeatherStone::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 经验之书（Tome of Ageless Wisdom，注册名 xp_tome）。原版稀有度 EPIC。 */
    public static final DeferredItem<ItemXPTome> XP_TOME =
            ITEMS.registerItem("xp_tome", ItemXPTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    // ---- 第二波：腰带 / 戒指 / 护身符 / 头饰 ----

    /** 远古之庇护（Ancient Aegis）。原版稀有度 EPIC，BELT 槽。 */
    public static final DeferredItem<ItemAncientAegis> ANCIENT_AEGIS =
            ITEMS.registerItem("ancient_aegis", ItemAncientAegis::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 七阳之戒（Ring of The Seven Suns）。原版稀有度 EPIC，RING 槽。 */
    public static final DeferredItem<ItemDarkSunRing> DARK_SUN_RING =
            ITEMS.registerItem("dark_sun_ring", ItemDarkSunRing::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 神圣护身符（Deific Amulet）。原版稀有度 EPIC，AMULET 槽。 */
    public static final DeferredItem<ItemDeificAmulet> DEIFIC_AMULET =
            ITEMS.registerItem("deific_amulet", ItemDeificAmulet::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 湮灭护符（Amulet of The Oblivion）。原版稀有度 EPIC，AMULET 槽。 */
    public static final DeferredItem<ItemOblivionAmulet> OBLIVION_AMULET =
            ITEMS.registerItem("oblivion_amulet", ItemOblivionAmulet::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 恐惧之冠（Crown of Terror）。原版稀有度 EPIC。
     *
     * <p>它是本模组唯一的护甲类物品：既能戴在头盔位（{@code ArmorItem}），也能放进 Curios 的
     * {@code head} 槽。原版是金材质 + {@code setMaxDamage(1000)}，这里护甲值走自定义材质
     * {@link FRArmorMaterials#TERROR_CROWN}，耐久度在 {@code Item.Properties} 上单独给。
     */
    public static final DeferredItem<ItemTerrorCrown> TERROR_CROWN =
            ITEMS.registerItem("terror_crown",
                    properties -> new ItemTerrorCrown(FRArmorMaterials.TERROR_CROWN, ArmorItem.Type.HELMET,
                            properties.durability(1000)),
                    new Item.Properties().rarity(Rarity.EPIC));

    // ---- 第三波：剩余的饰品 ----

    /** 日耀石（Shiny Stone）。原版稀有度 EPIC，CHARM 槽。 */
    public static final DeferredItem<ItemShinyStone> SHINY_STONE =
            ITEMS.registerItem("shiny_stone", ItemShinyStone::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 浑浊之核（Nebulous Core，注册名 arcanum）。原版稀有度 EPIC，CHARM 槽。 */
    public static final DeferredItem<ItemArcanum> ARCANUM =
            ITEMS.registerItem("arcanum", ItemArcanum::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 休眠浑浊之核（Dormant Nebulous Core）。由浑浊之核转化而来，没有独立配方与研究词条。 */
    public static final DeferredItem<ItemDormantArcanum> DORMANT_ARCANUM =
            ITEMS.registerItem("dormant_arcanum", ItemDormantArcanum::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 不和谐之戒（Ring of Discord，注册名 discord_ring）。原版稀有度 EPIC，RING 槽。 */
    public static final DeferredItem<ItemRingOfDiscord> DISCORD_RING =
            ITEMS.registerItem("discord_ring", ItemRingOfDiscord::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    // ---- 第四波：原创补完 ----

    /**
     * 食尸鬼之颅（Ghastly Skull）。
     *
     * <p>1.7.10 原版注册了它但逻辑没写完（详见 {@link ItemGhastlySkull} 的类注释），
     * 1.12.2 移植版也没做。这一件是按原作者留下的意图与要素分配补完的**原创设计**，
     * 不是对照复刻。原版稀有度 EPIC，附带 3 点扭曲。
     */
    public static final DeferredItem<ItemGhastlySkull> GHASTLY_SKULL =
            ITEMS.registerItem("ghastly_skull", ItemGhastlySkull::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    // ---- 第四波：核心类与武器 ----

    /**
     * 虚伪审判（False Justice）。原版稀有度 EPIC，附带 4 点扭曲。研究格子 col=-7 / row=0。
     *
     * <p>随身携带生效：把携带者造成与受到的伤害都转成两倍真实伤害，并且让携带者与
     * <b>被携带者打死的目标</b>都不死。详见 {@link ItemFalseJustice}。
     */
    public static final DeferredItem<ItemFalseJustice> FALSE_JUSTICE =
            ITEMS.registerItem("false_justice", ItemFalseJustice::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /** 混沌之核（Chaos Core）。原版稀有度 EPIC，附带 2 点扭曲。研究格子 col=8 / row=-4。 */
    public static final DeferredItem<ItemChaosCore> CHAOS_CORE =
            ITEMS.registerItem("chaos_core", ItemChaosCore::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 欧米伽之核（Omega Core）。原版稀有度 EPIC。
     *
     * <p><b>原版没有研究词条、也没有灌注配方</b>，只能创造模式获取；按「只以原版为准」的原则，
     * 本项目同样不配配方与研究。
     */
    public static final DeferredItem<ItemOmegaCore> OMEGA_CORE =
            ITEMS.registerItem("omega_core", ItemOmegaCore::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 悖论之刃（The Paradox）。原版稀有度 EPIC，附带 8 点扭曲。
     *
     * <p>材质数值取自原版 {@code materialParadoxicalStuff}，见 {@link FRToolTiers#PARADOX}；
     * 攻击力附加是 -4，本体几乎没伤害，威力全在悖论效果上，所以这里的属性只给标准的剑基线。
     */
    public static final DeferredItem<ItemParadox> PARADOX =
            ITEMS.registerItem("paradox",
                    properties -> new ItemParadox(FRToolTiers.PARADOX,
                            properties.attributes(SwordItem.createAttributes(FRToolTiers.PARADOX, 3.0F, -2.4F))),
                    new Item.Properties().rarity(Rarity.EPIC));

    // ---- 第六波：弹射物书籍 ----

    /**
     * 霹雳咒书（Thunderpeal）。原版稀有度 EPIC，堆叠上限 1。
     *
     * <p>右键发射雷电球，消耗物品自身的 Vis 充能，并有 30 tick 的共用冷却。
     * 详见 {@link ItemThunderpeal}。
     */
    public static final DeferredItem<ItemThunderpeal> THUNDERPEAL =
            ITEMS.registerItem("thunderpeal", ItemThunderpeal::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    // ---- 第七波：错位之典 ----

    /**
     * 错位之典（Tome of Discord），注册名 {@code tome_of_discord}。
     *
     * <p>原版类名是 {@code ItemTeleportationTome}（研究键才叫 DiscordTome），1.7.10 注册名也是
     * ItemTeleportationTome；本项目按「一个物品一个物品」的节奏迁移时统一改用
     * {@code tome_of_discord} 这个注册名。原版稀有度 EPIC，堆叠上限 1，附带 2 点扭曲。
     *
     * <p>右键有三种传送模式，消耗物品自身 Vis 充能，并有 20 tick 共用冷却。
     * 详见 {@link ItemTeleportationTome}。
     */
    public static final DeferredItem<ItemTeleportationTome> TELEPORTATION_TOME =
            ITEMS.registerItem("tome_of_discord", ItemTeleportationTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 虚空吞噬者（Devourer of The Void），注册名 {@code devourer_of_the_void}。
     *
     * <p>原版类名是 {@code ItemObeliskDrainer}（本项目的注册名按第 7 波口径统一改成
     * {@code devourer_of_the_void}）。原版稀有度 EPIC，堆叠上限 1，附带 3 点扭曲。
     *
     * <p>手持右键可锁定 16 格内的神秘方尖碑并拉弓引导：每 30 tick 抽取一次，
     * 回复 4 点生命、补 2 点饥饿。详见 {@link ItemDevourerOfTheVoid}。
     */
    public static final DeferredItem<ItemDevourerOfTheVoid> DEVOURER_OF_THE_VOID =
            ITEMS.registerItem("devourer_of_the_void", ItemDevourerOfTheVoid::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 邪术之咒（Eldritch Spell），注册名 {@code eldritch_spell}。
     *
     * <p>原版类名 {@code ItemEldritchSpell}。原版稀有度 EPIC，堆叠上限 1，附带 4 点扭曲。
     *
     * <p>右键发射暗物质法球，消耗物品自身 Vis 充能，并有 20 tick 共用冷却。
     * 详见 {@link ItemEldritchSpell}。
     */
    public static final DeferredItem<ItemEldritchSpell> ELDRITCH_SPELL =
            ITEMS.registerItem("eldritch_spell", ItemEldritchSpell::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 腥红之咒（Crimson Spell），注册名 {@code crimson_spell}。
     *
     * <p>原版类名 {@code ItemCrimsonSpell}。原版稀有度 EPIC，堆叠上限 1，附带 3 点扭曲。
     *
     * <p>右键沿视线索敌并发射猩红法球，消耗物品自身 Vis 充能，并有 30 tick 共用冷却。
     * 详见 {@link ItemCrimsonSpell}。
     */
    public static final DeferredItem<ItemCrimsonSpell> CRIMSON_SPELL =
            ITEMS.registerItem("crimson_spell", ItemCrimsonSpell::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 原初混沌之典（Tome of Primal Chaos），注册名 {@code tome_of_primal_chaos}。
     *
     * <p>原版类名 {@code ItemChaosTome}。原版稀有度 EPIC，堆叠上限 1，附带 4 点扭曲。
     *
     * <p>右键拉弓持续引导，每 2 tick 生成一颗原初能量法球，消耗物品自身 Vis 充能。
     * 详见 {@link ItemChaosTome}。
     */
    public static final DeferredItem<ItemChaosTome> TOME_OF_PRIMAL_CHAOS =
            ITEMS.registerItem("tome_of_primal_chaos", ItemChaosTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 核子之怒（Nuclear Fury），注册名 {@code nuclear_fury}。
     *
     * <p>1.7.10 原版类名是 {@code ItemMissileTome}（这里沿用 RE 的 {@link ItemNuclearFury}）。
     * 原版稀有度 EPIC，堆叠上限 1，附带 5 点扭曲。
     *
     * <p>右键拉弓持续引导，每 2 tick 生成一颗追踪导弹，每秒消耗 5 点 Vis 充能；没有冷却。
     * 详见 {@link ItemNuclearFury}。
     */
    public static final DeferredItem<ItemNuclearFury> NUCLEAR_FURY =
            ITEMS.registerItem("nuclear_fury", ItemNuclearFury::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 千咒之诫（Edict of a Thousand Damned Souls），注册名 {@code soul_tome}。
     *
     * <p>原版类名 {@code ItemSoulTome}。原版稀有度 EPIC，堆叠上限 1，附带 3 点扭曲。
     *
     * <p>右键拉弓持续引导：20 格内的活体每 4 tick 被随机抽取一次最大生命 1/10 的灵魂伤害，
     * 3 格内的活体每 tick 被击退并承受真雷伤害；引导期间玩家几乎无法水平移动。
     * 详见 {@link ItemSoulTome}。
     */
    public static final DeferredItem<ItemSoulTome> SOUL_TOME =
            ITEMS.registerItem("soul_tome", ItemSoulTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 永恒放逐之诫（Edict of Eternal Banishment），注册名 {@code edict_of_banishment}。
     *
     * <p>1.7.10 原版类名就是 {@code ItemOverthrower}（1.7.10 的 Main 里注册的是它，同目录那份
     * {@code ItemOverthrowerLegacy} 只是没被任何地方引用的备用实现，详见提交说明）。
     * 原版稀有度 EPIC，堆叠上限 1，附带 2 点扭曲。
     *
     * <p>右键锁定准星指向的活体后拉弓引导 150 tick，结束时把目标放逐到下界并劈下真雷；
     * 引导期间每秒消耗物品自身的 Vis 充能。详见 {@link ItemOverthrower}。
     */
    public static final DeferredItem<ItemOverthrower> EDICT_OF_BANISHMENT =
            ITEMS.registerItem("edict_of_banishment", ItemOverthrower::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 深渊魔典（Grimoire of The Abyss），注册名 {@code void_grimoire}。
     *
     * <p>1.7.10 原版类名就是 {@code ItemVoidGrimoire}。原版稀有度 EPIC，堆叠上限 1，附带 3 点扭曲。
     *
     * <p>右键锁定准星指向的活体后拉弓引导 100 tick：每 tick 把目标定身并缓缓上浮，
     * 引导结束时把目标直接丢进本维度的虚空（{@code y ≈ -100000}）；引导期间每秒消耗物品自身的
     * Vis 充能，结束后有 30 tick 共用冷却。详见 {@link ItemVoidGrimoire}。
     */
    public static final DeferredItem<ItemVoidGrimoire> VOID_GRIMOIRE =
            ITEMS.registerItem("void_grimoire", ItemVoidGrimoire::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 预言之典（Tome of Predestiny），注册名 {@code tome_of_predestiny}。
     *
     * <p>1.7.10 原版类名就是 {@code ItemTelekinesisTome}（同目录的 {@code ItemTelekinesisTomeLegacy}
     * 没有被任何地方引用，不移植）。原版稀有度 EPIC，堆叠上限 1，附带 4 点扭曲。
     *
     * <p>按住右键以念力控制视线前方的活体（潜行时保持距离、否则拉近），左键对锁定目标劈闪电，
     * 潜行 + 左键则把它抛开；引导与攻击都消耗物品自身的 Vis 充能。详见 {@link ItemTelekinesisTome}。
     */
    public static final DeferredItem<ItemTelekinesisTome> TOME_OF_PREDESTINY =
            ITEMS.registerItem("tome_of_predestiny", ItemTelekinesisTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 月耀咒书（Tome of Lunar Flares），注册名 {@code tome_of_lunar_flares}。
     *
     * <p>1.7.10 原版类名就是 {@code ItemLunarFlares}。原版稀有度 EPIC，堆叠上限 1，附带 3 点扭曲。
     *
     * <p>右键拉弓持续引导，每 2 tick 对准星指向的方块降下一颗耀月之辉，消耗物品自身 Vis 充能；
     * 耀月之辉击中锁定方块时爆炸，对 5×5×5 格内的活体造成范围伤害并把它们推开。
     * 详见 {@link ItemLunarFlares} 与 {@link com.beiwu.forgottenrelics_plus.entity.EntityLunarFlare}。
     */
    public static final DeferredItem<ItemLunarFlares> TOME_OF_LUNAR_FLARES =
            ITEMS.registerItem("tome_of_lunar_flares", ItemLunarFlares::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 神化（Apotheosis），注册名 {@code apotheosis}。
     *
     * <p>1.7.10 原版类名 {@code ItemApotheosis}。原版稀有度 EPIC，堆叠上限 1，附带 5 点扭曲。
     *
     * <p>右键拉弓持续引导，每 2 tick 召唤一把巴比伦武器，消耗物品自身 Vis 充能；
     * 武器先悬停 15 tick，再朝视线落点直线飞出，直击与爆炸各有独立伤害。
     * 详见 {@link ItemApotheosis} 与
     * {@link com.beiwu.forgottenrelics_plus.entity.EntityBabylonWeapon}。
     */
    public static final DeferredItem<ItemApotheosis> APOTHEOSIS =
            ITEMS.registerItem("apotheosis", ItemApotheosis::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 破碎的命运巨著（Tome of Broken Fates），注册名 {@code tome_of_broken_fates}。
     *
     * <p>1.7.10 原版类名 {@code ItemFateTome}。原版稀有度 EPIC，堆叠上限 1，附带 7 点扭曲
     * （全模组第二高，仅次于悖论之刃的 8）。
     *
     * <p>放在背包里就能生效：致死时消耗 600 点 Vis 充能取消死亡、回满生命、随机施加增益或减益，
     * 随后进入 30~90 秒冷却；同时携带两本则有约六万分之一的概率触发自毁。
     * 详见 {@link ItemFateTome}。
     */
    public static final DeferredItem<ItemFateTome> TOME_OF_BROKEN_FATES =
            ITEMS.registerItem("tome_of_broken_fates", ItemFateTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    /**
     * 湮灭之钥（Keystone of The Oblivion），注册名 {@code oblivion_stone}。
     *
     * <p>1.7.10 原版类名就是 {@code ItemOblivionStone}。原版稀有度 EPIC，堆叠上限 1，附带 2 点扭曲。
     * 它不消耗也不储存 Vis，所以不实现 {@code FRRechargable}。
     *
     * <p>右键切换模式 / 启停；背包每 10 tick 按模式吞噬已绑定的物品；在合成栏里与一件物品组合可把它
     * 登记进清单、只放它自己则清空清单（自定义合成配方，见
     * {@code com.beiwu.forgottenrelics_plus.recipes.RecipeOblivionStone}）。
     * 详见 {@link ItemOblivionStone}。
     */
    public static final DeferredItem<ItemOblivionStone> OBLIVION_STONE =
            ITEMS.registerItem("oblivion_stone", ItemOblivionStone::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private FRItems() {
    }
}
