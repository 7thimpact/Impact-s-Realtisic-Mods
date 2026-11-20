package com.impact.realisticcrafting.item;

import com.impact.realisticcrafting.config.CraftingServerConfig;

import java.util.function.IntSupplier;

/** Crafting's plain parts: stackable items that only go into other recipes. Wooden ones burn (see the spec's fuel table). */
public enum CraftingPart {
    TRIMMED_STICK("trimmed_stick", () -> 100),
    SHAPED_STONE("shaped_stone", () -> 0),
    STONE_HINGE("stone_hinge", () -> 0),
    TWINE("twine", () -> 0),
    STRING_CLUMP("string_clump", () -> 0),
    WOOD_SCRAPS("wood_scraps", () -> CraftingServerConfig.get().woodScraps.burnTicks.get());

    public final String path;
    private final IntSupplier burnTicks;

    CraftingPart(String path, IntSupplier burnTicks) {
        this.path = path;
        this.burnTicks = burnTicks;
    }

    /** Furnace burn time in ticks; 0 for parts that don't burn (stone, string). */
    public int burnTicks() {
        return burnTicks.getAsInt();
    }
}
