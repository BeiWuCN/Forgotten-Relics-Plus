package com.beiwu.forgottenrelicsplus.data;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;

final class FRWeaponResearch {
    private FRWeaponResearch() {}

    static void register(FRResearchBootstrap r) {
        r.entry("apotheosis", "fr.name.Apotheosis", -3, 7)
                .parents(
                        "forgotten_relics_plus:tome_of_discord",
                        "~forgotten_relics_plus:tome_of_lunar_flares",
                        "~forgotten_relics_plus:soul_tome",
                        "~forgotten_relics_plus:nuclear_fury",
                        "~forgotten_relics_plus:crimson_spell",
                        "~forgotten_relics_plus:tome_of_broken_fates",
                        "~forgotten_relics_plus:arcanum",
                        "~forgotten_relics_plus:oblivion_amulet",
                        "~forgotten_relics_plus:paradox",
                        "~forgotten_relics_plus:edict_of_banishment",
                        "~forgotten_relics_plus:dark_sun_ring",
                        "~forgotten_relics_plus:shiny_stone",
                        "~forgotten_relics_plus:dimensional_mirror")
                .meta(ResearchEntryMeta.ROUND)
                .icons("forgotten_relics_plus:apotheosis")
                .stage(r.stage("fr.text.Apotheosis.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(10))
                .stage(r.stage("fr.text.Apotheosis.2").recipes("forgotten_relics_plus:infusion/apotheosis"))
                .researchNote(8, "praecantatio", "aversio", "lux", "cognitio", "potentia", "alienis")
                .register();

        r.entry("arcanum", "fr.name.NebulousCore", -1, -4)
                .parents(
                        "forgotten_relics_plus:deific_amulet",
                        "thaumaturge:infusion",
                        "~thaumaturge:primordial_pearl",
                        "~thaumaturge:vis_amulet",
                        "~forgotten_relics_plus:ring_of_superposition",
                        "~thaumaturge:metallurgy")
                .meta(ResearchEntryMeta.HIDDEN, ResearchEntryMeta.SPIKY)
                .icons("forgotten_relics_plus:arcanum")
                .stage(r.stage("fr.text.NebulousCore.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.NebulousCore.2").recipes("forgotten_relics_plus:infusion/arcanum"))
                .researchNote(6, "auram", "praecantatio", "permutatio", "vacuos", "vitreus")
                .register();

        r.entry("chaos_core", "fr.name.ChaosCore", 4, -2)
                .parents(
                        "forgotten_relics_plus:generic_theory",
                        "thaumaturge:infusion",
                        "~thaumaturge:alumentum",
                        "~forgotten_relics_plus:ancient_aegis")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:chaos_core")
                .stage(r.stage("fr.text.ChaosCore.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .warp(2))
                .stage(r.stage("fr.text.ChaosCore.2").recipes("forgotten_relics_plus:infusion/chaos_core"))
                .researchNote(5, "perditio", "permutatio", "vitium", "potentia")
                .register();

        r.entry("crimson_spell", "fr.name.CrimsonSpell", -1, 11)
                .parents("forgotten_relics_plus:eldritch_spell")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:crimson_spell")
                .stage(r.stage("fr.text.CrimsonSpell.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(5))
                .stage(r.stage("fr.text.CrimsonSpell.2").recipes("forgotten_relics_plus:infusion/crimson_spell"))
                .researchNote(6, "aversio", "ignis", "spiritus", "praecantatio", "vitium")
                .register();

        r.entry("devourer_of_the_void", "fr.name.ObeliskDrainer", -1, 7)
                .parents(
                        "forgotten_relics_plus:tome_of_discord",
                        "~thaumaturge:enter_outer_lands",
                        "~thaumaturge:base_eldritch",
                        "~thaumaturge:base_alchemy",
                        "~thaumaturge:oculus")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:devourer_of_the_void")
                .stage(r.stage("fr.text.ObeliskDrainer.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(4))
                .stage(r.stage("fr.text.ObeliskDrainer.2")
                        .recipes("forgotten_relics_plus:infusion/devourer_of_the_void"))
                .researchNote(7, "vacuos", "alienis", "desiderium", "perditio", "praecantatio")
                .register();

        r.entry("edict_of_banishment", "fr.name.Overthrower", 3, 7)
                .parents("forgotten_relics_plus:tome_of_discord", "~thaumaturge:focus_hellbat")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:edict_of_banishment")
                .stage(r.stage("fr.text.Overthrower.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(3))
                .stage(r.stage("fr.text.Overthrower.2").recipes("forgotten_relics_plus:infusion/edict_of_banishment"))
                .researchNote(6, "aversio", "potentia", "ignis", "motus", "praecantatio")
                .register();

        r.entry("eldritch_spell", "fr.name.EldritchSpell", 0, 9)
                .parents("forgotten_relics_plus:devourer_of_the_void")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:eldritch_spell")
                .stage(r.stage("fr.text.EldritchSpell.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(6))
                .stage(r.stage("fr.text.EldritchSpell.2").recipes("forgotten_relics_plus:infusion/eldritch_spell"))
                .researchNote(6, "alienis", "tenebrae", "vacuos", "praecantatio", "aversio")
                .register();

        r.entry("false_justice", "fr.name.FalseJustice", -6, 2)
                .parents(
                        "forgotten_relics_plus:generic_theory",
                        "thaumaturge:unlock_eldritch",
                        "~forgotten_relics_plus:apotheosis")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:false_justice")
                .stage(r.stage("fr.text.FalseJustice.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .warp(4))
                .stage(r.stage("fr.text.FalseJustice.2").recipes("forgotten_relics_plus:infusion/false_justice"))
                .researchNote(7, "aversio", "praemunio", "permutatio", "spiritus", "alienis")
                .register();

        r.entry("nuclear_fury", "fr.name.NuclearFury", 2, 11)
                .parents("forgotten_relics_plus:tome_of_primal_chaos", "~thaumaturge:primordial_pearl")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:nuclear_fury")
                .stage(r.stage("fr.text.NuclearFury.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(4))
                .stage(r.stage("fr.text.NuclearFury.2").recipes("forgotten_relics_plus:infusion/nuclear_fury"))
                .researchNote(7, "potentia", "ignis", "perditio", "aversio", "praecantatio")
                .register();

        r.entry("thunderpeal", "fr.name.Thunderpeal", 2, 4)
                .parents(
                        "forgotten_relics_plus:spellbook_theory",
                        "~forgotten_relics_plus:tome_of_discord",
                        "~thaumaturge:base_auromancy")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:thunderpeal")
                .stage(r.stage("fr.text.Thunderpeal.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.Thunderpeal.2").recipes("forgotten_relics_plus:infusion/thunderpeal"))
                .researchNote(4, "potentia", "aer", "aversio", "praecantatio")
                .register();

        r.entry("omega_core", "fr.name.OmegaCore", -3, 11)
                .parents(
                        "forgotten_relics_plus:apotheosis",
                        "~forgotten_relics_plus:tome_of_broken_fates",
                        "~forgotten_relics_plus:void_grimoire")
                .meta(ResearchEntryMeta.SPIKY, ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:omega_core")
                .stage(r.stage("fr.text.OmegaCore.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(10))
                .stage(r.stage("fr.text.OmegaCore.2").recipes("forgotten_relics_plus:infusion/omega_core"))
                .researchNote(8, "auram", "praecantatio", "alienis", "potentia", "spiritus", "permutatio")
                .register();
    }
}
