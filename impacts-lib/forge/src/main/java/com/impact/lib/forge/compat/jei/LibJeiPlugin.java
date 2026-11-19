package com.impact.lib.forge.compat.jei;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.lib.grinding.Grinding;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/** Lib's JEI plugin: the shared Tool Crafting and Grinding categories, filled from the live recipe set. JEI finds it on its own. */
@JeiPlugin
public final class LibJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceIds.of(ImpactsLib.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new ToolCraftingCategory(guiHelper), new GrindingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipes = Minecraft.getInstance().level.getRecipeManager();
        List<ToolCraftingRecipe> toolRecipes = recipes.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(ToolCraftingRecipe.class::isInstance)
                .map(ToolCraftingRecipe.class::cast)
                .toList();
        registration.addRecipes(LibJeiRecipeTypes.TOOL_CRAFTING, toolRecipes);
        registration.addRecipes(LibJeiRecipeTypes.GRINDING, recipes.getAllRecipesFor(Grinding.recipeType()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(LibJeiRecipeTypes.TOOL_CRAFTING, Items.CRAFTING_TABLE);
        registration.addRecipeCatalysts(LibJeiRecipeTypes.GRINDING, Grinding.woodenBowl(), Grinding.stoneBowl(), Grinding.woodenStick(), Grinding.stoneStick());
    }
}
