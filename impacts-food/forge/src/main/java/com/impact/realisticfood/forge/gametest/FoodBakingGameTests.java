package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.CraftingGrids;
import com.impact.lib.grinding.GrinderTier;
import com.impact.lib.grinding.Grinding;
import com.impact.lib.grinding.GrindingRecipe;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingRecipe;
import com.impact.realisticfood.boiling.BoilingRecipes;
import com.impact.realisticfood.boiling.PotKind;
import com.impact.realisticfood.food.BakingIngredient;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/** Food milestone 9: Flour, Dough with any water bottle, Loaf Dough, the doughs' results, cake, cookies, pie, grid bread off. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodBakingGameTests {
    private FoodBakingGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void wheatGrindsIntoFlour(GameTestHelper helper) {
        Optional<GrindingRecipe> recipe = helper.getLevel().getRecipeManager().getAllRecipesFor(Grinding.recipeType()).stream()
                .filter(candidate -> candidate.uses(new ItemStack(Items.WHEAT))).findFirst();
        helper.assertTrue(recipe.isPresent(), "Food ships a grinding recipe for wheat");
        helper.assertTrue(recipe.get().getResultItem(helper.getLevel().registryAccess()).is(flour()), "Wheat grinds into Flour");
        helper.assertTrue(recipe.get().minimumTier() == GrinderTier.STONE, "It takes a stone grinder");
        helper.assertTrue(recipe.get().byproducts().stream().anyMatch(byproduct -> byproduct.stack().is(Items.WHEAT_SEEDS) && byproduct.chance() == 0.5F),
                "With a 50% chance of a wheat seed");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void flourAndWaterMakeDough(GameTestHelper helper) {
        ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        CraftingContainer grid = CraftingGrids.of(new ItemStack(flour()), new ItemStack(flour()), new ItemStack(flour()), waterBottle);
        CraftingRecipe recipe = craftingRecipe(helper, grid).orElseThrow();
        ItemStack dough = recipe.assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(dough.is(FoodItems.bakingIngredient(BakingIngredient.DOUGH)) && dough.getCount() == 3, "3 Flour and a water bottle make 3 Dough");
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(grid);
        helper.assertTrue(remaining.stream().anyMatch(stack -> stack.is(Items.GLASS_BOTTLE)), "The glass bottle comes back");
        ItemStack loafDough = craftingRecipe(helper, CraftingGrids.of(dough(), dough(), dough())).orElseThrow()
                .assemble(CraftingGrids.of(dough(), dough(), dough()), helper.getLevel().registryAccess());
        helper.assertTrue(loafDough.is(FoodItems.bakingIngredient(BakingIngredient.LOAF_DOUGH)), "3 Dough in a row make Loaf Dough");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void doughsBakeFlattenAndBoil(GameTestHelper helper) {
        Item loafDough = FoodItems.bakingIngredient(BakingIngredient.LOAF_DOUGH);
        Item dough = FoodItems.bakingIngredient(BakingIngredient.DOUGH);
        helper.assertTrue(cooked(helper, RecipeType.SMELTING, loafDough).is(Items.BREAD), "Loaf Dough bakes into bread");
        ItemStack flatbread = cooked(helper, RecipeType.CAMPFIRE_COOKING, loafDough);
        helper.assertTrue(flatbread.is(FoodItems.fixedFood(FixedFood.FLATBREAD)) && flatbread.getCount() == 3, "On a campfire it gives 3 Flatbread");
        BoilingRecipe boiledLoaf = BoilingRecipes.find(helper.getLevel(), new ItemStack(loafDough), PotKind.CAULDRON).orElseThrow();
        helper.assertTrue(boiledLoaf.result().is(FoodItems.fixedFood(FixedFood.DUMPLING)) && boiledLoaf.result().getCount() == 3, "Boiled, 3 Dumplings");
        helper.assertTrue(cooked(helper, RecipeType.SMELTING, dough).is(FoodItems.fixedFood(FixedFood.DOUGH_BUN)), "Dough bakes into a Dough Bun");
        helper.assertTrue(cooked(helper, RecipeType.CAMPFIRE_COOKING, dough).is(FoodItems.fixedFood(FixedFood.FLATBREAD)), "On a campfire, a Flatbread");
        helper.assertTrue(BoilingRecipes.find(helper.getLevel(), new ItemStack(dough), PotKind.CAULDRON).orElseThrow().result().is(FoodItems.fixedFood(FixedFood.DUMPLING)),
                "Boiled, a Dumpling");
        helper.assertTrue(FoodCuttingGameTests.foodOf(FoodItems.fixedFood(FixedFood.DOUGH_BUN)).getNutrition() == 3, "A Dough Bun restores 3 hunger");
        helper.assertTrue(FoodCuttingGameTests.foodOf(FoodItems.fixedFood(FixedFood.FLATBREAD)).getNutrition() == 4, "A Flatbread restores 4");
        helper.assertTrue(cooked(helper, RecipeType.SMELTING, FoodItems.fixedFood(FixedFood.DOUGH_BUN)).is(FoodItems.fixedFood(FixedFood.BURNT_FOOD)),
                "A Dough Bun burns with more dry heat");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void flourReplacesWheatInBaking(GameTestHelper helper) {
        helper.assertTrue(craftingRecipe(helper, CraftingGrids.of(new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT))).isEmpty(),
                "Grid bread is off");
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.itemId(Items.CAKE)).isEmpty(), "Vanilla's cake recipe is off");
        CraftingContainer cookieGrid = CraftingGrids.of(new ItemStack(flour()), new ItemStack(Items.COCOA_BEANS), new ItemStack(flour()));
        ItemStack cookies = craftingRecipe(helper, cookieGrid).orElseThrow().assemble(cookieGrid, helper.getLevel().registryAccess());
        helper.assertTrue(cookies.is(Items.COOKIE) && cookies.getCount() == 8, "2 Flour and cocoa beans make 8 cookies");
        CraftingContainer pieGrid = CraftingGrids.of(new ItemStack(Items.PUMPKIN), new ItemStack(Items.SUGAR), new ItemStack(Items.EGG), new ItemStack(flour()));
        helper.assertTrue(craftingRecipe(helper, pieGrid).orElseThrow().assemble(pieGrid, helper.getLevel().registryAccess()).is(Items.PUMPKIN_PIE),
                "Pumpkin pie takes Flour");
        CraftingContainer bowlGrid = CraftingGrids.of(new ItemStack(Items.BOWL), ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS));
        helper.assertTrue(craftingRecipe(helper, bowlGrid).orElseThrow().assemble(bowlGrid, helper.getLevel().registryAccess()).is(Grinding.woodenBowl()),
                "Without Crafting, a bowl on a plank makes a wooden grinder bowl");
        helper.succeed();
    }

    private static Item flour() {
        return FoodItems.bakingIngredient(BakingIngredient.FLOUR);
    }

    private static ItemStack dough() {
        return new ItemStack(FoodItems.bakingIngredient(BakingIngredient.DOUGH));
    }

    private static <R extends AbstractCookingRecipe> ItemStack cooked(GameTestHelper helper, RecipeType<R> type, Item input) {
        return helper.getLevel().getRecipeManager().getRecipeFor(type, new SimpleContainer(new ItemStack(input)), helper.getLevel())
                .map(recipe -> recipe.assemble(new SimpleContainer(new ItemStack(input)), helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    private static Optional<CraftingRecipe> craftingRecipe(GameTestHelper helper, CraftingContainer grid) {
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
    }
}
