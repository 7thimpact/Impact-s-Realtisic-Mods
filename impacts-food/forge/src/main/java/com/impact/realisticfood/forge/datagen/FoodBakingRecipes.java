package com.impact.realisticfood.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.crafting.WaterBottleIngredient;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.lib.forge.datagen.GrindingRecipeBuilder;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.lib.grinding.GrinderTier;
import com.impact.lib.grinding.Grinding;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.cooking.CookingMethod;
import com.impact.realisticfood.cooking.StyleCooking;
import com.impact.realisticfood.food.BakingIngredient;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.forge.condition.FeatureEnabledCondition;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.function.Consumer;

/**
 * Baking: Flour from wheat in a stone grinder, Dough from Flour and any water bottle, Loaf Dough, the doughs' furnace,
 * campfire and boiling results, cake, cookies, pumpkin pie and porridge from Flour, and vanilla's grid bread, cake,
 * cookies and pie, which stay only while baking is off. Also the grinders' recipes without Crafting.
 */
final class FoodBakingRecipes {
    private static final String BAKING_FOLDER = "baking/";
    private static final float WHEAT_SEED_CHANCE = 0.5F;
    private static final int DOUGH_PER_BATCH = 3;
    private static final int LOAF_DOUGH_PIECES = 3;
    private static final int COOKIES_PER_BATCH = 8;
    private static final int DOUGH_BOIL_TICKS = 200;
    private static final float BAKING_EXPERIENCE = 0.35F;

    private FoodBakingRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> bakingOutput = conditional(output, new FeatureEnabledCondition(FoodFeature.BAKING));
        Item flour = FoodItems.bakingIngredient(BakingIngredient.FLOUR);
        Item dough = FoodItems.bakingIngredient(BakingIngredient.DOUGH);
        Item loafDough = FoodItems.bakingIngredient(BakingIngredient.LOAF_DOUGH);
        GrindingRecipeBuilder.of(flour, 1).requires(Items.WHEAT).minimumTier(GrinderTier.STONE)
                .byproduct(Items.WHEAT_SEEDS, 1, WHEAT_SEED_CHANCE)
                .save(bakingOutput, bakingId("flour"));
        ToolCraftingRecipeBuilder.of(dough, DOUGH_PER_BATCH).requires(flour).requires(flour).requires(flour)
                .requires(WaterBottleIngredient.INSTANCE)
                .remainder(WaterBottleIngredient.INSTANCE, Items.GLASS_BOTTLE)
                .save(bakingOutput, bakingId("dough"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, loafDough).pattern("DDD").define('D', dough)
                .unlockedBy("has_dough", InventoryChangeTrigger.TriggerInstance.hasItems(dough))
                .save(bakingOutput, bakingId("loaf_dough"));
        doughCooking(bakingOutput, BakingIngredient.LOAF_DOUGH, Items.BREAD, FixedFood.FLATBREAD, LOAF_DOUGH_PIECES, FixedFood.DUMPLING, LOAF_DOUGH_PIECES);
        doughCooking(bakingOutput, BakingIngredient.DOUGH, FoodItems.fixedFood(FixedFood.DOUGH_BUN), FixedFood.FLATBREAD, 1, FixedFood.DUMPLING, 1);
        flourRecipes(bakingOutput, flour);
        porridge(output, flour);
        vanillaGridBakingWithoutBaking(output);
        grinders(output);
    }

