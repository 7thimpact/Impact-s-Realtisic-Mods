package com.impact.realisticcrafting.item;

import com.impact.realisticcrafting.config.CraftingServerConfig;

import java.util.function.IntSupplier;

/** Crafting's grid tools: they stay in the grid and lose durability with each craft (Lib's tool crafting). */
public enum CraftingTool {
    BONE_NEEDLE("bone_needle", () -> CraftingServerConfig.get().durability.needleUses.get()),
    FLINT_CHISEL("flint_chisel", () -> CraftingServerConfig.get().durability.flintChiselUses.get()),
    IRON_CHISEL("iron_chisel", () -> CraftingServerConfig.get().durability.ironChiselUses.get()),
    IRON_SAW("iron_saw", () -> CraftingServerConfig.get().durability.ironToolUses.get()),
    IRON_HAMMER("iron_hammer", () -> CraftingServerConfig.get().durability.ironToolUses.get()),
    IRON_TWEEZERS("iron_tweezers", () -> CraftingServerConfig.get().durability.ironToolUses.get());

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
