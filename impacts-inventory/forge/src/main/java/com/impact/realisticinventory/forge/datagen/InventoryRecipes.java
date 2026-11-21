package com.impact.realisticinventory.forge.datagen;

import com.google.gson.JsonObject;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticinventory.forge.condition.PacksEnabledCondition;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackRecipes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.function.Consumer;

/**
 * Inventory's recipes without Crafting installed (Crafting's part-based versions arrive with Crafting). Every pack recipe
 * also needs {@code packs.enabled}. The two bigger packs are upgrades that keep the smaller pack's contents and color.
 */
final class InventoryRecipes extends RecipeProvider {
    private static final String CRAFTING_MOD_ID = "impactscrafting";

    InventoryRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        ICondition[] aloneConditions = {PacksEnabledCondition.INSTANCE, ConditionalRecipes.modNotLoaded(CRAFTING_MOD_ID)};
        Consumer<FinishedRecipe> conditionalOutput = recipe -> output.accept(ConditionalRecipes.withConditions(recipe, aloneConditions));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, InventoryItems.satchel())
                .pattern("S S")
                .pattern("LLL")
                .define('S', Items.STRING)
                .define('L', Items.LEATHER)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(conditionalOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, InventoryItems.backpack())
                .pattern("LCL")
                .pattern("LPL")
                .define('L', Items.LEATHER)
                .define('C', Items.CHEST)
                .define('P', InventoryItems.satchel())
                .unlockedBy("has_satchel", has(InventoryItems.satchel()))
                .save(recipe -> conditionalOutput.accept(new PackUpgradeType(recipe)));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, InventoryItems.framePack())
                .pattern("I I")
                .pattern("SPS")
                .pattern("I I")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .define('P', InventoryItems.backpack())
                .unlockedBy("has_backpack", has(InventoryItems.backpack()))
                .save(recipe -> conditionalOutput.accept(new PackUpgradeType(recipe)));
    }

    /** A shaped recipe written as Inventory's pack upgrade, which copies the old pack's NBT into the new one. */
    private record PackUpgradeType(FinishedRecipe shapedRecipe) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            shapedRecipe.serializeRecipeData(json);
        }

        @Override
        public ResourceLocation getId() {
            return shapedRecipe.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return PackRecipes.packUpgradeSerializer();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return shapedRecipe.serializeAdvancement();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return shapedRecipe.getAdvancementId();
        }
    }
}
