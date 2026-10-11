package com.beiwu.forgottenrelicsplus.data;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;

final class FRRelicResearch {
    private FRRelicResearch() {}

    static void register(FRResearchBootstrap r) {
        r.entry("advanced_mining_charm", "fr.name.AdvancedMiningCharm", -4, -4)
                .parents("forgotten_relics_plus:mining_charm")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:advanced_mining_charm")
                .stage(r.stage("fr.text.AdvancedMiningCharm.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .obtain("thaumaturge:void_pickaxe", 1)
                        .craft("forgotten_relics_plus:mining_charm", 1))
                .stage(r.stage("fr.text.AdvancedMiningCharm.2")
                        .recipes("forgotten_relics_plus:infusion/advanced_mining_charm"))
                .researchNote(5, "instrumentum", "metallum", "potentia", "praecantatio")
                .register();

        r.entry("ancient_aegis", "fr.name.AncientAegis", 1, -4)
                .parents("forgotten_relics_plus:ring_of_superposition", "thaumaturge:unlock_eldritch")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:ancient_aegis")
                .stage(r.stage("fr.text.AncientAegis.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1))
                .stage(r.stage("fr.text.AncientAegis.2").recipes("forgotten_relics_plus:infusion/ancient_aegis"))
                .researchNote(6, "praemunio", "spiritus", "alienis", "praecantatio", "metallum")
                .register();

        r.entry("dark_sun_ring", "fr.name.DarkSunRing", 6, 2)
                .parents(
                        "forgotten_relics_plus:generic_theory",
                        "thaumaturge:infusion",
                        "~thaumaturge:unlock_eldritch",
                        "~thaumaturge:base_alchemy",
                        "~thaumaturge:runic_shielding")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:dark_sun_ring")
                .stage(r.stage("fr.text.DarkSunRing.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(3))
                .stage(r.stage("fr.text.DarkSunRing.2").recipes("forgotten_relics_plus:infusion/dark_sun_ring"))
                .researchNote(4, "tenebrae", "praemunio", "spiritus", "praecantatio")
                .register();

        r.entry("deific_amulet", "fr.name.DeificAmulet", -2, -2)
                .parents("forgotten_relics_plus:generic_theory", "thaumaturge:infusion")
                .meta(ResearchEntryMeta.ROUND)
                .icons("forgotten_relics_plus:deific_amulet")
                .stage(r.stage("fr.text.DeificAmulet.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.DeificAmulet.2").recipes("forgotten_relics_plus:infusion/deific_amulet"))
                .researchNote(3, "victus", "praemunio", "praecantatio")
                .register();

        r.entry("dimensional_mirror", "fr.name.DimensionalMirror", -4, 2)
                .parents(
                        "forgotten_relics_plus:generic_theory",
                        "thaumaturge:mirror_hand",
                        "~thaumaturge:focus_rift",
                        "~forgotten_relics_plus:deific_amulet")
                .meta(ResearchEntryMeta.SPIKY)
                .icons("forgotten_relics_plus:dimensional_mirror")
                .stage(r.stage("fr.text.DimensionalMirror.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .craft("thaumaturge:hand_mirror", 1)
                        .warp(1))
                .stage(r.stage("fr.text.DimensionalMirror.2")
                        .recipes("forgotten_relics_plus:infusion/dimensional_mirror"))
                .researchNote(4, "permutatio", "vacuos", "vitreus", "praecantatio")
                .register();

        r.entry("ghastly_skull", "fr.name.GhastlySkull", 0, -4)
                .parents("forgotten_relics_plus:oblivion_amulet", "thaumaturge:unlock_eldritch")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:ghastly_skull")
                .stage(r.stage("fr.text.GhastlySkull.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1))
                .stage(r.stage("fr.text.GhastlySkull.2").recipes("forgotten_relics_plus:infusion/ghastly_skull"))
                .researchNote(6, "mortuus", "spiritus", "aversio", "tenebrae", "praecantatio")
                .register();

        r.entry("mining_charm", "fr.name.MiningCharm", -4, -2)
                .parents("forgotten_relics_plus:generic_theory", "thaumaturge:infusion", "~thaumaturge:elemental_tools")
                .meta(ResearchEntryMeta.ROUND)
                .icons("forgotten_relics_plus:mining_charm")
                .stage(r.stage("fr.text.MiningCharm.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .obtain("thaumaturge:elemental_pickaxe", 1))
                .stage(r.stage("fr.text.MiningCharm.2").recipes("forgotten_relics_plus:infusion/mining_charm"))
                .researchNote(2, "terra", "instrumentum", "potentia")
                .register();

        r.entry("oblivion_amulet", "fr.name.OblivionAmulet", 0, -2)
                .parents(
                        "forgotten_relics_plus:generic_theory",
                        "thaumaturge:unlock_eldritch",
                        "~forgotten_relics_plus:oblivion_stone",
                        "~forgotten_relics_plus:terror_crown",
                        "~thaumaturge:primordial_pearl")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:oblivion_amulet")
                .stage(r.stage("fr.text.OblivionAmulet.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(8))
                .stage(r.stage("fr.text.OblivionAmulet.2").recipes("forgotten_relics_plus:infusion/oblivion_amulet"))
                .researchNote(5, "vacuos", "praemunio", "perditio", "praecantatio")
                .register();

        r.entry("oblivion_stone", "fr.name.OblivionStone", 3, -4)
                .parents("forgotten_relics_plus:ring_of_superposition", "~thaumaturge:base_eldritch")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:oblivion_stone")
                .stage(r.stage("fr.text.OblivionStone.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(3))
                .stage(r.stage("fr.text.OblivionStone.2").recipes("forgotten_relics_plus:infusion/oblivion_stone"))
                .researchNote(7, "vacuos", "perditio", "tenebrae", "permutatio", "praecantatio")
                .register();

        r.entry("paradox", "fr.name.TheParadox", 4, -4)
                .parents("forgotten_relics_plus:chaos_core", "thaumaturge:infusion", "~thaumaturge:primordial_pearl")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:paradox")
                .stage(r.stage("fr.text.TheParadox.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(8))
                .stage(r.stage("fr.text.TheParadox.2").recipes("forgotten_relics_plus:infusion/paradox"))
                .researchNote(7, "aversio", "permutatio", "metallum", "praecantatio", "alienis")
                .register();

        r.entry("ring_of_discord", "fr.name.DiscordRing", -3, -4)
                .parents(
                        "forgotten_relics_plus:deific_amulet",
                        "thaumaturge:unlock_eldritch",
                        "~forgotten_relics_plus:tome_of_discord")
                .meta(ResearchEntryMeta.ROUND)
                .icons("forgotten_relics_plus:discord_ring")
                .stage(r.stage("fr.text.DiscordRing.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.DiscordRing.2").recipes("forgotten_relics_plus:infusion/discord_ring"))
                .researchNote(2, "permutatio", "potentia", "praecantatio")
                .register();

        r.entry("ring_of_superposition", "fr.name.SuperpositionRing", 2, -2)
                .parents("forgotten_relics_plus:generic_theory", "thaumaturge:infusion", "~thaumaturge:unlock_eldritch")
                .icons("forgotten_relics_plus:superposition_ring")
                .stage(r.stage("fr.text.SuperpositionRing.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.SuperpositionRing.2")
                        .recipes("forgotten_relics_plus:infusion/superposition_ring"))
                .researchNote(4, "permutatio", "vinculum", "spiritus", "praecantatio")
                .register();

        r.entry("shiny_stone", "fr.name.ShinyStone", -2, -4)
                .parents("forgotten_relics_plus:deific_amulet", "thaumaturge:infusion")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:shiny_stone")
                .stage(r.stage("fr.text.ShinyStone.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.ShinyStone.2").recipes("forgotten_relics_plus:infusion/shiny_stone"))
                .researchNote(3, "lux", "victus", "potentia")
                .register();

        r.entry("terror_crown", "fr.name.TerrorCrown", 6, -2)
                .parents(
                        "forgotten_relics_plus:generic_theory",
                        "thaumaturge:unlock_eldritch",
                        "~thaumaturge:enter_outer_lands",
                        "~thaumaturge:oculus")
                .meta(ResearchEntryMeta.SPIKY, ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:terror_crown")
                .stage(r.stage("fr.text.TerrorCrown.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .obtain("thaumaturge:goggles_revealing", 1)
                        .warp(4))
                .stage(r.stage("fr.text.TerrorCrown.2").recipes("forgotten_relics_plus:infusion/terror_crown"))
                .researchNote(5, "sensus", "tenebrae", "aversio", "praecantatio")
                .register();

        r.entry("weather_stone", "fr.name.WeatherStone", -6, -2)
                .parents(
                        "forgotten_relics_plus:generic_theory",
                        "thaumaturge:infusion",
                        "~forgotten_relics_plus:deific_amulet")
                .icons("forgotten_relics_plus:weather_stone")
                .stage(r.stage("fr.text.WeatherStone.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .obtain("botania:teru_teru_bozu", 1))
                .stage(r.stage("fr.text.WeatherStone.2").recipes("forgotten_relics_plus:infusion/weather_stone"))
                .researchNote(2, "aer", "aqua", "potentia")
                .register();
    }
}
