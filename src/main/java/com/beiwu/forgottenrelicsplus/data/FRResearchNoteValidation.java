package com.beiwu.forgottenrelicsplus.data;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.research.FRResearchNoteGenerator;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.TTResearchCategories;
import com.leclowndu93150.thaumaturge.content.research.ResearchEntry;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.mojang.serialization.JsonOps;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class FRResearchNoteValidation implements DataProvider {
    private final CompletableFuture<HolderLookup.Provider> registries;

    public FRResearchNoteValidation(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return registries.thenAccept(provider -> {
            int rechargeableItems = 0;
            for (var registered : FRItems.ITEMS.getEntries()) {
                if (!(registered.get() instanceof FRRechargable rechargeable)) continue;
                ItemStack stack = new ItemStack(registered.get());
                int capacity = rechargeable.getMaxCharge(stack, null);
                require(RechargeAccess.isRechargeable(stack), "Missing charge profile: " + registered.getId());
                require(
                        RechargeAccess.profile(stack).capacity() == capacity,
                        "Incorrect capacity: " + registered.getId());
                require(RechargeAccess.getCharge(stack) == 0, "New item already charged: " + registered.getId());
                RechargeAccess.rechargeItemBlindly(stack, null, capacity + 1);
                require(RechargeAccess.getCharge(stack) == capacity, "Charge exceeds capacity: " + registered.getId());
                require(
                        RechargeAccess.consumeCharge(stack, null, capacity),
                        "Cannot spend charge: " + registered.getId());
                require(
                        !RechargeAccess.consumeCharge(stack, null, 1),
                        "Empty item spends charge: " + registered.getId());
                rechargeableItems++;
            }
            require(rechargeableItems == 21, "Expected 21 rechargeable relics, got " + rechargeableItems);
            require(
                    GogglesAccess.isRevealing(new ItemStack(FRItems.TERROR_CROWN.get())),
                    "Crown lost revealing ability");
            ForgottenRelics.LOGGER.info(
                    "Validated {} rechargeable relics and crown revealing component", rechargeableItems);
            var entries = provider.lookupOrThrow(IResearchEntry.REGISTRY_KEY);
            int[] checked = {0};
            entries.listElements()
                    .filter(holder -> holder.key().location().getNamespace().equals(ForgottenRelics.MOD_ID))
                    .forEach(holder -> {
                        IResearchEntry entry = holder.value();
                        if (entry.stages().stream()
                                .noneMatch(stage -> !stage.recipes().isEmpty())) return;
                        ResourceLocation id = holder.key().location();
                        require(entry.complexity() >= 2 && entry.complexity() <= 8, "Invalid note complexity: " + id);
                        require(!entry.noteAspects().isEmpty(), "Missing note anchors: " + id);
                        require(entry.stages().getFirst().recipes().isEmpty(), "Recipe revealed before note: " + id);
                        require(
                                ResearchNotes.theoryRows(entry.stages().getFirst()) == 1,
                                "Missing first-page note gate: " + id);
                        require(ResearchNotes.theoryRowsBefore(entry, 1) == 1, "Invalid recipe-page gate: " + id);
                        if (entry.complexity() == 8) {
                            ResearchNoteData upstream = NoteGenerator.generate(
                                    ResourceLocation.fromNamespaceAndPath("thaumaturge", "validation"),
                                    0,
                                    entry.noteAspects(),
                                    8,
                                    RandomSource.create(0));
                            require(
                                    upstream.cells().size() == HexGrid.disk(4).size() - 6,
                                    "Upstream note difficulty changed");
                            ResearchEntry foreign = new ResearchEntry(
                                    provider.lookupOrThrow(IResearchCategory.REGISTRY_KEY)
                                            .getOrThrow(TTResearchCategories.BASICS),
                                    entry.nameKey(),
                                    entry.parents(),
                                    entry.siblings(),
                                    entry.column(),
                                    entry.row(),
                                    entry.stages(),
                                    entry.meta(),
                                    entry.icons(),
                                    entry.addenda(),
                                    entry.noteAspects(),
                                    8);
                            require(
                                    ResearchEntry.DIRECT_CODEC
                                            .encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), foreign)
                                            .error()
                                            .isPresent(),
                                    "Upstream complexity limit changed");
                        }
                        int expectedCells = HexGrid.disk(Math.min(4, entry.complexity() + 1))
                                        .size()
                                - entry.complexity() * 2;
                        for (int seed = 0; seed < 32; seed++) {
                            ResearchNoteData note = NoteGenerator.generate(
                                    id, 0, entry.noteAspects(), entry.complexity(), RandomSource.create(seed));
                            require(
                                    note.cells().size() == expectedCells,
                                    "Note complexity was clamped or holes were omitted: " + id);
                            require(note.cellMap().size() == note.cells().size(), "Duplicate note cells: " + id);
                            require(
                                    FRResearchNoteGenerator.isConnected(note.cellMap()),
                                    "Disconnected note grid: " + id);
                            long roots = note.cells().stream()
                                    .filter(cell -> cell.type() == ResearchNoteData.TYPE_ROOT)
                                    .count();
                            require(roots == entry.noteAspects().entries().size(), "Missing note anchors: " + id);
                            require(!note.complete(), "New note is already complete: " + id);
                            require(note.entry().equals(id) && note.index() == 0, "Incorrect note unlock key: " + id);
                        }
                        checked[0]++;
                    });
            require(checked[0] == 35, "Expected notes for 35 crafting research entries, got " + checked[0]);
            ForgottenRelics.LOGGER.info("Validated {} research note gates across 32 seeds each", checked[0]);
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    @Override
    public String getName() {
        return "Forgotten Relics Plus research note validation";
    }
}
