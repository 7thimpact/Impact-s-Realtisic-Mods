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
    WOOD_SCRAPS("wood_scraps", () -> CraftingServerConfig.get().woodScraps.burnTicks.get()),
    CHEST_TOP("chest_top", () -> 600),
    OAR("oar", () -> 200),
    STONE_CASING("stone_casing", () -> 0),
    STONE_BASE("stone_base", () -> 0),
    MORTAR("mortar", () -> 0),
    LEATHER_STRIP("leather_strip", () -> 0),
    FLINT_ARROWHEAD("flint_arrowhead", () -> 0),
    IRON_PLATE("iron_plate", () -> 0),
    IRON_ROD("iron_rod", () -> 0),
    IRON_RIVETS("iron_rivets", () -> 0),
    IRON_HINGE("iron_hinge", () -> 0),
    IRON_WHEEL("iron_wheel", () -> 0),
    PAPER_PULP("paper_pulp", () -> 0),
    REDSTONE_MECHANISM("redstone_mechanism", () -> 0),
    PISTON_HEAD("piston_head", () -> 0);

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
