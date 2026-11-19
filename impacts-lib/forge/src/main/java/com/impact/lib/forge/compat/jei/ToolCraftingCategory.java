package com.impact.lib.forge.compat.jei;

import com.impact.lib.crafting.Byproduct;
import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.lib.lang.LibTranslations;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;

/** Tool crafting: the grid (tools in their own column, since they fit any slot), the result, and byproducts with their chance. */
final class ToolCraftingCategory implements IRecipeCategory<ToolCraftingRecipe> {
    private static final int SLOT_SIZE = 18;
    private static final int GRID_COLUMNS = 3;
    private static final int ARROW_X = 61;
    private static final int ARROW_Y = 19;
    private static final int RESULT_X = 95;
    private static final int RESULT_Y = 19;
    private static final int BYPRODUCTS_Y = 40;
    private static final int TOOLS_X = 124;
    private static final int WIDTH = 142;
    private static final int HEIGHT = 58;
    private static final float PERCENT = 100.0F;

    private final IDrawable icon;

    ToolCraftingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(Items.CRAFTING_TABLE));
    }

    @Override
    public RecipeType<ToolCraftingRecipe> getRecipeType() {
        return LibJeiRecipeTypes.TOOL_CRAFTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(LibTranslations.JEI_TOOL_CRAFTING);
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
    public void setRecipe(IRecipeLayoutBuilder builder, ToolCraftingRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.shape().getIngredients();
        int shapeWidth = recipe.shape() instanceof ShapedRecipe shaped ? shaped.getWidth() : GRID_COLUMNS;
        for (int index = 0; index < ingredients.size(); index++) {
            int column = index % shapeWidth;
            int row = index / shapeWidth;
            builder.addSlot(RecipeIngredientRole.INPUT, column * SLOT_SIZE + 1, row * SLOT_SIZE + 1)
                    .setStandardSlotBackground()
                    .addIngredients(ingredients.get(index));
        }
        for (int toolIndex = 0; toolIndex < recipe.tools().size(); toolIndex++) {
            builder.addSlot(RecipeIngredientRole.INPUT, TOOLS_X, toolIndex * SLOT_SIZE + 1)
                    .setStandardSlotBackground()
                    .addIngredients(recipe.tools().get(toolIndex))
                    .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(Component.translatable(LibTranslations.JEI_TOOL, recipe.toolDamage())));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, RESULT_Y)
                .setOutputSlotBackground()
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
        for (int byproductIndex = 0; byproductIndex < recipe.byproducts().size(); byproductIndex++) {
            Byproduct byproduct = recipe.byproducts().get(byproductIndex);
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X + byproductIndex * SLOT_SIZE, BYPRODUCTS_Y)
                    .setStandardSlotBackground()
                    .addItemStack(byproduct.stack())
                    .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(Component.translatable(LibTranslations.JEI_BYPRODUCT,
                            Math.round(byproduct.chance() * PERCENT))));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ToolCraftingRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrowWidget().setPosition(ARROW_X, ARROW_Y);
    }
}
