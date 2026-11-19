package com.impact.lib.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.audit.RecipeChecks;
import com.impact.lib.audit.RecipeConflict;
import com.impact.lib.audit.RecipeConflictChecker;
import com.impact.lib.crafting.ToolCraftingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

/** Lib's food slice: the recipe conflict checker and the live recipe check. */
@GameTestHolder(ImpactsLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibRecipeCheckGameTests {
    private static final String EMPTY_TEMPLATE = "empty";
    private static final String IMPACT_NAMESPACE = "impactstest";
    private static final String OTHER_NAMESPACE = "othertest";

    private LibRecipeCheckGameTests() {
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void sameIngredientsOrShapeIsAConflict(GameTestHelper helper) {
        helper.assertTrue(conflictsAmong(shapeless(IMPACT_NAMESPACE, "a", Items.DIRT, Items.DIRT), shapeless(OTHER_NAMESPACE, "b", Items.DIRT, Items.DIRT)) == 1,
                "Two shapeless recipes with the same ingredients collide");
        helper.assertTrue(conflictsAmong(column(IMPACT_NAMESPACE, "a"), column(OTHER_NAMESPACE, "b")) == 1, "Two identical shapes collide");
        helper.assertTrue(conflictsAmong(column(IMPACT_NAMESPACE, "a"), row(OTHER_NAMESPACE, "b")) == 0, "A column and a row don't");
        helper.assertTrue(conflictsAmong(shapeless(OTHER_NAMESPACE, "a", Items.DIRT), shapeless(OTHER_NAMESPACE, "b", Items.DIRT)) == 0,
                "Conflicts between two non-Impact recipes aren't Impact's to report");
        ToolCraftingRecipe cutting = new ToolCraftingRecipe(shapeless(IMPACT_NAMESPACE, "cut", Items.BEEF), List.of(Ingredient.of(Items.IRON_SWORD)), 1,
                List.of(), List.of(), ToolCraftingRecipe.NO_CRAFT_TICKS, Optional.empty());
        helper.assertTrue(conflictsAmong(cutting, shapeless(OTHER_NAMESPACE, "b", Items.BEEF, Items.IRON_SWORD)) == 1, "Tools count as ingredients");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void theLiveRecipeSetHasNoConflicts(GameTestHelper helper) {
        List<RecipeConflict> conflicts = RecipeChecks.runAndLog(helper.getLevel().getServer());
        helper.assertTrue(conflicts.isEmpty(), "Live recipe conflicts: " + conflicts);
        helper.succeed();
    }

    private static int conflictsAmong(Recipe<?>... recipes) {
        RecipeManager manager = new RecipeManager(ICondition.IContext.EMPTY);
        manager.replaceRecipes(List.of(recipes));
        return RecipeConflictChecker.findGridConflicts(manager).size();
    }

    private static ShapelessRecipe shapeless(String namespace, String path, Item... items) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (Item item : items) {
            ingredients.add(Ingredient.of(item));
        }
        return new ShapelessRecipe(ResourceIds.of(namespace, path), "", CraftingBookCategory.MISC, new ItemStack(Items.STONE), ingredients);
    }

    private static ShapedRecipe column(String namespace, String path) {
        return new ShapedRecipe(ResourceIds.of(namespace, path), "", CraftingBookCategory.MISC, 1, 2,
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.STICK), Ingredient.of(Items.STICK)), new ItemStack(Items.STONE));
    }

    private static ShapedRecipe row(String namespace, String path) {
        return new ShapedRecipe(ResourceIds.of(namespace, path), "", CraftingBookCategory.MISC, 2, 1,
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.STICK), Ingredient.of(Items.STICK)), new ItemStack(Items.STONE));
    }
}
