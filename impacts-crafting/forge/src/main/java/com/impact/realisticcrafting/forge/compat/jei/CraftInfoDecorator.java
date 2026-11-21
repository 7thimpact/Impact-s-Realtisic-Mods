package com.impact.realisticcrafting.forge.compat.jei;

import com.impact.realisticcrafting.lang.CraftingTranslations;
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.timing.CraftTimes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Writes each crafting recipe's craft time under its arrow, and on hover the first table that can make it, so the
 * crafting tree reads in JEI. Works for vanilla's crafting category and Lib's Tool Crafting category.
 */
final class CraftInfoDecorator<T extends Recipe<?>> implements IRecipeCategoryDecorator<T> {
    private static final int TEXT_X = 58;
    private static final int TEXT_Y = 42;
    private static final int TEXT_HEIGHT = 9;
    private static final int TEXT_COLOR = 0xFF808080;

    @Override
    public void draw(T recipe, IRecipeCategory<T> category, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, JeiText.seconds(CraftTimes.ticksFor(recipe)), TEXT_X, TEXT_Y, TEXT_COLOR, false);
    }

    @Override
    public void decorateTooltips(ITooltipBuilder tooltip, T recipe, IRecipeCategory<T> category, IRecipeSlotsView slots, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(JeiText.seconds(CraftTimes.ticksFor(recipe)));
        boolean isOverText = mouseX >= TEXT_X && mouseX < TEXT_X + textWidth && mouseY >= TEXT_Y && mouseY < TEXT_Y + TEXT_HEIGHT;
        if (!isOverText) {
            return;
        }
        Component station = Component.translatable(CraftingStation.lowestFor(recipe).nameTranslationKey());
        tooltip.add(Component.translatable(CraftingTranslations.JEI_CRAFT_INFO, station, JeiText.seconds(CraftTimes.ticksFor(recipe)))
                .withStyle(ChatFormatting.GRAY));
    }
}
