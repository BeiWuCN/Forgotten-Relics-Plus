package com.beiwu.forgottenrelicsplus.data;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;

final class FRTomeResearch {
    private FRTomeResearch() {}

    static void register(FRResearchBootstrap r) {
        r.entry("experience_tome", "fr.name.XPTome", -5, 2)
                .parents("forgotten_relics_plus:weather_stone", "thaumaturge:jar_brain", "~thaumaturge:base_alchemy")
                .icons("forgotten_relics_plus:xp_tome")
                .stage(r.stage("fr.text.XPTome.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .obtain("thaumaturge:curio_arcane", 1)
                        .obtain("minecraft:writable_book", 1)
                        .warp(2))
                .stage(r.stage("fr.text.XPTome.2").recipes("forgotten_relics_plus:infusion/xp_tome"))
                .researchNote(3, "cognitio", "sensus", "praecantatio")
                .register();

        r.entry("soul_tome", "fr.name.SoulTome", -1, 9)
                .parents("forgotten_relics_plus:devourer_of_the_void", "~thaumaturge:jar_brain")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:soul_tome")
                .stage(r.stage("fr.text.SoulTome.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(3))
                .stage(r.stage("fr.text.SoulTome.2").recipes("forgotten_relics_plus:infusion/soul_tome"))
                .researchNote(5, "spiritus", "mortuus", "exanimis", "praecantatio")
                .register();

        r.entry("tome_of_broken_fates", "fr.name.FateTome", -4, 2)
                .parents("forgotten_relics_plus:spellbook_theory", "~thaumaturge:enter_outer_lands")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:tome_of_broken_fates")
                .stage(r.stage("fr.text.FateTome.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(8))
                .stage(r.stage("fr.text.FateTome.2").recipes("forgotten_relics_plus:infusion/tome_of_broken_fates"))
                .researchNote(8, "permutatio", "victus", "mortuus", "spiritus", "alienis", "praecantatio")
                .register();

        r.entry("tome_of_discord", "fr.name.DiscordTome", -2, 5)
                .parents("forgotten_relics_plus:spellbook_theory", "~thaumaturge:focus_rift")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:tome_of_discord")
                .stage(r.stage("fr.text.DiscordTome.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(3))
                .stage(r.stage("fr.text.DiscordTome.2").recipes("forgotten_relics_plus:infusion/tome_of_discord"))
                .researchNote(5, "permutatio", "motus", "vacuos", "praecantatio")
                .register();

        r.entry("tome_of_lunar_flares", "fr.name.LunarFlares", -5, 9)
                .parents(
                        "forgotten_relics_plus:tome_of_predestiny",
                        "~forgotten_relics_plus:eldritch_spell",
                        "~thaumaturge:primordial_pearl")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:tome_of_lunar_flares")
                .stage(r.stage("fr.text.LunarFlares.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.LunarFlares.2").recipes("forgotten_relics_plus:infusion/tome_of_lunar_flares"))
                .researchNote(7, "lux", "aer", "aversio", "praecantatio", "alienis")
                .register();

        r.entry("tome_of_predestiny", "fr.name.TelekinesisTome", -4, 7)
                .parents(
                        "forgotten_relics_plus:tome_of_discord",
                        "~thaumaturge:unlock_eldritch",
                        "~forgotten_relics_plus:thunderpeal")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:tome_of_predestiny")
                .stage(r.stage("fr.text.TelekinesisTome.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1))
                .stage(r.stage("fr.text.TelekinesisTome.2")
                        .recipes("forgotten_relics_plus:infusion/tome_of_predestiny"))
                .researchNote(4, "motus", "vinculum", "potentia", "praecantatio")
                .register();

        r.entry("tome_of_primal_chaos", "fr.name.ChaosTome", 2, 3)
                .parents("forgotten_relics_plus:eldritch_spell", "~thaumaturge:staff_primal")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:tome_of_primal_chaos")
                .stage(r.stage("fr.text.ChaosTome.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(7))
                .stage(r.stage("fr.text.ChaosTome.2").recipes("forgotten_relics_plus:infusion/tome_of_primal_chaos"))
                .researchNote(6, "perditio", "permutatio", "potentia", "vitium", "praecantatio")
                .register();

        r.entry("void_grimoire", "fr.name.VoidGrimoire", -7, 7)
                .parents(
                        "forgotten_relics_plus:edict_of_banishment",
                        "~thaumaturge:oculus",
                        "~forgotten_relics_plus:terror_crown",
                        "~forgotten_relics_plus:chaos_core")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("forgotten_relics_plus:void_grimoire")
                .stage(r.stage("fr.text.VoidGrimoire.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:infusion", 1)
                        .warp(4))
                .stage(r.stage("fr.text.VoidGrimoire.2").recipes("forgotten_relics_plus:infusion/void_grimoire"))
                .researchNote(7, "vacuos", "tenebrae", "alienis", "motus", "praecantatio")
                .register();
    }
}
