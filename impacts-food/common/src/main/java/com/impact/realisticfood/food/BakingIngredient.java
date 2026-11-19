package com.impact.realisticfood.food;

/** Baking's ingredients: Flour from the grinder, Dough from Flour and water, and Loaf Dough from Dough. */
public enum BakingIngredient {
    FLOUR("flour", "Flour"),
    DOUGH("dough", "Dough"),
    LOAF_DOUGH("loaf_dough", "Loaf Dough");

    public final String itemPath;
    public final String displayName;

    BakingIngredient(String itemPath, String displayName) {
        this.itemPath = itemPath;
        this.displayName = displayName;
    }
}
