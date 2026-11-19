package com.impact.lib.crafting;

/**
 * Config-driven numbers for one mod's tool-crafting recipes (Food's cut yields and byproduct chances). A mod registers
 * it for its recipe namespace with {@link ToolCrafting#registerOverrides}; recipes keep their JSON values otherwise.
 */
public interface ToolCraftingOverrides {
    /** The result count to give, given the count the recipe's JSON names. */
    default int resultCount(ToolCraftingRecipe recipe, int recipeCount) {
        return recipeCount;
    }

    /** The chance of the byproduct, given the chance the recipe's JSON names. 0 turns the byproduct off. */
    default float byproductChance(ToolCraftingRecipe recipe, Byproduct byproduct) {
        return byproduct.chance();
    }
}
