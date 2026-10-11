package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.items.ItemAdvancedMiningCharm;
import com.beiwu.forgottenrelicsplus.items.ItemAncientAegis;
import com.beiwu.forgottenrelicsplus.items.ItemApotheosis;
import com.beiwu.forgottenrelicsplus.items.ItemDarkSunRing;
import com.beiwu.forgottenrelicsplus.items.ItemArcanum;
import com.beiwu.forgottenrelicsplus.items.ItemDeificAmulet;
import com.beiwu.forgottenrelicsplus.items.ItemDormantArcanum;
import com.beiwu.forgottenrelicsplus.items.ItemDevourerOfTheVoid;
import com.beiwu.forgottenrelicsplus.items.ItemEldritchSpell;
import com.beiwu.forgottenrelicsplus.items.ItemChaosCore;
import com.beiwu.forgottenrelicsplus.items.ItemChaosTome;
import com.beiwu.forgottenrelicsplus.items.ItemCrimsonSpell;
import com.beiwu.forgottenrelicsplus.items.ItemDimensionalMirror;
import com.beiwu.forgottenrelicsplus.items.ItemFalseJustice;
import com.beiwu.forgottenrelicsplus.items.ItemFateTome;
import com.beiwu.forgottenrelicsplus.items.ItemGhastlySkull;
import com.beiwu.forgottenrelicsplus.items.ItemLunarFlares;
import com.beiwu.forgottenrelicsplus.items.ItemMiningCharm;
import com.beiwu.forgottenrelicsplus.items.ItemNuclearFury;
import com.beiwu.forgottenrelicsplus.items.ItemOmegaCore;
import com.beiwu.forgottenrelicsplus.items.ItemOblivionAmulet;
import com.beiwu.forgottenrelicsplus.items.ItemOblivionStone;
import com.beiwu.forgottenrelicsplus.items.ItemOverthrower;
import com.beiwu.forgottenrelicsplus.items.ItemParadox;
import com.beiwu.forgottenrelicsplus.items.ItemRingOfDiscord;
import com.beiwu.forgottenrelicsplus.items.ItemShinyStone;
import com.beiwu.forgottenrelicsplus.items.ItemSoulTome;
import com.beiwu.forgottenrelicsplus.items.ItemSuperpositionRing;
import com.beiwu.forgottenrelicsplus.items.ItemTelekinesisTome;
import com.beiwu.forgottenrelicsplus.items.ItemTeleportationTome;
import com.beiwu.forgottenrelicsplus.items.ItemTerrorCrown;
import com.beiwu.forgottenrelicsplus.items.ItemThunderpeal;
import com.beiwu.forgottenrelicsplus.items.ItemVoidGrimoire;
import com.beiwu.forgottenrelicsplus.items.ItemWeatherStone;
import com.beiwu.forgottenrelicsplus.items.ItemXPTome;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FRItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ForgottenRelics.MOD_ID);

    public static final DeferredItem<ItemMiningCharm> MINING_CHARM =
            ITEMS.registerItem("mining_charm", ItemMiningCharm::new,
                    new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemAdvancedMiningCharm> ADVANCED_MINING_CHARM =
            ITEMS.registerItem("advanced_mining_charm", ItemAdvancedMiningCharm::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemDimensionalMirror> DIMENSIONAL_MIRROR =
            ITEMS.registerItem("dimensional_mirror", ItemDimensionalMirror::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemSuperpositionRing> SUPERPOSITION_RING =
            ITEMS.registerItem("superposition_ring", ItemSuperpositionRing::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemWeatherStone> WEATHER_STONE =
            ITEMS.registerItem("weather_stone", ItemWeatherStone::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemXPTome> XP_TOME =
            ITEMS.registerItem("xp_tome", ItemXPTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemAncientAegis> ANCIENT_AEGIS =
            ITEMS.registerItem("ancient_aegis", ItemAncientAegis::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemDarkSunRing> DARK_SUN_RING =
            ITEMS.registerItem("dark_sun_ring", ItemDarkSunRing::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemDeificAmulet> DEIFIC_AMULET =
            ITEMS.registerItem("deific_amulet", ItemDeificAmulet::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemOblivionAmulet> OBLIVION_AMULET =
            ITEMS.registerItem("oblivion_amulet", ItemOblivionAmulet::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemTerrorCrown> TERROR_CROWN =
            ITEMS.registerItem("terror_crown",
                    properties -> new ItemTerrorCrown(FRArmorMaterials.TERROR_CROWN, ArmorItem.Type.HELMET,
                            properties.durability(1000)),
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemShinyStone> SHINY_STONE =
            ITEMS.registerItem("shiny_stone", ItemShinyStone::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemArcanum> ARCANUM =
            ITEMS.registerItem("arcanum", ItemArcanum::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemDormantArcanum> DORMANT_ARCANUM =
            ITEMS.registerItem("dormant_arcanum", ItemDormantArcanum::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemRingOfDiscord> DISCORD_RING =
            ITEMS.registerItem("discord_ring", ItemRingOfDiscord::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemGhastlySkull> GHASTLY_SKULL =
            ITEMS.registerItem("ghastly_skull", ItemGhastlySkull::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemFalseJustice> FALSE_JUSTICE =
            ITEMS.registerItem("false_justice", ItemFalseJustice::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemChaosCore> CHAOS_CORE =
            ITEMS.registerItem("chaos_core", ItemChaosCore::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemOmegaCore> OMEGA_CORE =
            ITEMS.registerItem("omega_core", ItemOmegaCore::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemParadox> PARADOX =
            ITEMS.registerItem("paradox",
                    properties -> new ItemParadox(FRToolTiers.PARADOX,
                            properties.attributes(SwordItem.createAttributes(FRToolTiers.PARADOX, 3.0F, -2.4F))),
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemThunderpeal> THUNDERPEAL =
            ITEMS.registerItem("thunderpeal", ItemThunderpeal::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemTeleportationTome> TELEPORTATION_TOME =
            ITEMS.registerItem("tome_of_discord", ItemTeleportationTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemDevourerOfTheVoid> DEVOURER_OF_THE_VOID =
            ITEMS.registerItem("devourer_of_the_void", ItemDevourerOfTheVoid::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemEldritchSpell> ELDRITCH_SPELL =
            ITEMS.registerItem("eldritch_spell", ItemEldritchSpell::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemCrimsonSpell> CRIMSON_SPELL =
            ITEMS.registerItem("crimson_spell", ItemCrimsonSpell::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemChaosTome> TOME_OF_PRIMAL_CHAOS =
            ITEMS.registerItem("tome_of_primal_chaos", ItemChaosTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemNuclearFury> NUCLEAR_FURY =
            ITEMS.registerItem("nuclear_fury", ItemNuclearFury::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemSoulTome> SOUL_TOME =
            ITEMS.registerItem("soul_tome", ItemSoulTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemOverthrower> EDICT_OF_BANISHMENT =
            ITEMS.registerItem("edict_of_banishment", ItemOverthrower::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemVoidGrimoire> VOID_GRIMOIRE =
            ITEMS.registerItem("void_grimoire", ItemVoidGrimoire::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemTelekinesisTome> TOME_OF_PREDESTINY =
            ITEMS.registerItem("tome_of_predestiny", ItemTelekinesisTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemLunarFlares> TOME_OF_LUNAR_FLARES =
            ITEMS.registerItem("tome_of_lunar_flares", ItemLunarFlares::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemApotheosis> APOTHEOSIS =
            ITEMS.registerItem("apotheosis", ItemApotheosis::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemFateTome> TOME_OF_BROKEN_FATES =
            ITEMS.registerItem("tome_of_broken_fates", ItemFateTome::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<ItemOblivionStone> OBLIVION_STONE =
            ITEMS.registerItem("oblivion_stone", ItemOblivionStone::new,
                    new Item.Properties().rarity(Rarity.EPIC));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private FRItems() {
    }
}
