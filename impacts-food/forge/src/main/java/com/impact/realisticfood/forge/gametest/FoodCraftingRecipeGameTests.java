package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Food milestone 10, recipe switching: with Impact's Realistic Crafting installed, the knives and the spit are made
 * from Crafting's parts and the plain recipes switch off; without it, the reverse. The Iron Knife's Iron Plate puts it
 * at Crafting's Stone Table.
 */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodCraftingRecipeGameTests {
    private static final List<String> SWITCHED_RECIPES = List.of("flint_knife", "iron_knife", "spit");
    private static final String WITH_CRAFTING_SUFFIX = "_with_" + FoodMod.CRAFTING_MOD_ID;
    private static final TagKey<Item> STONE_TIER = TagKey.create(Registries.ITEM, ResourceIds.of(FoodMod.CRAFTING_MOD_ID, "tier/stone"));

    private FoodCraftingRecipeGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void toolRecipesSwitchToCraftingParts(GameTestHelper helper) {
        boolean hasCrafting = ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID);
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String recipeName : SWITCHED_RECIPES) {
            boolean hasAloneRecipe = recipes.byKey(ResourceIds.of(FoodMod.MOD_ID, recipeName)).isPresent();
            boolean hasCraftingRecipe = recipes.byKey(ResourceIds.of(FoodMod.MOD_ID, recipeName + WITH_CRAFTING_SUFFIX)).isPresent();
            helper.assertTrue(hasAloneRecipe != hasCrafting, "The plain " + recipeName + " recipe loads only without Crafting");
            helper.assertTrue(hasCraftingRecipe == hasCrafting, "The " + recipeName + " recipe from Crafting's parts loads only with Crafting");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void onlyTheIronKnifeNeedsTheStoneTable(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        helper.assertTrue(hasStoneTierIngredient(helper, "iron_knife"), "The Iron Knife's Iron Plate is a stone-tier part");
        helper.assertFalse(hasStoneTierIngredient(helper, "flint_knife"), "The Flint Knife is made at any table");
        helper.assertFalse(hasStoneTierIngredient(helper, "spit"), "The spit is made at any table");
        helper.succeed();
    }

    private static boolean hasStoneTierIngredient(GameTestHelper helper, String recipeName) {
        Recipe<?> recipe = helper.getLevel().getRecipeManager().byKey(ResourceIds.of(FoodMod.MOD_ID, recipeName + WITH_CRAFTING_SUFFIX))
                .orElseThrow(() -> new IllegalStateException("No " + recipeName + " recipe from Crafting's parts"));
        return recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(FoodCraftingRecipeGameTests::isStoneTierOnly);
    }

    // Crafting gives an ingredient the tier of its cheapest item, so it's stone tier only when every item it accepts is.
    private static boolean isStoneTierOnly(Ingredient ingredient) {
        ItemStack[] accepted = ingredient.getItems();
        return accepted.length > 0 && Arrays.stream(accepted).allMatch(stack -> stack.is(STONE_TIER));
    }
}
