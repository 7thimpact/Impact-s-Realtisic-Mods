package com.impact.realisticinventory;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.config.InventoryClientConfig;
import com.impact.realisticinventory.config.InventoryCommonConfig;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;

/** Inventory's startup steps that don't depend on the mod loader. */
public final class InventoryStartup {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private InventoryStartup() {
    }

    /** Mod construction: registers with Lib, then the log categories and config files. */
    public static void onModConstruction() {
        ImpactsLib.registerImpactMod(InventoryMod.MOD_ID);
        LOG.registerCategories(InventoryLogCategories.ALL);
        InventoryCommonConfig.register();
        InventoryServerConfig.register();
        InventoryClientConfig.register();
        ImpactsLib.onModsReady(impactMods -> StartupSummary.log(InventoryMod.MOD_ID));
    }
}
