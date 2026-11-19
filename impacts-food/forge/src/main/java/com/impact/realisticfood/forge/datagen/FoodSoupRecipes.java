package com.impact.realisticfood.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.forge.condition.FeatureEnabledCondition;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.function.Consumer;

/**
 * Soups cooked in pots (the spec's soup table, one set per recipe), and vanilla's grid stews, which stay only while
 * soups or the stew overhaul are off. Porridge arrives with baking, which adds Flour.
 */
final class FoodSoupRecipes {
    private static final String SOUP_FOLDER = "soup/";

    private FoodSoupRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> soupOutput = conditional(output, new FeatureEnabledCondition(FoodFeature.BOILING),
                new FeatureEnabledCondition(FoodFeature.SOUPS));
        Ingredient anyMushroom = Ingredient.of(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
        SoupRecipeBuilder.soup(Items.MUSHROOM_STEW).requires(Items.BROWN_MUSHROOM, 1).requires(Items.RED_MUSHROOM, 1)
                .save(soupOutput, soupId("mushroom_stew"));
        SoupRecipeBuilder.soup(Items.BEETROOT_SOUP).requires(Items.BEETROOT, 3).save(soupOutput, soupId("beetroot_soup"));
        SoupRecipeBuilder.soup(Items.RABBIT_STEW).requires(FoodItems.piece(FoodPiece.RAW_RABBIT_PIECE), 1)
                .requires(FoodItems.piece(FoodPiece.CARROT_SLICES), 1).requires(FoodItems.piece(FoodPiece.POTATO_SLICES), 1).requires(anyMushroom)
                .save(soupOutput, soupId("rabbit_stew"));
        SoupRecipeBuilder.soup(FoodItems.fixedFood(FixedFood.BONE_BROTH)).requires(Items.BONE, 3).save(soupOutput, soupId("bone_broth"));
        SoupRecipeBuilder.soup(FoodItems.fixedFood(FixedFood.HEARTY_STEW)).requires(Ingredient.of(FoodTags.MEAT_CUTS))
                .requires(FoodItems.piece(FoodPiece.POTATO_SLICES), 1).requires(FoodItems.piece(FoodPiece.CARROT_SLICES), 1)
                .save(soupOutput, soupId("hearty_stew"));
        SoupRecipeBuilder.soup(FoodItems.fixedFood(FixedFood.FISH_SOUP)).requires(Ingredient.of(FoodTags.FISH_FILLETS))
                .requires(FoodItems.piece(FoodPiece.POTATO_SLICES), 1).save(soupOutput, soupId("fish_soup"));
        SoupRecipeBuilder.soup(Items.SUSPICIOUS_STEW).requires(Items.BROWN_MUSHROOM, 1).requires(Items.RED_MUSHROOM, 1)
                .requires(Ingredient.of(ItemTags.SMALL_FLOWERS)).save(soupOutput, soupId("suspicious_stew"));
        vanillaGridStews(output);
    }

    // Vanilla's own recipes under their IDs: with soups and the stew overhaul on, they load nowhere, so stews come from pots.
    private static void vanillaGridStews(Consumer<FinishedRecipe> output) {
        ICondition potStewsOn = new AndCondition(new FeatureEnabledCondition(FoodFeature.SOUPS), new FeatureEnabledCondition(FoodFeature.STEW_OVERHAUL));
        Consumer<FinishedRecipe> gridOutput = conditional(output, new NotCondition(potStewsOn));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.MUSHROOM_STEW)
                .requires(Items.BROWN_MUSHROOM).requires(Items.RED_MUSHROOM).requires(Items.BOWL)
                .unlockedBy("has_bowl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOWL))
                .save(gridOutput, ResourceIds.itemId(Items.MUSHROOM_STEW));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.BEETROOT_SOUP)
                .requires(Items.BOWL).requires(Items.BEETROOT, 6)
                .unlockedBy("has_beetroot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BEETROOT))
                .save(gridOutput, ResourceIds.itemId(Items.BEETROOT_SOUP));
        rabbitStew(gridOutput, Items.BROWN_MUSHROOM, "rabbit_stew_from_brown_mushroom");
        rabbitStew(gridOutput, Items.RED_MUSHROOM, "rabbit_stew_from_red_mushroom");
        SpecialRecipeBuilder.special(RecipeSerializer.SUSPICIOUS_STEW).save(gridOutput, ResourceIds.itemId(Items.SUSPICIOUS_STEW).toString());
    }

    private static void rabbitStew(Consumer<FinishedRecipe> output, Item mushroom, String recipeName) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.RABBIT_STEW)
                .requires(Items.BAKED_POTATO).requires(Items.COOKED_RABBIT).requires(Items.BOWL).requires(Items.CARROT).requires(mushroom)
                .group("rabbit_stew")
                .unlockedBy("has_cooked_rabbit", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COOKED_RABBIT))
                .save(output, ResourceIds.of("minecraft", recipeName));
    }

    private static ResourceLocation soupId(String name) {
        return ResourceIds.of(FoodMod.MOD_ID, SOUP_FOLDER + name);
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
