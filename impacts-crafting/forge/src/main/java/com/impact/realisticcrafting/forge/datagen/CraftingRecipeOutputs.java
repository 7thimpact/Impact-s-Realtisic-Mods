package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
import com.impact.realisticcrafting.forge.condition.RecipeEnabledCondition;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import java.util.function.Consumer;

/** Shared pieces of Crafting's recipe generators: conditional outputs, recipe IDs, the tool ingredients and unlock criteria. */
final class CraftingRecipeOutputs {
    private CraftingRecipeOutputs() {
    }

    /** Shared parts load unless their ID is in {@code recipes.disabled}. */
    static Consumer<FinishedRecipe> partRecipe(Consumer<FinishedRecipe> output) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, new RecipeEnabledCondition(recipe.getId())));
    }

    /** Replacements of vanilla recipes also follow their catalog category. */
    static Consumer<FinishedRecipe> overhaulRecipe(Consumer<FinishedRecipe> output, OverhaulCategory category) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, new OverhaulEnabledCondition(category, recipe.getId())));
    }

    static ResourceLocation id(String path) {
        return ResourceIds.of(CraftingMod.MOD_ID, path);
    }

    static Ingredient trimmingTools() {
        return Ingredient.of(CraftingTags.TRIMMING_TOOLS);
    }

    static Ingredient chisels() {
        return Ingredient.of(CraftingItems.tool(CraftingTool.FLINT_CHISEL), CraftingItems.tool(CraftingTool.IRON_CHISEL));
    }

    static Ingredient tool(CraftingTool tool) {
        return Ingredient.of(CraftingItems.tool(tool));
    }

    static Ingredient boneNeedles() {
        return Ingredient.of(CraftingItems.tool(CraftingTool.BONE_NEEDLE));
    }

    /** Only an unused, empty Wooden Bucket works as a crafting part: no uses tag at all. */
    static Ingredient unusedWoodenBucket() {
        return StrictNBTIngredient.of(new ItemStack(CraftingItems.woodenBucket()));
    }

    static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
    }
}
