package com.impact.realisticfood.cooking;

/** Vanilla's three ways to cook an item, as Food's recipe IDs name them. */
public enum CookingMethod {
    SMELTING("smelting"),
    SMOKING("smoking"),
    CAMPFIRE("campfire_cooking");

    public final String recipeSuffix;

    CookingMethod(String recipeSuffix) {
        this.recipeSuffix = recipeSuffix;
    }
}
