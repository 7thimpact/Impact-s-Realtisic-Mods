package com.impact.realisticinventory.debug;

import com.impact.lib.log.ImpactsLog;

import java.util.List;

/** Inventory's debug log categories, as listed in its spec ("Debug logging"). */
public final class InventoryLogCategories {
    public static final String STARTUP = ImpactsLog.STARTUP_CATEGORY;
    public static final String HOTBAR = "hotbar";
    public static final String PICKUP = "pickup";
    public static final String LOCKS = "locks";
    public static final String MIGRATION = "migration";
    public static final String PACKS = "packs";
    public static final String SCREEN = "screen";

    public static final List<String> ALL = List.of(STARTUP, HOTBAR, PICKUP, LOCKS, MIGRATION, PACKS, SCREEN);

    private InventoryLogCategories() {
    }
}
