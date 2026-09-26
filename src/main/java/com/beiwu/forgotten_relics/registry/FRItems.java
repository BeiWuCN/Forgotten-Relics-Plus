package com.beiwu.forgottenrelics_re.registry;

import com.beiwu.forgottenrelics_re.ForgottenRelics;
import com.beiwu.forgottenrelics_re.items.ItemAdvancedMiningCharm;
import com.beiwu.forgottenrelics_re.items.ItemDimensionalMirror;
import com.beiwu.forgottenrelics_re.items.ItemMiningCharm;
import com.beiwu.forgottenrelics_re.items.ItemSuperpositionRing;
import com.beiwu.forgottenrelics_re.items.ItemWeatherStone;
import com.beiwu.forgottenrelics_re.items.ItemXPTome;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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
 * 这里由注册键决定。注册键一律沿用原版的注册名，因此贴图、模型、语言键都不需要改路径。
 *
 * <p>稀有度在 1.12.2 是覆写 {@code Item#getRarity}，1.21.1 改为在 {@code Item.Properties} 上声明，
 * 所以这里会看到 {@code .rarity(...)}；数值取自原版各物品的 {@code EnumRarity}。
 */
public final class FRItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ForgottenRelics.MOD_ID);

    // ---- 原版起始研究解锁的 5 件物品 ----

    /** 采矿护符（Mining Charm）。原版稀有度 UNCOMMON，CHARM 槽。 */
    public static final DeferredItem<ItemMiningCharm> MINING_CHARM =
            ITEMS.registerItem("mining_charm", ItemMiningCharm::new,
                    new Item.Properties().rarity(Rarity.UNCOMMON));

    /** 以太采矿护符（Ethereal Mining Charm）。原版稀有度 EPIC，CHARM 槽，由采矿护符灌注升级而来。 */
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

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private FRItems() {
    }
}
