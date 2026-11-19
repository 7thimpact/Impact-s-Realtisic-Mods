package com.impact.realisticfood.forge.compat.jei;

import com.impact.realisticfood.lang.FoodTranslations;
import com.impact.realisticfood.soup.SoupRecipe;
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

/** Soups: one set of ingredients, the soup, how many bowls a set makes, and the cook time. */
final class SoupCategory implements IRecipeCategory<SoupRecipe> {
    private static final int SLOT_SIZE = 18;
    private static final int ROW_Y = 5;
    private static final int ARROW_X = 76;
    private static final int RESULT_X = 110;
    private static final int TEXT_Y = 28;
    private static final int WIDTH = 140;
    private static final int HEIGHT = 40;

    private final IDrawable icon;

    SoupCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(Items.MUSHROOM_STEW));
    }

    @Override
    public RecipeType<SoupRecipe> getRecipeType() {
        return FoodJeiRecipeTypes.SOUP;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(FoodTranslations.JEI_SOUP);
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
    public void setRecipe(IRecipeLayoutBuilder builder, SoupRecipe recipe, IFocusGroup focuses) {
        for (int index = 0; index < recipe.getIngredients().size(); index++) {
            builder.addSlot(RecipeIngredientRole.INPUT, index * SLOT_SIZE + 1, ROW_Y).setStandardSlotBackground()
                    .addIngredients(recipe.getIngredients().get(index));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).setOutputSlotBackground()
                .addItemStack(recipe.result().copyWithCount(recipe.bowlsPerSet()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SoupRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(recipe.cookTicks()).setPosition(ARROW_X, ROW_Y);
        Component details = Component.translatable(FoodTranslations.JEI_SOUP_DETAILS, recipe.bowlsPerSet(), JeiTimes.seconds(recipe.cookTicks()));
        builder.addText(details, WIDTH, TEXT_Y).setPosition(0, TEXT_Y);
    }
}
