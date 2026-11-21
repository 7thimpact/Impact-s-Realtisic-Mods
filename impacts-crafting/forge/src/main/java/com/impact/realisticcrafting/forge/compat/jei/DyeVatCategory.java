package com.impact.realisticcrafting.forge.compat.jei;

import com.impact.realisticcrafting.lang.CraftingTranslations;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Dipping in the Dye Vat: the item, what colors the water (a dye, or a water bucket for plain water), the result, charges and time. */
final class DyeVatCategory implements IRecipeCategory<VatDip> {
    private static final int INPUT_X = 1;
    private static final int WATER_X = 21;
    private static final int ROW_Y = 5;
    private static final int ARROW_X = 46;
    private static final int RESULT_X = 80;
    private static final int TEXT_Y = 28;
    private static final int WIDTH = 140;
    private static final int HEIGHT = 40;
    private static final String COLOR_KEY_PREFIX = "color.minecraft.";

    private final IDrawable icon;

    DyeVatCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(CraftingItems.dyeVat()));
    }

    @Override
    public RecipeType<VatDip> getRecipeType() {
        return CraftingJeiRecipeTypes.DYE_VAT;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(CraftingTranslations.JEI_DYE_VAT);
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
    public void setRecipe(IRecipeLayoutBuilder builder, VatDip dip, IFocusGroup focuses) {
        ItemStack water = dip.color().map(color -> new ItemStack(DyeItem.byColor(color))).orElseGet(() -> new ItemStack(Items.WATER_BUCKET));
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, ROW_Y).setStandardSlotBackground().addIngredients(dip.input());
        builder.addSlot(RecipeIngredientRole.CATALYST, WATER_X, ROW_Y).setStandardSlotBackground().addItemStack(water);
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).setOutputSlotBackground().addItemStack(dip.result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, VatDip dip, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(dip.dipTicks()).setPosition(ARROW_X, ROW_Y);
        Component water = dip.color().map(color -> coloredWater(color, dip.charges()))
                .orElseGet(() -> Component.translatable(CraftingTranslations.JEI_PLAIN_WATER));
        builder.addText(Component.translatable(CraftingTranslations.JEI_CRAFT_INFO, water, JeiText.seconds(dip.dipTicks())), WIDTH, TEXT_Y)
                .setPosition(0, TEXT_Y);
    }

    // A dip normally costs 1 charge; only other costs are worth the space.
    private static Component coloredWater(DyeColor color, int charges) {
        Component colorName = Component.translatable(COLOR_KEY_PREFIX + color.getName());
        return charges == 1 ? Component.translatable(CraftingTranslations.JEI_VAT_COLOR, colorName)
                : Component.translatable(CraftingTranslations.JEI_VAT_COLOR_CHARGES, colorName, charges);
    }
}
