package com.beiwu.forgottenrelicsplus.recipes;

import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.items.ItemOblivionStone;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.registry.FRRecipeSerializers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RecipeOblivionStone extends CustomRecipe {

    public RecipeOblivionStone(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {

        return !bindingResult(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return bindingResult(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FRRecipeSerializers.OBLIVION_STONE_BINDING.get();
    }

    private static ItemStack bindingResult(CraftingInput input) {
        ItemStack stone = ItemStack.EMPTY;
        List<ItemStack> samples = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(FRItems.OBLIVION_STONE.get())) {
                if (!stone.isEmpty()) {

                    return ItemStack.EMPTY;
                }
                stone = stack;
            } else {
                samples.add(stack);
            }
        }
        if (stone.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (samples.isEmpty()) {

            ItemStack cleared = new ItemStack(FRItems.OBLIVION_STONE.get());
            cleared.set(FRDataComponents.OBLIVION_MODE.get(), ItemOblivionStone.getMode(stone));
            return cleared;
        }

        if (samples.size() != 1) {

            return ItemStack.EMPTY;
        }

        ItemStack sample = samples.get(0);
        List<ItemStack> bound = ItemOblivionStone.getBoundItems(stone);
        if (bound.size() >= FRConfig.OBLIVION_STONE_HARD_CAP.get()) {

            return ItemStack.EMPTY;
        }
        ItemStack entry = ItemOblivionStone.normalizeSample(sample);
        for (ItemStack existing : bound) {

            if (existing.is(entry.getItem())
                    && (existing.isDamageableItem() || ItemStack.isSameItemSameComponents(existing, entry))) {
                return ItemStack.EMPTY;
            }
        }

        ItemStack result = stone.copy();
        result.setCount(1);
        List<ItemStack> next = new ArrayList<>(bound);
        next.add(entry);
        result.set(FRDataComponents.OBLIVION_BOUND_ITEMS.get(), List.copyOf(next));
        return result;
    }
}
