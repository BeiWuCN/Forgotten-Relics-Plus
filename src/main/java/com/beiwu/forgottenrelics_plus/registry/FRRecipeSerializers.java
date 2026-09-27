package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.recipes.RecipeOblivionStone;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义配方序列化器注册表。
 *
 * <p>1.7.10 原版把 {@code RecipeOblivionStone} 这样的 {@code IRecipe} 直接塞进全局合成表；
 * 1.21.1 的配方是数据驱动的，任何非原版形状的配方都要先注册一个
 * {@link RecipeSerializer}，再由数据包 JSON（{@code data/<ns>/recipe/*.json}）里的
 * {@code type} 字段指过来。
 *
 * <p>本件只需要一个：湮灭之钥的绑定 / 清空配方。它复用原版通用的
 * {@link SimpleCraftingRecipeSerializer}（{@code crafting_special_*} 那一套用的就是它），
 * 因为该配方的全部信息都从合成格现算，JSON 里除了 {@code type} / {@code category} 没有任何字段。
 */
public final class FRRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ForgottenRelics.MOD_ID);

    /**
     * 湮灭之钥：合成栏绑定 / 清空配方。
     *
     * <p>注册名 {@code oblivion_stone_binding}，对应配方 JSON
     * {@code data/forgotten_relics_plus/recipe/oblivion_stone_binding.json}。
     */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeOblivionStone>>
            OBLIVION_STONE_BINDING = RECIPE_SERIALIZERS.register("oblivion_stone_binding",
                    () -> new SimpleCraftingRecipeSerializer<>(RecipeOblivionStone::new));

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }

    private FRRecipeSerializers() {
    }
}
