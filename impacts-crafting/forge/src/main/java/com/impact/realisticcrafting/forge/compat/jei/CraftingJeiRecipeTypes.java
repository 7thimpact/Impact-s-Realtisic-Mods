package com.impact.realisticcrafting.forge.compat.jei;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.recipe.HammeringRecipe;
import mezz.jei.api.recipe.RecipeType;

/** Crafting's two JEI categories. */
final class CraftingJeiRecipeTypes {
    static final RecipeType<HammeringRecipe> HAMMERING = RecipeType.create(CraftingMod.MOD_ID, "hammering", HammeringRecipe.class);
    static final RecipeType<VatDip> DYE_VAT = RecipeType.create(CraftingMod.MOD_ID, "dye_vat", VatDip.class);

    private CraftingJeiRecipeTypes() {
    }
}