    // A dough bakes in a furnace or smoker, goes flat on a campfire, and boils into dumplings.
    private static void doughCooking(Consumer<FinishedRecipe> output, BakingIngredient doughKind, Item baked, FixedFood campfireFood, int campfireCount,
                                     FixedFood boiledFood, int boiledCount) {
        Item dough = FoodItems.bakingIngredient(doughKind);
        Ingredient input = Ingredient.of(dough);
        InventoryChangeTrigger.TriggerInstance hasDough = InventoryChangeTrigger.TriggerInstance.hasItems(dough);
        int furnaceTicks = StyleCooking.rawTicks(CookingMethod.SMELTING).defaultValue();
        int smokerTicks = StyleCooking.rawTicks(CookingMethod.SMOKING).defaultValue();
        int campfireTicks = StyleCooking.rawTicks(CookingMethod.CAMPFIRE).defaultValue();
        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.FOOD, baked, BAKING_EXPERIENCE, furnaceTicks)
                .unlockedBy("has_dough", hasDough).save(output, StyleCooking.bakingRecipeId(doughKind, CookingMethod.SMELTING));
        SimpleCookingRecipeBuilder.smoking(input, RecipeCategory.FOOD, baked, BAKING_EXPERIENCE, smokerTicks)
                .unlockedBy("has_dough", hasDough).save(output, StyleCooking.bakingRecipeId(doughKind, CookingMethod.SMOKING));
        Item flatFood = FoodItems.fixedFood(campfireFood);
        SimpleCookingRecipeBuilder.campfireCooking(input, RecipeCategory.FOOD, flatFood, BAKING_EXPERIENCE, campfireTicks)
                .unlockedBy("has_dough", hasDough)
                .save(recipe -> output.accept(campfireCount == 1 ? recipe : new CookingResultCount(recipe, flatFood, campfireCount)),
                        StyleCooking.bakingRecipeId(doughKind, CookingMethod.CAMPFIRE));
        BoilingRecipeBuilder.boiling(input, FoodItems.fixedFood(boiledFood), DOUGH_BOIL_TICKS).resultCount(boiledCount)
                .save(conditional(output, new FeatureEnabledCondition(FoodFeature.BOILING)), bakingId(doughKind.itemPath + "_boiling"));
    }

    private static void flourRecipes(Consumer<FinishedRecipe> output, Item flour) {
        InventoryChangeTrigger.TriggerInstance hasFlour = InventoryChangeTrigger.TriggerInstance.hasItems(flour);
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.CAKE)
                .pattern(" M ")
                .pattern("SES")
                .pattern("FFF")
                .define('M', Items.MILK_BUCKET).define('S', Items.SUGAR).define('E', Items.EGG).define('F', flour)
                .unlockedBy("has_flour", hasFlour).save(output, bakingId("cake"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.COOKIE, COOKIES_PER_BATCH)
                .pattern("FCF")
                .define('F', flour).define('C', Items.COCOA_BEANS)
                .unlockedBy("has_flour", hasFlour).save(output, bakingId("cookie"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.PUMPKIN_PIE)
                .requires(Items.PUMPKIN).requires(Items.SUGAR).requires(Items.EGG).requires(flour)
                .unlockedBy("has_flour", hasFlour).save(output, bakingId("pumpkin_pie"));
    }

    private static void porridge(Consumer<FinishedRecipe> output, Item flour) {
        Consumer<FinishedRecipe> soupOutput = conditional(output, new FeatureEnabledCondition(FoodFeature.BOILING),
                new FeatureEnabledCondition(FoodFeature.SOUPS), new FeatureEnabledCondition(FoodFeature.BAKING));
        SoupRecipeBuilder.soup(FoodItems.fixedFood(FixedFood.PORRIDGE)).requires(flour, 2).requires(Items.MILK_BUCKET, 1)
                .save(soupOutput, ResourceIds.of(FoodMod.MOD_ID, "soup/porridge"));
    }

    // Vanilla's own recipes under their IDs, for when baking is off; with it on, bread, cake, cookies and pie take Flour.
    private static void vanillaGridBakingWithoutBaking(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> withoutBaking = conditional(output, new NotCondition(new FeatureEnabledCondition(FoodFeature.BAKING)));
        InventoryChangeTrigger.TriggerInstance hasWheat = InventoryChangeTrigger.TriggerInstance.hasItems(Items.WHEAT);
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.BREAD).pattern("###").define('#', Items.WHEAT)
                .unlockedBy("has_wheat", hasWheat).save(withoutBaking, ResourceIds.itemId(Items.BREAD));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.CAKE)
                .pattern("AAA")
                .pattern("BEB")
                .pattern("CCC")
                .define('A', Items.MILK_BUCKET).define('B', Items.SUGAR).define('C', Items.WHEAT).define('E', Items.EGG)
                .unlockedBy("has_egg", InventoryChangeTrigger.TriggerInstance.hasItems(Items.EGG)).save(withoutBaking, ResourceIds.itemId(Items.CAKE));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.COOKIE, COOKIES_PER_BATCH).pattern("#X#")
                .define('#', Items.WHEAT).define('X', Items.COCOA_BEANS)
                .unlockedBy("has_cocoa", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COCOA_BEANS)).save(withoutBaking, ResourceIds.itemId(Items.COOKIE));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.PUMPKIN_PIE).requires(Items.PUMPKIN).requires(Items.SUGAR).requires(Items.EGG)
                .unlockedBy("has_pumpkin", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PUMPKIN))
                .save(withoutBaking, ResourceIds.itemId(Items.PUMPKIN_PIE));
    }

    // Lib's grinders from vanilla materials, without Crafting (its parts make them otherwise). None is a two-item stick
    // and planks shape, which Salty's Realistic Forging's wood pestle takes.
    private static void grinders(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> aloneOutput = conditional(output, ConditionalRecipes.modNotLoaded(FoodMod.CRAFTING_MOD_ID));
        // A bowl set on a plank: planks alone in a bowl shape are vanilla's boat.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Grinding.woodenBowl()).pattern("B").pattern("P").define('B', Items.BOWL).define('P', ItemTags.PLANKS)
                .unlockedBy("has_bowl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOWL)).save(aloneOutput, bakingId("wooden_grinder_bowl"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Grinding.stoneBowl()).pattern("C C").pattern("CCC").define('C', ItemTags.STONE_TOOL_MATERIALS)
                .unlockedBy("has_cobblestone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COBBLESTONE)).save(aloneOutput, bakingId("stone_grinder_bowl"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Grinding.woodenStick()).pattern("S").pattern("S").pattern("P")
                .define('S', Items.STICK).define('P', ItemTags.PLANKS)
                .unlockedBy("has_stick", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK)).save(aloneOutput, bakingId("wooden_grinding_stick"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Grinding.stoneStick()).pattern("S").pattern("S").pattern("C")
                .define('S', Items.STICK).define('C', ItemTags.STONE_TOOL_MATERIALS)
                .unlockedBy("has_cobblestone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COBBLESTONE)).save(aloneOutput, bakingId("stone_grinding_stick"));
    }

    private static ResourceLocation bakingId(String name) {
        return ResourceIds.of(FoodMod.MOD_ID, BAKING_FOLDER + name);
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
