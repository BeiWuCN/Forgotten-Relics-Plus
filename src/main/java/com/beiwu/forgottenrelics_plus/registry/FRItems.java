package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.items.ItemAdvancedMiningCharm;
import com.beiwu.forgottenrelics_plus.items.ItemAncientAegis;
import com.beiwu.forgottenrelics_plus.items.ItemDarkSunRing;
import com.beiwu.forgottenrelics_plus.items.ItemArcanum;
import com.beiwu.forgottenrelics_plus.items.ItemDeificAmulet;
import com.beiwu.forgottenrelics_plus.items.ItemDormantArcanum;
import com.beiwu.forgottenrelics_plus.items.ItemChaosCore;
import com.beiwu.forgottenrelics_plus.items.ItemDimensionalMirror;
import com.beiwu.forgottenrelics_plus.items.ItemGhastlySkull;
import com.beiwu.forgottenrelics_plus.items.ItemMiningCharm;
import com.beiwu.forgottenrelics_plus.items.ItemOmegaCore;
import com.beiwu.forgottenrelics_plus.items.ItemOblivionAmulet;
import com.beiwu.forgottenrelics_plus.items.ItemParadox;
import com.beiwu.forgottenrelics_plus.items.ItemRingOfDiscord;
import com.beiwu.forgottenrelics_plus.items.ItemShinyStone;
import com.beiwu.forgottenrelics_plus.items.ItemSuperpositionRing;
import com.beiwu.forgottenrelics_plus.items.ItemTerrorCrown;
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

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private FRItems() {
    }
}
