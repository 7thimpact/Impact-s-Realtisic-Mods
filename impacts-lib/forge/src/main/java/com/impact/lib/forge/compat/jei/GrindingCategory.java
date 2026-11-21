package com.impact.lib.forge.compat.jei;

import com.impact.lib.crafting.Byproduct;
import com.impact.lib.grinding.GrinderTier;
import com.impact.lib.grinding.Grinding;
import com.impact.lib.grinding.GrindingRecipe;
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

/** Grinding: what goes in the bowl (one item, or two to mix), the result, byproducts, and the grinder tier it needs. */
final class GrindingCategory implements IRecipeCategory<GrindingRecipe> {
    private static final int SLOT_SIZE = 18;
    private static final int INPUTS_Y = 5;
    private static final int ARROW_X = 42;
    private static final int RESULT_X = 76;
    private static final int TEXT_Y = 28;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final float PERCENT = 100.0F;

    private final IDrawable icon;

    GrindingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(Grinding.woodenBowl()));
    }

    @Override
    public RecipeType<GrindingRecipe> getRecipeType() {
        return LibJeiRecipeTypes.GRINDING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(LibTranslations.JEI_GRINDING);
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
    public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipe recipe, IFocusGroup focuses) {
        for (int index = 0; index < recipe.getIngredients().size(); index++) {
            builder.addSlot(RecipeIngredientRole.INPUT, index * SLOT_SIZE + 1, INPUTS_Y)
                    .setStandardSlotBackground()
                    .addIngredients(recipe.getIngredients().get(index));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, INPUTS_Y)
                .setOutputSlotBackground()
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
        for (int byproductIndex = 0; byproductIndex < recipe.byproducts().size(); byproductIndex++) {
            Byproduct byproduct = recipe.byproducts().get(byproductIndex);
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X + (byproductIndex + 1) * SLOT_SIZE + 4, INPUTS_Y)
                    .setStandardSlotBackground()
                    .addItemStack(byproduct.stack())
                    .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(Component.translatable(LibTranslations.JEI_BYPRODUCT,
                            Math.round(byproduct.chance() * PERCENT))));
        }
        ItemStack bowl = new ItemStack(recipe.minimumTier() == GrinderTier.STONE ? Grinding.stoneBowl() : Grinding.woodenBowl());
        builder.addSlot(RecipeIngredientRole.CATALYST, ARROW_X + 4, INPUTS_Y + SLOT_SIZE).addItemStack(bowl);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, GrindingRecipe recipe, IFocusGroup focuses) {
        int grindTicks = recipe.grindTicks() > 0 ? recipe.grindTicks() : GrinderTier.WOOD.grindTicks();
        builder.addAnimatedRecipeArrowWidget(grindTicks).setPosition(ARROW_X, INPUTS_Y);
        Component tier = Component.translatable(recipe.minimumTier() == GrinderTier.STONE ? LibTranslations.JEI_STONE_GRINDER : LibTranslations.JEI_WOODEN_GRINDER);
        builder.addText(tier, WIDTH, TEXT_Y).setPosition(0, TEXT_Y + 2);
    }
}
