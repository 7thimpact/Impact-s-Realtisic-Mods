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
 * What a crafting recipe needs in the grid, in a form two recipes can be compared by: a fixed grid of cells for shaped
 * recipes (a tool recipe's shaped part too, its tools kept apart since they fit any slot), or a bag of ingredients for
 * shapeless recipes (a shapeless tool recipe's tools go in the bag). Each ingredient is the list of stacks it accepts.
 */
record GridSignature(ResourceLocation id, int width, int height, List<List<ItemStack>> cells, boolean isShaped, List<List<ItemStack>> tools) {
    static Optional<GridSignature> of(CraftingRecipe recipe) {
        if (recipe instanceof ToolCraftingRecipe toolRecipe) {
            if (toolRecipe.shape() instanceof ShapedRecipe shaped) {
                return Optional.of(new GridSignature(recipe.getId(), shaped.getWidth(), shaped.getHeight(), cellChoicesOf(shaped.getIngredients()), true,
                        choicesOf(toolRecipe.tools())));
            }
            List<List<ItemStack>> bag = new ArrayList<>(choicesOf(toolRecipe.shape().getIngredients()));
            bag.addAll(choicesOf(toolRecipe.tools()));
            return Optional.of(new GridSignature(recipe.getId(), 0, 0, bag, false, List.of()));
        }
        if (recipe.isSpecial() || recipe.getIngredients().isEmpty()) {
            return Optional.empty();
        }
        if (recipe instanceof ShapedRecipe shaped) {
            return Optional.of(new GridSignature(recipe.getId(), shaped.getWidth(), shaped.getHeight(), cellChoicesOf(shaped.getIngredients()), true,
                    List.of()));
        }
        return Optional.of(new GridSignature(recipe.getId(), 0, 0, choicesOf(recipe.getIngredients()), false, List.of()));
    }

    /** Every ingredient the grid has to hold: the filled cells, and the tools. */
    List<List<ItemStack>> filledChoices() {
        List<List<ItemStack>> filled = new ArrayList<>(cells.stream().filter(choices -> !choices.isEmpty()).toList());
        filled.addAll(tools);
        return filled;
    }

    private static List<List<ItemStack>> choicesOf(List<Ingredient> ingredients) {
        return ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).map(ingredient -> Arrays.asList(ingredient.getItems())).toList();
    }

    // Empty cells stay as empty lists, so positions line up.
    private static List<List<ItemStack>> cellChoicesOf(List<Ingredient> ingredients) {
        return ingredients.stream().map(ingredient -> ingredient.isEmpty() ? List.<ItemStack>of() : Arrays.asList(ingredient.getItems())).toList();
    }
}
