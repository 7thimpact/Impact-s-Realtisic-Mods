package com.impact.realisticcrafting.item;

import com.impact.realisticcrafting.config.CraftingServerConfig;

import java.util.function.IntSupplier;

/** Crafting's grid tools: they stay in the grid and lose durability with each craft (Lib's tool crafting). */
public enum CraftingTool {
    BONE_NEEDLE("bone_needle", () -> CraftingServerConfig.get().durability.needleUses.get()),
    FLINT_CHISEL("flint_chisel", () -> CraftingServerConfig.get().durability.flintChiselUses.get());

    public final String path;
    private final IntSupplier maxUses;

    CraftingTool(String path, IntSupplier maxUses) {
        this.path = path;
        this.maxUses = maxUses;
    }

    public int maxUses() {
        return maxUses.getAsInt();
    }
}
