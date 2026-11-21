package com.impact.realisticvitality.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.forge.condition.FeatureEnabledCondition;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.function.Consumer;

/** Vitality's recipes: purifying any water bottle, Cold Water, the drink containers, water blocks, clothing and breathing gear. */
final class VitalityRecipes extends RecipeProvider {
    private static final float PURIFYING_EXPERIENCE = 0.1F;
    private static final int FURNACE_TICKS = 200;
    private static final int SMOKER_TICKS = 100;
    private static final int CAMPFIRE_TICKS = 600;

    VitalityRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> thirstOutput = conditional(output, FeatureEnabledCondition.of(VitalityNeed.THIRST.configKey));
        purifying(thirstOutput, Ingredient.of(VitalityItems.waterBottle(BottledWater.UNCLEAN)), BottledWater.UNCLEAN.itemPath);
        purifying(thirstOutput, Ingredient.of(VitalityItems.waterBottle(BottledWater.SALT)), BottledWater.SALT.itemPath);
        purifying(thirstOutput, vanillaWaterBottle(), "water_bottle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, VitalityItems.waterBottle(BottledWater.COLD))
                .requires(VitalityItems.waterBottle(BottledWater.PURIFIED))
                .requires(Items.ICE)
                .unlockedBy("has_purified_water", has(VitalityItems.waterBottle(BottledWater.PURIFIED)))
                .save(thirstOutput);
        VitalityContainerRecipes.build(output);
        VitalityBreathingRecipes.build(output);
        VitalityCraftingRecipes.build(output);
    }

    private static void purifying(Consumer<FinishedRecipe> output, Ingredient water, String waterName) {
        Item purified = VitalityItems.waterBottle(BottledWater.PURIFIED);
        String unlockName = "has_" + waterName;
        SimpleCookingRecipeBuilder.smelting(water, RecipeCategory.FOOD, purified, PURIFYING_EXPERIENCE, FURNACE_TICKS)
                .unlockedBy(unlockName, inventoryTrigger(water))
                .save(output, ResourceIds.of(VitalityMod.MOD_ID, "purified_water_from_smelting_" + waterName));
        SimpleCookingRecipeBuilder.smoking(water, RecipeCategory.FOOD, purified, PURIFYING_EXPERIENCE, SMOKER_TICKS)
                .unlockedBy(unlockName, inventoryTrigger(water))
                .save(output, ResourceIds.of(VitalityMod.MOD_ID, "purified_water_from_smoking_" + waterName));
        SimpleCookingRecipeBuilder.campfireCooking(water, RecipeCategory.FOOD, purified, PURIFYING_EXPERIENCE, CAMPFIRE_TICKS)
                .unlockedBy(unlockName, inventoryTrigger(water))
                .save(output, ResourceIds.of(VitalityMod.MOD_ID, "purified_water_from_campfire_cooking_" + waterName));
    }

    // The recipe book unlocks the recipe once the player holds the water it purifies.
    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(Ingredient water) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(water.getItems()[0].getItem()).build());
    }

    // A vanilla water bottle is a potion with the water potion in its NBT.
    private static Ingredient vanillaWaterBottle() {
        CompoundTag waterPotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.WATER).getOrCreateTag().copy();
        return PartialNBTIngredient.of(Items.POTION, waterPotion);
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
