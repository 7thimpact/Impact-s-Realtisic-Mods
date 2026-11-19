package com.impact.lib.audit;

import com.impact.lib.crafting.ToolCraftingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * What a crafting recipe needs in the grid, in a form two recipes can be compared by: a fixed grid of cells for plain
 * shaped recipes, or a bag of ingredients for shapeless and tool recipes (tools count as ingredients, in any slot).
 * Each ingredient is the list of stacks it accepts.
 */
record GridSignature(ResourceLocation id, int width, int height, List<List<ItemStack>> cells, boolean isShaped) {
    static Optional<GridSignature> of(CraftingRecipe recipe) {
        if (recipe instanceof ToolCraftingRecipe toolRecipe) {
            List<List<ItemStack>> bag = new ArrayList<>(choicesOf(toolRecipe.shape().getIngredients()));
            bag.addAll(choicesOf(toolRecipe.tools()));
            return Optional.of(new GridSignature(recipe.getId(), 0, 0, bag, false));
        }
        if (recipe.isSpecial() || recipe.getIngredients().isEmpty()) {
            return Optional.empty();
        }
        if (recipe instanceof ShapedRecipe shaped) {
            return Optional.of(new GridSignature(recipe.getId(), shaped.getWidth(), shaped.getHeight(), cellChoicesOf(shaped.getIngredients()), true));
        }
        return Optional.of(new GridSignature(recipe.getId(), 0, 0, choicesOf(recipe.getIngredients()), false));
    }

    /** Ingredients that aren't empty cells. */
    List<List<ItemStack>> filledChoices() {
        return cells.stream().filter(choices -> !choices.isEmpty()).toList();
    }

    private static List<List<ItemStack>> choicesOf(List<Ingredient> ingredients) {
        return ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).map(ingredient -> Arrays.asList(ingredient.getItems())).toList();
    }

    // Empty cells stay as empty lists, so positions line up.
    private static List<List<ItemStack>> cellChoicesOf(List<Ingredient> ingredients) {
        return ingredients.stream().map(ingredient -> ingredient.isEmpty() ? List.<ItemStack>of() : Arrays.asList(ingredient.getItems())).toList();
    }
}
