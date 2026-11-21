package com.impact.realisticcrafting.menu;

/**
 * How metal a recipe is, by its ingredients: non-metal (no ores and nothing made from them), stone (iron, copper and
 * redstone, from {@code #impactscrafting:tier/stone}) or advanced (every other ore material, from
 * {@code #impactscrafting:tier/advanced}). Each crafting station accepts recipes up to its own tier.
 */
public enum CraftingTier {
    NON_METAL("non_metal"),
    STONE("stone"),
    ADVANCED("advanced");

    public final String serializedName;

    CraftingTier(String serializedName) {
        this.serializedName = serializedName;
    }

    public boolean isAbove(CraftingTier other) {
        return ordinal() > other.ordinal();
    }

    public static CraftingTier highest(CraftingTier first, CraftingTier second) {
        return first.isAbove(second) ? first : second;
    }

    public static CraftingTier lowest(CraftingTier first, CraftingTier second) {
        return first.isAbove(second) ? second : first;
    }
}
