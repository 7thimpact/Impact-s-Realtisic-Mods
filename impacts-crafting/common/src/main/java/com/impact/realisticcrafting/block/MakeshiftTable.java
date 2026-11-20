package com.impact.realisticcrafting.block;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.registry.CraftingBlocks;

import java.util.function.Supplier;

/** The two makeshift tables: crude 3×3 crafting stations that wear out like an anvil. */
public enum MakeshiftTable {
    WOODEN("makeshift_table", CraftingStation.MAKESHIFT_TABLE, () -> CraftingServerConfig.get().tables.makeshiftTableUses.get()),
    STONE("makeshift_stone_table", CraftingStation.MAKESHIFT_STONE_TABLE, () -> CraftingServerConfig.get().tables.makeshiftStoneTableUses.get());

    public final String path;
    public final CraftingStation station;
    private final Supplier<Integer> maxUsesSetting;

    MakeshiftTable(String path, CraftingStation station, Supplier<Integer> maxUsesSetting) {
        this.path = path;
        this.station = station;
        this.maxUsesSetting = maxUsesSetting;
    }

    public int maxUses() {
        return maxUsesSetting.get();
    }

    public MakeshiftTableBlock block() {
        return CraftingBlocks.table(this);
    }

    public String containerTitleKey() {
        return "container." + CraftingMod.MOD_ID + "." + path;
    }
}
