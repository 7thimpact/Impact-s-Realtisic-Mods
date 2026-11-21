package com.impact.realisticfood.forge.datagen;

import com.google.gson.JsonElement;
import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.lib.forge.datagen.ForeignRecipeBuilder;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.cooking.CookingMethod;
import com.impact.realisticfood.cooking.PieceCooking;
import com.impact.realisticfood.cutting.Cut;
import com.impact.realisticfood.cutting.Cuts;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.forge.condition.FeatureEnabledCondition;
import com.impact.realisticfood.item.KnifeMaterial;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import com.impact.realisticfood.spit.Spits;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Food's recipes without Crafting installed (the versions made from Crafting's parts arrive with Crafting): knives,
 * every cut, cooking for raw pieces, and boiling. With cutting on, vanilla's grid seed recipes give way to knife cutting.
 */
final class FoodRecipes extends RecipeProvider {
    private static final float PIECE_COOKING_EXPERIENCE = 0.1F;
    private static final String WITH_CRAFTING_SUFFIX = "_with_" + FoodMod.CRAFTING_MOD_ID;
    private static final String CRAFTING_TRIMMED_STICK = "impactscrafting:trimmed_stick";
    private static final String CRAFTING_FLINT_ARROWHEAD = "impactscrafting:flint_arrowhead";
    private static final String CRAFTING_IRON_PLATES = "impactscrafting:iron_plates";
    private static final int EGG_BOIL_TICKS = 200;

    FoodRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        knives(output);
        cuts(output);
        vanillaSeedsWithoutCutting(output);
        pieceCooking(output);
        boiling(output);
        FoodStyleRecipes.build(output);
        FoodSoupRecipes.build(output);
        FoodBakingRecipes.build(output);
    }

    private static void knives(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> aloneOutput = conditional(output,
                new FeatureEnabledCondition(FoodFeature.CUTTING), ConditionalRecipes.modNotLoaded(FoodMod.CRAFTING_MOD_ID));
        knife(aloneOutput, KnifeMaterial.FLINT, Items.FLINT);
        knife(aloneOutput, KnifeMaterial.IRON, Items.IRON_INGOT);
        // Three sticks in a row: two sticks would clash with Salty's Realistic Forging's two_sticks recipe.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Spits.spitItem())
                .pattern("SSS")
                .define('S', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(conditional(output, new FeatureEnabledCondition(FoodFeature.SPIT), ConditionalRecipes.modNotLoaded(FoodMod.CRAFTING_MOD_ID)));
        knivesWithCrafting(output);
    }

    // Crafting's parts, named by ID because Crafting isn't loaded while Food generates its data. The Iron Plate is a
    // stone-tier part, so Crafting puts the Iron Knife on the Makeshift Stone Table by itself.
    private static void knivesWithCrafting(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> craftingOutput = conditional(output,
                new FeatureEnabledCondition(FoodFeature.CUTTING), ConditionalRecipes.modLoaded(FoodMod.CRAFTING_MOD_ID));
        knifeWithCrafting(craftingOutput, KnifeMaterial.FLINT, ForeignRecipeBuilder.item(CRAFTING_FLINT_ARROWHEAD));
        knifeWithCrafting(craftingOutput, KnifeMaterial.IRON, ForeignRecipeBuilder.tag(CRAFTING_IRON_PLATES));
        ForeignRecipeBuilder.of(RecipeSerializer.SHAPED_RECIPE)
                .pattern("SS")
                .key('S', ForeignRecipeBuilder.item(CRAFTING_TRIMMED_STICK))
                .result(ResourceIds.itemId(Spits.spitItem()).toString(), 1)
                .save(conditional(output, new FeatureEnabledCondition(FoodFeature.SPIT), ConditionalRecipes.modLoaded(FoodMod.CRAFTING_MOD_ID)),
                        withCraftingId(ResourceIds.itemId(Spits.spitItem()).getPath()));
    }

    private static void knifeWithCrafting(Consumer<FinishedRecipe> output, KnifeMaterial material, JsonElement blade) {
        Item knife = FoodItems.knife(material);
        ForeignRecipeBuilder.of(RecipeSerializer.SHAPED_RECIPE)
                .pattern("B")
                .pattern("S")
                .key('B', blade)
                .key('S', ForeignRecipeBuilder.item(CRAFTING_TRIMMED_STICK))
                .result(ResourceIds.itemId(knife).toString(), 1)
                .save(output, withCraftingId(ResourceIds.itemId(knife).getPath()));
    }

    // Named like ConditionalRecipes.pair's recipes: flint_knife_with_impactscrafting.
    private static ResourceLocation withCraftingId(String path) {
        return ResourceIds.of(FoodMod.MOD_ID, path + WITH_CRAFTING_SUFFIX);
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

    // The egg is the one non-food that boils; styled foods' boiling recipes are in FoodStyleRecipes.
    private static void boiling(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> boilingOutput = conditional(output, new FeatureEnabledCondition(FoodFeature.BOILING));
        BoilingRecipeBuilder.boiling(Ingredient.of(Items.EGG), FoodItems.fixedFood(FixedFood.BOILED_EGG), EGG_BOIL_TICKS)
                .save(boilingOutput, ResourceIds.of(FoodMod.MOD_ID, "boiling/boiled_egg"));
    }

    private static int defaultTicks(CookingMethod method) {
        return PieceCooking.ticksSetting(method).defaultValue();
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
