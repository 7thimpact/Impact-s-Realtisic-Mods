package com.impact.realisticcrafting.forge.compat.jei;

import com.impact.realisticcrafting.lang.CraftingTranslations;
import com.impact.realisticcrafting.recipe.Hammering;
import com.impact.realisticcrafting.recipe.HammeringRecipe;
import com.impact.realisticcrafting.registry.CraftingItems;
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

import java.util.Arrays;
import java.util.IntSummaryStatistics;

/** Hammering a loose item: the loose item (main hand), the hammers that work (off hand), the result, and how long to hold. */
final class HammeringCategory implements IRecipeCategory<HammeringRecipe> {
    private static final int LOOSE_X = 1;
    private static final int HAMMER_X = 21;
    private static final int ROW_Y = 5;
    private static final int ARROW_X = 46;
    private static final int RESULT_X = 80;
    private static final int TEXT_Y = 28;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private final IDrawable icon;

    HammeringCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(CraftingItems.stonePebble()));
    }

    @Override
    public RecipeType<HammeringRecipe> getRecipeType() {
        return CraftingJeiRecipeTypes.HAMMERING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(CraftingTranslations.JEI_HAMMERING);
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
    public void setRecipe(IRecipeLayoutBuilder builder, HammeringRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, LOOSE_X, ROW_Y).setStandardSlotBackground().addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.CATALYST, HAMMER_X, ROW_Y).setStandardSlotBackground().addIngredients(recipe.hammer());
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).setOutputSlotBackground().addItemStack(recipe.result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, HammeringRecipe recipe, IFocusGroup focuses) {
        IntSummaryStatistics holdTicks = Arrays.stream(recipe.hammer().getItems()).mapToInt(hammer -> Hammering.ticksFor(recipe, hammer)).summaryStatistics();
        int slowestTicks = holdTicks.getCount() == 0 ? 0 : holdTicks.getMax();
        builder.addAnimatedRecipeArrowWidget(slowestTicks).setPosition(ARROW_X, ROW_Y);
        Component holdText = holdTicks.getMin() == slowestTicks
                ? Component.translatable(CraftingTranslations.JEI_HOLD_TIME, JeiText.seconds(slowestTicks))
                : Component.translatable(CraftingTranslations.JEI_HOLD_TIME_RANGE, JeiText.seconds(holdTicks.getMin()), JeiText.seconds(slowestTicks));
        builder.addText(holdText, WIDTH, TEXT_Y).setPosition(0, TEXT_Y);
    }
}
