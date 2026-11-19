package com.impact.lib.forge.compat.jei;

import com.impact.lib.ImpactsLib;
import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.lib.grinding.GrindingRecipe;
import mezz.jei.api.recipe.RecipeType;

/** Lib's shared JEI categories. Other Impact mods add catalysts to these instead of making their own. */
public final class LibJeiRecipeTypes {
    public static final RecipeType<ToolCraftingRecipe> TOOL_CRAFTING = RecipeType.create(ImpactsLib.MOD_ID, "tool_crafting", ToolCraftingRecipe.class);
    public static final RecipeType<GrindingRecipe> GRINDING = RecipeType.create(ImpactsLib.MOD_ID, "grinding", GrindingRecipe.class);

    private LibJeiRecipeTypes() {
    }
}
