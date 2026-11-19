package com.impact.realisticfood.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.cooking.CookingMethod;
import com.impact.realisticfood.cooking.PieceCooking;
import com.impact.realisticfood.cutting.Cut;
import com.impact.realisticfood.cutting.Cuts;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.forge.condition.FeatureEnabledCondition;
import com.impact.realisticfood.item.KnifeMaterial;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Food's recipes without Crafting installed (the versions made from Crafting's parts arrive with Crafting): knives,
 * every cut, and cooking for raw pieces. With cutting on, vanilla's grid seed recipes give way to knife cutting.
 */
final class FoodRecipes extends RecipeProvider {
    private static final float PIECE_COOKING_EXPERIENCE = 0.1F;

    FoodRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        knives(output);
        cuts(output);
        vanillaSeedsWithoutCutting(output);
        pieceCooking(output);
    }

    private static void knives(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> aloneOutput = conditional(output,
                new FeatureEnabledCondition(FoodFeature.CUTTING), ConditionalRecipes.modNotLoaded(FoodMod.CRAFTING_MOD_ID));
        knife(aloneOutput, KnifeMaterial.FLINT, Items.FLINT);
        knife(aloneOutput, KnifeMaterial.IRON, Items.IRON_INGOT);
    }

    private static void knife(Consumer<FinishedRecipe> output, KnifeMaterial material, Item blade) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FoodItems.knife(material))
                .pattern("B")
                .pattern("S")
                .define('B', blade)
                .define('S', Items.STICK)
                .unlockedBy("has_blade", has(blade))
                .save(output);
    }

    private static void cuts(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> cuttingOutput = conditional(output, new FeatureEnabledCondition(FoodFeature.CUTTING));
        for (Cut cut : Cuts.ALL) {
            ToolCraftingRecipeBuilder recipe = ToolCraftingRecipeBuilder.of(cut.result().get(), cut.resultCount())
                    .requires(cut.input())
                    .tool(Ingredient.of(FoodTags.KNIVES));
            cut.byproduct().ifPresent(byproduct -> recipe.byproduct(byproduct.item(), 1, byproduct.chance()));
            recipe.save(cuttingOutput, cut.recipeId());
        }
    }

    // Vanilla's own recipes, kept under their IDs for when cutting is off; with it on, the knife cuts give the seeds.
    private static void vanillaSeedsWithoutCutting(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> withoutCutting = conditional(output, new NotCondition(new FeatureEnabledCondition(FoodFeature.CUTTING)));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MELON_SEEDS)
                .requires(Items.MELON_SLICE)
                .unlockedBy("has_melon", has(Items.MELON_SLICE))
                .save(withoutCutting, ResourceIds.itemId(Items.MELON_SEEDS));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PUMPKIN_SEEDS, 4)
                .requires(Items.PUMPKIN)
                .unlockedBy("has_pumpkin", has(Items.PUMPKIN))
                .save(withoutCutting, ResourceIds.itemId(Items.PUMPKIN_SEEDS));
    }

    private static void pieceCooking(Consumer<FinishedRecipe> output) {
        Arrays.stream(FoodPiece.values()).filter(FoodPiece::cooks).forEach(rawPiece -> {
            Item raw = FoodItems.piece(rawPiece);
            Item cooked = FoodItems.piece(rawPiece.cooked);
            Ingredient input = Ingredient.of(raw);
            String unlockName = "has_" + rawPiece.path;
            SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.FOOD, cooked, PIECE_COOKING_EXPERIENCE, defaultTicks(CookingMethod.SMELTING))
                    .unlockedBy(unlockName, has(raw))
                    .save(output, PieceCooking.recipeId(rawPiece, CookingMethod.SMELTING));
            SimpleCookingRecipeBuilder.smoking(input, RecipeCategory.FOOD, cooked, PIECE_COOKING_EXPERIENCE, defaultTicks(CookingMethod.SMOKING))
                    .unlockedBy(unlockName, has(raw))
                    .save(output, PieceCooking.recipeId(rawPiece, CookingMethod.SMOKING));
            SimpleCookingRecipeBuilder.campfireCooking(input, RecipeCategory.FOOD, cooked, PIECE_COOKING_EXPERIENCE, defaultTicks(CookingMethod.CAMPFIRE))
                    .unlockedBy(unlockName, has(raw))
                    .save(output, PieceCooking.recipeId(rawPiece, CookingMethod.CAMPFIRE));
        });
    }

    private static int defaultTicks(CookingMethod method) {
        return PieceCooking.ticksSetting(method).defaultValue();
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
