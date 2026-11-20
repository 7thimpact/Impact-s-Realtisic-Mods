package com.impact.realisticcrafting.config;

/** Crafting's systems that can be switched off on their own in the common file. */
public enum CraftingFeature {
    PEBBLE_WORLDGEN("pebbleWorldgen", "Loose pebbles generate on exposed stone."),
    HAMMERING("hammering", "Loose items are finished by hammering them in hand."),
    TIMED_CRAFTING("timedCrafting", "Every crafting grid takes time, by the recipe's depth in the crafting tree."),
    TABLE_TIERS("tableTiers", "Tables refuse recipes above their tier."),
    DYE_VAT("dyeVat", "The Dye Vat, where all dyeing happens."),
    BEDS("beds", "Beds are built in the world from a frame, a comforter and a sheet.");

    public final String configKey;
    public final String description;

    CraftingFeature(String configKey, String description) {
        this.configKey = configKey;
        this.description = description;
    }

    public boolean isOn() {
        return CraftingCommonConfig.get().features.get(this).get();
    }
}
