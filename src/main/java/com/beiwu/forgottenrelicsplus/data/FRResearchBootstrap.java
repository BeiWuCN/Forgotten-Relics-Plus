package com.beiwu.forgottenrelicsplus.data;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.google.gson.JsonPrimitive;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchIcon;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.content.research.ResearchCategory;
import com.leclowndu93150.thaumaturge.content.research.ResearchEntry;
import com.leclowndu93150.thaumaturge.content.research.ResearchStage;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class FRResearchBootstrap {
    public static final ResourceKey<IResearchCategory> CATEGORY = ResourceKey.create(
            IResearchCategory.REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "forgotten_relics"));

    private final BootstrapContext<IResearchEntry> context;
    private final HolderGetter<IResearchCategory> categories;
    private final HolderGetter<IAspect> aspects;
    private final HolderGetter<Item> items;

    private FRResearchBootstrap(BootstrapContext<IResearchEntry> context) {
        this.context = context;
        categories = context.lookup(IResearchCategory.REGISTRY_KEY);
        aspects = context.lookup(IAspect.REGISTRY_KEY);
        items = context.lookup(Registries.ITEM);
    }

    public static void categories(BootstrapContext<IResearchCategory> context) {
        HolderGetter<IAspect> aspects = context.lookup(IAspect.REGISTRY_KEY);
        context.register(
                CATEGORY,
                new ResearchCategory(
                        Optional.of(id("thaumaturge:crimson_rites")),
                        AspectList.ofEntries(List.of(
                                new AspectInstance(
                                        aspects.getOrThrow(ResourceKey.create(
                                                IAspect.REGISTRY_KEY, id("thaumaturge:praecantatio"))),
                                        20),
                                new AspectInstance(
                                        aspects.getOrThrow(
                                                ResourceKey.create(IAspect.REGISTRY_KEY, id("thaumaturge:alienis"))),
                                        15),
                                new AspectInstance(
                                        aspects.getOrThrow(
                                                ResourceKey.create(IAspect.REGISTRY_KEY, id("thaumaturge:tenebrae"))),
                                        15),
                                new AspectInstance(
                                        aspects.getOrThrow(
                                                ResourceKey.create(IAspect.REGISTRY_KEY, id("thaumaturge:vitreus"))),
                                        10),
                                new AspectInstance(
                                        aspects.getOrThrow(
                                                ResourceKey.create(IAspect.REGISTRY_KEY, id("thaumaturge:fabrico"))),
                                        10))),
                        id("forgotten_relics_plus:textures/item/omega_core.png"),
                        id("thaumaturge:textures/gui/gui_research_back_5.png"),
                        Optional.of(id("thaumaturge:textures/gui/gui_research_back_over.png")),
                        7));
    }

    public static void entries(BootstrapContext<IResearchEntry> context) {
        FRResearchBootstrap research = new FRResearchBootstrap(context);
        FRBasicResearch.register(research);
        FRRelicResearch.register(research);
        FRTomeResearch.register(research);
        FRWeaponResearch.register(research);
    }

    EntryBuilder entry(String path, String name, int column, int row) {
        return new EntryBuilder(path, name, column, row);
    }

    StageBuilder stage(String text) {
        return new StageBuilder(text);
    }

    AspectInstance aspect(String aspect, int amount) {
        return new AspectInstance(aspects.getOrThrow(ResourceKey.create(IAspect.REGISTRY_KEY, id(aspect))), amount);
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }

    final class EntryBuilder {
        private final String path;
        private final String name;
        private final int column;
        private final int row;
        private final LinkedHashSet<ResearchParent> parents = new LinkedHashSet<>();
        private final EnumSet<ResearchEntryMeta> meta = EnumSet.noneOf(ResearchEntryMeta.class);
        private final List<ResearchIcon> icons = new ArrayList<>();
        private final List<com.leclowndu93150.thaumaturge.api.research.IResearchStage> stages = new ArrayList<>();
        private AspectList notes = AspectList.EMPTY;
        private int complexity = 1;
        private boolean requiresNote;

        private EntryBuilder(String path, String name, int column, int row) {
            this.path = path;
            this.name = name;
            this.column = column;
            this.row = row;
        }

        EntryBuilder parents(String... values) {
            for (String value : values) {
                parents.add(ResearchParent.CODEC
                        .parse(JsonOps.INSTANCE, new JsonPrimitive(value))
                        .getOrThrow());
            }
            return this;
        }

        EntryBuilder meta(ResearchEntryMeta... values) {
            meta.addAll(List.of(values));
            return this;
        }

        EntryBuilder icons(String... values) {
            for (String value : values) {
                icons.add(ResearchIcon.CODEC
                        .parse(JsonOps.INSTANCE, new JsonPrimitive(value))
                        .getOrThrow());
            }
            return this;
        }

        EntryBuilder stage(StageBuilder stage) {
            stages.add(stage.build());
            return this;
        }

        EntryBuilder notes(AspectInstance... values) {
            notes = AspectList.ofEntries(List.of(values));
            return this;
        }

        EntryBuilder complexity(int value) {
            if (value < 1 || value > 8) {
                throw new IllegalArgumentException("Invalid research complexity: " + value);
            }
            complexity = value;
            return this;
        }

        EntryBuilder researchNote(int value, String... anchors) {
            if (value < 2 || value > 8 || anchors.length < 3 || anchors.length > 6) {
                throw new IllegalArgumentException("Invalid research note profile: " + path);
            }
            complexity(value);
            List<AspectInstance> entries = new ArrayList<>();
            int base = (value + 1) / 2;
            for (int i = 0; i < anchors.length; i++) {
                entries.add(aspect("thaumaturge:" + anchors[i], Math.max(1, base - (i >= 3 ? 1 : 0))));
            }
            notes = AspectList.ofEntries(entries);
            requiresNote = true;
            return this;
        }

        void register() {
            if (stages.isEmpty()) {
                throw new IllegalStateException("Research must contain stages: " + path);
            }
            if (requiresNote) {
                ResearchStage first = (ResearchStage) stages.getFirst();
                if (stages.size() < 2 || !first.recipes().isEmpty()) {
                    throw new IllegalStateException("Research note must precede the recipe page: " + path);
                }
                List<KnowledgeReward> requirements = new ArrayList<>(first.requiredKnowledge());
                requirements.add(new KnowledgeReward(KnowledgeType.THEORY, categories.getOrThrow(CATEGORY), 1));
                stages.set(
                        0,
                        new ResearchStage(
                                first.textKey(),
                                first.recipes(),
                                first.construct(),
                                first.obtain(),
                                first.craft(),
                                first.knowledge(),
                                List.copyOf(requirements),
                                first.requiredResearch(),
                                first.warp()));
            }
            context.register(
                    ResourceKey.create(
                            IResearchEntry.REGISTRY_KEY,
                            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, path)),
                    new ResearchEntry(
                            categories.getOrThrow(CATEGORY),
                            name,
                            new LinkedHashSet<>(parents),
                            Set.of(),
                            column,
                            row,
                            List.copyOf(stages),
                            EnumSet.copyOf(meta),
                            List.copyOf(icons),
                            List.of(),
                            notes,
                            complexity));
        }
    }

    final class StageBuilder {
        private final String text;
        private final List<ResourceLocation> recipes = new ArrayList<>();
        private final List<ResearchRequirement> obtain = new ArrayList<>();
        private final List<ResearchRequirement> craft = new ArrayList<>();
        private final List<KnowledgeReward> requiredKnowledge = new ArrayList<>();
        private int warp;

        private StageBuilder(String text) {
            this.text = text;
        }

        StageBuilder recipes(String... values) {
            for (String value : values) {
                recipes.add(id(value));
            }
            return this;
        }

        StageBuilder requiresKnowledge(KnowledgeType type, String category, int amount) {
            requiredKnowledge.add(new KnowledgeReward(
                    type,
                    categories.getOrThrow(ResourceKey.create(IResearchCategory.REGISTRY_KEY, id(category))),
                    amount));
            return this;
        }

        StageBuilder obtain(String item, int amount) {
            obtain.add(requirement(item, amount));
            return this;
        }

        StageBuilder craft(String item, int amount) {
            craft.add(requirement(item, amount));
            return this;
        }

        StageBuilder warp(int value) {
            warp = value;
            return this;
        }

        private ResearchRequirement requirement(String item, int amount) {
            return new ResearchRequirement(
                    HolderSet.direct(items.getOrThrow(ResourceKey.create(Registries.ITEM, id(item)))),
                    DataComponentPatch.EMPTY,
                    amount);
        }

        private ResearchStage build() {
            return new ResearchStage(
                    text,
                    List.copyOf(recipes),
                    Optional.empty(),
                    List.copyOf(obtain),
                    List.copyOf(craft),
                    List.of(),
                    List.copyOf(requiredKnowledge),
                    List.of(),
                    warp);
        }
    }
}
