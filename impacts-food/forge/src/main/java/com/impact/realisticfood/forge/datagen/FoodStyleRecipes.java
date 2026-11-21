package com.impact.realisticfood.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.cooking.CookingMethod;
import com.impact.realisticfood.cooking.StyleCooking;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.forge.condition.FeatureEnabledCondition;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The spec's "Cooking any food" recipes: cooked food dries in a furnace or smoker, toasts on a campfire and boils; raw
 * food boils straight into its boiled version; dried and toasted food burns; finished food boils into Mush; and the
 * new raw foods (tropical fish, rotten flesh, eggs) cook. Vanilla's own raw-to-cooked recipes stay as they are.
 */
final class FoodStyleRecipes {
    private static final float STYLE_EXPERIENCE = 0.1F;
    private static final float RAW_EXPERIENCE = 0.35F;
    private static final float BURNT_EXPERIENCE = 0.0F;
    private static final String BOILING_FOLDER = "boiling/";
    private static final int MUSH_BOIL_TICKS = 200;

    private FoodStyleRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        for (FoodBase base : FoodBase.values()) {
            driedAndToasted(output, base);
            boiled(output, base);
        }
        newRawFoods(output);
        overcooking(output);
    }

    private static void driedAndToasted(Consumer<FinishedRecipe> output, FoodBase base) {
        Ingredient cooked = Ingredient.of(base.baseItem());
        String unlockName = "has_" + base.path;
        FoodItems.styled(base, FoodStyle.DRIED).ifPresent(dried -> {
            Consumer<FinishedRecipe> driedOutput = styleOutput(output, FoodStyle.DRIED);
            SimpleCookingRecipeBuilder.smelting(cooked, RecipeCategory.FOOD, dried, STYLE_EXPERIENCE, dryHeatTicks(CookingMethod.SMELTING))
                    .unlockedBy(unlockName, InventoryChangeTrigger.TriggerInstance.hasItems(base.baseItem()))
                    .save(driedOutput, StyleCooking.styledRecipeId(base, FoodStyle.DRIED, CookingMethod.SMELTING));
            SimpleCookingRecipeBuilder.smoking(cooked, RecipeCategory.FOOD, dried, STYLE_EXPERIENCE, dryHeatTicks(CookingMethod.SMOKING))
                    .unlockedBy(unlockName, InventoryChangeTrigger.TriggerInstance.hasItems(base.baseItem()))
                    .save(driedOutput, StyleCooking.styledRecipeId(base, FoodStyle.DRIED, CookingMethod.SMOKING));
        });
        FoodItems.styled(base, FoodStyle.TOASTED).ifPresent(toasted ->
                SimpleCookingRecipeBuilder.campfireCooking(cooked, RecipeCategory.FOOD, toasted, STYLE_EXPERIENCE, dryHeatTicks(CookingMethod.CAMPFIRE))
                        .unlockedBy(unlockName, InventoryChangeTrigger.TriggerInstance.hasItems(base.baseItem()))
                        .save(styleOutput(output, FoodStyle.TOASTED), StyleCooking.styledRecipeId(base, FoodStyle.TOASTED, CookingMethod.CAMPFIRE)));
    }

    // Cooked food boils into its boiled version, and so does its raw form, in one step.
    private static void boiled(Consumer<FinishedRecipe> output, FoodBase base) {
        Item boiled = FoodItems.styled(base, FoodStyle.BOILED).orElseThrow();
        Consumer<FinishedRecipe> boiledOutput = conditional(output, styleConditions(FoodStyle.BOILED, new FeatureEnabledCondition(FoodFeature.BOILING)));
        BoilingRecipeBuilder.boiling(Ingredient.of(base.baseItem()), boiled, base.boilTime.cookedBoilTicks())
                .save(boiledOutput, boilingId(boiled, base.baseItem()));
        base.rawItem().ifPresent(raw -> BoilingRecipeBuilder.boiling(Ingredient.of(raw), boiled, base.boilTime.rawBoilTicks)
                .save(boiledOutput, boilingId(boiled, raw)));
    }

    private static void newRawFoods(Consumer<FinishedRecipe> output) {
        rawCooking(output, Items.TROPICAL_FISH, FixedFood.COOKED_TROPICAL_FISH, List.of(CookingMethod.values()));
        rawCooking(output, Items.ROTTEN_FLESH, FixedFood.COOKED_ROTTEN_FLESH, List.of(CookingMethod.values()));
        // An egg would crack in a furnace; it fries on a campfire.
        rawCooking(output, Items.EGG, FixedFood.FRIED_EGG, List.of(CookingMethod.CAMPFIRE));
    }

    private static void rawCooking(Consumer<FinishedRecipe> output, Item raw, FixedFood cooked, List<CookingMethod> methods) {
        Ingredient input = Ingredient.of(raw);
        Item result = FoodItems.fixedFood(cooked);
        for (CookingMethod method : methods) {
            cookingBuilder(method, input, result, RAW_EXPERIENCE, StyleCooking.rawTicks(method).defaultValue())
                    .unlockedBy("has_" + ResourceIds.itemId(raw).getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(raw))
                    .save(output, StyleCooking.rawRecipeId(cooked.itemPath, method));
        }
    }

    // Dry heat burns dried and toasted food, fried eggs, buns and flatbread (and dried kelp, which is already dried);
    // boiling any finished food makes Mush.
    private static void overcooking(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> overcookingOutput = conditional(output, new FeatureEnabledCondition(FoodFeature.STYLES),
                new FeatureEnabledCondition(FoodFeature.OVERCOOKING));
        Item burntFood = FoodItems.fixedFood(FixedFood.BURNT_FOOD);
        List<Item> dryFinishedFoods = List.of(FoodItems.fixedFood(FixedFood.FRIED_EGG), FoodItems.fixedFood(FixedFood.DOUGH_BUN),
                FoodItems.fixedFood(FixedFood.FLATBREAD));
        Ingredient burnsInFurnace = anyOf(List.of(FoodTags.DRIED_FOODS, FoodTags.TOASTED_FOODS),
                Stream.concat(dryFinishedFoods.stream(), Stream.of(Items.DRIED_KELP)).toList());
        Ingredient burnsOnCampfire = anyOf(List.of(FoodTags.DRIED_FOODS, FoodTags.TOASTED_FOODS), dryFinishedFoods);
        for (CookingMethod method : CookingMethod.values()) {
            Ingredient input = method == CookingMethod.CAMPFIRE ? burnsOnCampfire : burnsInFurnace;
            cookingBuilder(method, input, burntFood, BURNT_EXPERIENCE, dryHeatTicks(method))
                    .unlockedBy("has_styled_food", hasAny(FoodTags.STYLED_FOODS))
                    .save(overcookingOutput, StyleCooking.burntRecipeId(method));
        }
        Ingredient boilsIntoMush = anyOf(List.of(FoodTags.STYLED_FOODS), Stream.concat(dryFinishedFoods.stream(),
                Stream.of(FoodItems.fixedFood(FixedFood.BOILED_EGG), FoodItems.fixedFood(FixedFood.DUMPLING), burntFood)).toList());
        BoilingRecipeBuilder.boiling(boilsIntoMush, FoodItems.fixedFood(FixedFood.MUSH), MUSH_BOIL_TICKS)
                .save(conditional(overcookingOutput, new FeatureEnabledCondition(FoodFeature.BOILING)),
                        ResourceIds.of(FoodMod.MOD_ID, BOILING_FOLDER + FixedFood.MUSH.itemPath));
    }

    private static SimpleCookingRecipeBuilder cookingBuilder(CookingMethod method, Ingredient input, ItemLike result, float experience, int ticks) {
        return switch (method) {
            case SMELTING -> SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.FOOD, result, experience, ticks);
            case SMOKING -> SimpleCookingRecipeBuilder.smoking(input, RecipeCategory.FOOD, result, experience, ticks);
            case CAMPFIRE -> SimpleCookingRecipeBuilder.campfireCooking(input, RecipeCategory.FOOD, result, experience, ticks);
        };
    }

    private static Ingredient anyOf(List<TagKey<Item>> tags, List<Item> items) {
        Stream<Ingredient.Value> tagValues = tags.stream().map(Ingredient.TagValue::new);
        Stream<Ingredient.Value> itemValues = items.stream().map(item -> new Ingredient.ItemValue(new ItemStack(item)));
        return Ingredient.fromValues(Stream.concat(tagValues, itemValues));
    }

    private static InventoryChangeTrigger.TriggerInstance hasAny(TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
    }

    private static int dryHeatTicks(CookingMethod method) {
        return StyleCooking.dryHeatTicks(method).defaultValue();
    }

    /** {@code impactsfood:boiling/<result>_from_<input>}. */
    private static ResourceLocation boilingId(Item result, Item input) {
        return ResourceIds.of(FoodMod.MOD_ID, BOILING_FOLDER + ResourceIds.itemId(result).getPath() + "_from_" + ResourceIds.itemId(input).getPath());
    }

    private static ICondition[] styleConditions(FoodStyle style, ICondition... extraConditions) {
        Stream<ICondition> styleSwitches = Stream.of(new FeatureEnabledCondition(FoodFeature.STYLES), new FeatureEnabledCondition(style.feature));
        return Stream.concat(styleSwitches, Stream.of(extraConditions)).toArray(ICondition[]::new);
    }

    private static Consumer<FinishedRecipe> styleOutput(Consumer<FinishedRecipe> output, FoodStyle style) {
        return conditional(output, styleConditions(style));
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
