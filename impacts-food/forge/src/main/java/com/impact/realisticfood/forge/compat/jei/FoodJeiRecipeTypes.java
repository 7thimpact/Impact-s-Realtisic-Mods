package com.impact.realisticfood.forge.compat.jei;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingRecipe;
import com.impact.realisticfood.soup.SoupRecipe;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;

/** Food's JEI categories. Cutting shows in Lib's Tool Crafting category instead. */
final class FoodJeiRecipeTypes {
    static final RecipeType<BoilingRecipe> BOILING = RecipeType.create(FoodMod.MOD_ID, "boiling", BoilingRecipe.class);
    static final RecipeType<SoupRecipe> SOUP = RecipeType.create(FoodMod.MOD_ID, "soup", SoupRecipe.class);
    static final RecipeType<CampfireCookingRecipe> SPIT_ROASTING = RecipeType.create(FoodMod.MOD_ID, "spit_roasting", CampfireCookingRecipe.class);

    private FoodJeiRecipeTypes() {
    }
}
