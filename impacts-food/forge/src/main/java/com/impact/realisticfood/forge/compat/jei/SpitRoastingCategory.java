package com.impact.realisticfood.forge.compat.jei;

import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.lang.FoodTranslations;
import com.impact.realisticfood.spit.Spits;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;

/** Spit roasting: every campfire recipe, with its time on a spit and while the spit is turned. */
final class SpitRoastingCategory implements IRecipeCategory<CampfireCookingRecipe> {
    private static final int INPUT_X = 1;
    private static final int ROW_Y = 5;
    private static final int ARROW_X = 26;
    private static final int RESULT_X = 60;
    private static final int TEXT_Y = 28;
    private static final int WIDTH = 140;
    private static final int HEIGHT = 40;

    private final IDrawable icon;

    SpitRoastingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(Spits.spitItem()));
    }

    @Override
    public RecipeType<CampfireCookingRecipe> getRecipeType() {
        return FoodJeiRecipeTypes.SPIT_ROASTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(FoodTranslations.JEI_SPIT_ROASTING);
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
    public void setRecipe(IRecipeLayoutBuilder builder, CampfireCookingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, ROW_Y).setStandardSlotBackground().addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).setOutputSlotBackground()
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, CampfireCookingRecipe recipe, IFocusGroup focuses) {
        FoodServerConfig.SpitSettings settings = FoodServerConfig.get().spit;
        double spitTicks = recipe.getCookingTime() / settings.speed.get();
        double turnedTicks = recipe.getCookingTime() / settings.turnSpeed.get();
        builder.addAnimatedRecipeArrowWidget((int) Math.round(spitTicks)).setPosition(ARROW_X, ROW_Y);
        Component times = Component.translatable(FoodTranslations.JEI_SPIT_TIMES, JeiTimes.seconds(spitTicks), JeiTimes.seconds(turnedTicks));
        builder.addText(times, WIDTH, TEXT_Y).setPosition(0, TEXT_Y);
    }
}
