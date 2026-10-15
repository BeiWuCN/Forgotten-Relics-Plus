package com.beiwu.forgottenrelicsplus.gametest;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionInput;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgottenRelics.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OmegaRecipeGameTests {
    private OmegaRecipeGameTests() {}

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void allJarredNodeVariantsMatchInfusion(GameTestHelper helper) throws Exception {
        Ingredient nodeIngredient;
        try (var stream = OmegaRecipeGameTests.class.getResourceAsStream(
                "/data/forgotten_relics_plus/recipe/infusion/omega_core.json")) {
            if (stream == null) {
                throw new IllegalStateException("Omega Core recipe resource is missing");
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject node = null;
            for (var component : json.getAsJsonArray("components")) {
                var ingredient = component.getAsJsonObject();
                if (ingredient.has("item") && ingredient.get("item").getAsString().equals("thaumaturge:jar_node")) {
                    node = ingredient;
                    break;
                }
            }
            helper.assertTrue(node != null && node.size() == 1, "Jarred node must have no component restrictions");
            nodeIngredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, node).getOrThrow();
        }
        InfusionRecipe recipe = new InfusionRecipe(Ingredient.of(Items.DIAMOND), List.of(nodeIngredient),
                AspectList.EMPTY, 0, new ItemStack(Items.EMERALD), Optional.empty());
        ItemStack jar = nodeIngredient.getItems()[0].copy();
        assertMatches(helper, recipe, jar, "unconfigured node");
        var aer = helper.getLevel().registryAccess().registryOrThrow(IAspect.REGISTRY_KEY).getHolderOrThrow(TTAspects.AER);
        AspectList base = AspectList.of(new AspectInstance(aer, 1024));
        for (NodeType type : NodeType.values()) {
            var modifiers = new java.util.ArrayList<Optional<NodeModifier>>();
            modifiers.add(Optional.empty());
            for (NodeModifier modifier : NodeModifier.values()) {
                modifiers.add(Optional.of(modifier));
            }
            for (var modifier : modifiers) {
                for (int amount : new int[] {0, 1, 1024}) {
                    AspectList contents = amount == 0 ? AspectList.EMPTY : AspectList.of(new AspectInstance(aer, amount));
                    jar.set(TTDataComponents.NODE_DATA.get(), new NodeData(type, modifier, contents, base));
                    assertMatches(helper, recipe, jar, type + "/" + modifier + "/" + amount);
                }
            }
        }
        helper.assertTrue(!recipe.matches(new InfusionInput(new ItemStack(Items.DIAMOND),
                List.of(new ItemStack(Items.GLASS_BOTTLE))), helper.getLevel()), "An ordinary bottle cannot replace a node");
        helper.succeed();
    }

    private static void assertMatches(GameTestHelper helper, InfusionRecipe recipe, ItemStack jar, String variant) {
        InfusionInput input = new InfusionInput(new ItemStack(Items.DIAMOND), List.of(jar));
        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Rejected jarred node: " + variant);
        var consumed = recipe.jobComponents(input);
        helper.assertTrue(consumed != null && consumed.size() == 1
                && ItemStack.isSameItemSameComponents(jar, consumed.getFirst()), "Lost node data while matching: " + variant);
    }
}
