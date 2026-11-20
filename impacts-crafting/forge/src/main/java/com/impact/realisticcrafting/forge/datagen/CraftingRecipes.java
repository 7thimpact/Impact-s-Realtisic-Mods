package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * Crafting's recipes. Each loads only while its catalog category is on and its ID isn't in {@code recipes.disabled}.
 * The vanilla recipes they replace are switched off by {@link VanillaRecipeOverrides}.
 */
final class CraftingRecipes extends RecipeProvider {
    CraftingRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, CraftingItems.table(MakeshiftTable.WOODEN))
                .pattern("PP")
                .pattern("PP")
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(inCategory(output, OverhaulCategory.WOOD), id(MakeshiftTable.WOODEN.path));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, CraftingItems.table(MakeshiftTable.STONE))
                .pattern("FF")
                .pattern("SS")
                .pattern("SS")
                .define('F', Items.FLINT)
                .define('S', Items.SMOOTH_STONE)
                .unlockedBy("has_smooth_stone", has(Items.SMOOTH_STONE))
                .save(inCategory(output, OverhaulCategory.STONE), id(MakeshiftTable.STONE.path));
    }

    private static ResourceLocation id(String path) {
        return ResourceIds.of(CraftingMod.MOD_ID, path);
    }

    // Each recipe is gated by its category and by its own ID, so recipes.disabled can drop it.
    private static Consumer<FinishedRecipe> inCategory(Consumer<FinishedRecipe> output, OverhaulCategory category) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, new OverhaulEnabledCondition(category, recipe.getId())));
    }
}
