package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.recipes.RecipeOblivionStone;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FRRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ForgottenRelics.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeOblivionStone>>
            OBLIVION_STONE_BINDING = RECIPE_SERIALIZERS.register("oblivion_stone_binding",
                    () -> new SimpleCraftingRecipeSerializer<>(RecipeOblivionStone::new));

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }

    private FRRecipeSerializers() {
    }
}
