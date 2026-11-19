package com.impact.realisticinventory;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.config.InventoryClientConfig;
import com.impact.realisticinventory.config.InventoryCommonConfig;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.inventory.LockedSlotOverflow;
import com.impact.realisticinventory.network.InventoryNetwork;
import com.impact.realisticinventory.pack.InventoryBackSlotProvider;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackMenus;
import com.impact.realisticinventory.pack.PackRecipes;
import com.impact.realisticinventory.pack.PlacedPacks;

/** Inventory's startup steps that don't depend on the mod loader. */
public final class InventoryStartup {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private InventoryStartup() {
    }

    /**
     * Mod construction: registers with Lib, then the log categories, config files, player data, items, menus and
     * recipe types, the network channel and the back slot.
     */
    public static void onModConstruction() {
        ImpactsLib.registerImpactMod(InventoryMod.MOD_ID);
        LOG.registerCategories(InventoryLogCategories.ALL);
        InventoryCommonConfig.register();
        InventoryServerConfig.register();
        InventoryClientConfig.register();
        PlayerAttachments.register(HandSwitch.HOTBAR);
        PlayerAttachments.register(LockedSlotOverflow.MIGRATION);
        InventoryItems.register();
        PackMenus.register();
        PlacedPacks.register();
        PackRecipes.register();
        InventoryNetwork.create();
        BackSlot.registerBackSlotProvider(InventoryMod.MOD_ID, new InventoryBackSlotProvider());
        ImpactsLib.onModsReady(impactMods -> StartupSummary.log(InventoryMod.MOD_ID));
    }
}
