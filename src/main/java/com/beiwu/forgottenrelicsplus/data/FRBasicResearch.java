package com.beiwu.forgottenrelicsplus.data;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;

final class FRBasicResearch {
    private FRBasicResearch() {}

    static void register(FRResearchBootstrap r) {
        r.entry("generic_theory", "fr.name.GenericTheory", 0, 0)
                .parents("thaumaturge:unlock_eldritch", "~thaumaturge:infusion", "~thaumaturge:metallurgy")
                .meta(ResearchEntryMeta.ROUND, ResearchEntryMeta.SPIKY, ResearchEntryMeta.HIDDEN)
                .icons("thaumaturge:curio_knowledge")
                .stage(r.stage("fr.text.GenericTheory.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:eldritch", 1))
                .stage(r.stage("fr.text.GenericTheory.2"))
                .register();

        r.entry("pech_wand", "fr.name.PechWand", -5, -4)
                .parents(
                        "forgotten_relics_plus:weather_stone",
                        "~thaumaturge:base_auromancy",
                        "~thaumaturge:focus_elemental")
                .meta(ResearchEntryMeta.HIDDEN)
                .icons("thaumaturge:pech_wand")
                .stage(r.stage("fr.text.PechWand.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:alchemy", 1))
                .stage(r.stage("fr.text.PechWand.2").recipes("forgotten_relics_plus:infusion/pech_wand"))
                .researchNote(3, "praecantatio", "desiderium", "humanus")
                .register();

        r.entry("spellbook_theory", "fr.name.SpellbookTheory", 0, 2)
                .parents("forgotten_relics_plus:generic_theory", "~thaumaturge:crimson_rites")
                .meta(ResearchEntryMeta.ROUND, ResearchEntryMeta.HIDDEN)
                .icons("minecraft:writable_book")
                .stage(r.stage("fr.text.SpellbookTheory.1")
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:auromancy", 1)
                        .requiresKnowledge(KnowledgeType.OBSERVATION, "thaumaturge:artifice", 1))
                .stage(r.stage("fr.text.SpellbookTheory.2"))
                .register();
    }
}
