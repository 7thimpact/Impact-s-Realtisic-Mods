package com.impact.realisticfood.forge.compat.jei;

import com.impact.realisticfood.boiling.BoilingRecipe;
import com.impact.realisticfood.boiling.BoilingRecipes;
import com.impact.realisticfood.lang.FoodTranslations;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Boiling single items: the input, the boiled result, how long it takes, and which pots do it. */
final class BoilingCategory implements IRecipeCategory<BoilingRecipe> {
    private static final int INPUT_X = 1;
    private static final int ROW_Y = 5;
    private static final int ARROW_X = 26;
    private static final int RESULT_X = 60;
    private static final int TEXT_Y = 28;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private final IDrawable icon;

    BoilingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(Items.CAULDRON));
    }

    @Override
    public RecipeType<BoilingRecipe> getRecipeType() {
        return FoodJeiRecipeTypes.BOILING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(FoodTranslations.JEI_BOILING);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BoilingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, ROW_Y).setStandardSlotBackground().addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).setOutputSlotBackground().addItemStack(recipe.result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BoilingRecipe recipe, IFocusGroup focuses) {
        int boilTicks = boilTicks(recipe);
        builder.addAnimatedRecipeArrowWidget(boilTicks).setPosition(ARROW_X, ROW_Y);
        Component vessel = Component.translatable(FoodTranslations.jeiVessel(recipe.vessel()));
        builder.addText(Component.translatable(FoodTranslations.JEI_POT_AND_TIME, vessel, JeiTimes.seconds(boilTicks)), WIDTH, TEXT_Y).setPosition(0, TEXT_Y);
    }

    private static int boilTicks(BoilingRecipe recipe) {
        ItemStack[] inputs = recipe.input().getItems();
        return inputs.length == 0 ? recipe.recipeBoilTicks() : BoilingRecipes.boilTicks(recipe, inputs[0]);
    }
}
