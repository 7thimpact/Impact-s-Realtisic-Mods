package com.impact.realisticfood.forge.compat.jei;

import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingRecipes;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.lang.FoodTranslations;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.soup.SoupRecipes;
import com.impact.realisticfood.spit.Spits;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/**
 * Food's JEI plugin: Boiling, Soups and Spit Roasting categories, info pages on the three cooking styles, the spit and
 * the campfire bucket. Cutting shows in Lib's Tool Crafting category. JEI finds it on its own.
 */
@JeiPlugin
public final class FoodJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceIds.of(FoodMod.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new BoilingCategory(guiHelper), new SoupCategory(guiHelper), new SpitRoastingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipes = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(FoodJeiRecipeTypes.BOILING, recipes.getAllRecipesFor(BoilingRecipes.recipeType()));
        registration.addRecipes(FoodJeiRecipeTypes.SOUP, recipes.getAllRecipesFor(SoupRecipes.recipeType()));
        registration.addRecipes(FoodJeiRecipeTypes.SPIT_ROASTING, recipes.getAllRecipesFor(RecipeType.CAMPFIRE_COOKING));
        for (FoodStyle style : FoodStyle.values()) {
            List<ItemStack> styled = FoodItems.styledFoods(style).stream().map(ItemStack::new).toList();
            registration.addIngredientInfo(styled, VanillaTypes.ITEM_STACK, Component.translatable(FoodTranslations.JEI_STYLES_INFO),
                    Component.translatable(FoodTranslations.jeiStyleInfo(style)));
        }
        registration.addIngredientInfo(new ItemStack(Spits.spitItem()), VanillaTypes.ITEM_STACK, Component.translatable(FoodTranslations.JEI_SPIT_INFO));
        registration.addIngredientInfo(new ItemStack(Items.WATER_BUCKET), VanillaTypes.ITEM_STACK, Component.translatable(FoodTranslations.JEI_CAMPFIRE_BUCKET_INFO));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(FoodJeiRecipeTypes.BOILING, Items.CAULDRON, Items.WATER_BUCKET);
        registration.addRecipeCatalysts(FoodJeiRecipeTypes.SOUP, Items.CAULDRON, Items.WATER_BUCKET, Items.BOWL);
        registration.addRecipeCatalysts(FoodJeiRecipeTypes.SPIT_ROASTING, Spits.spitItem(), Items.CAMPFIRE);
    }
}
