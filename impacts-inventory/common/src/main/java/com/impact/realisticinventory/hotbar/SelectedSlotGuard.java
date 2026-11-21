package com.impact.realisticinventory.hotbar;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Keeps vanilla's selected hotbar index at 0, the main hand. Changes from other code (auto-tool switchers, some
 * MCreator procedures) are undone and logged.
 */
public final class SelectedSlotGuard {
    private static final String UNKNOWN_CALLER = "unknown (the field was written directly)";
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private SelectedSlotGuard() {
    }

    /** Called every tick for every player's inventory, on both sides. */
    public static void enforce(Inventory inventory) {
        boolean isMainHandSelected = inventory.selected == InventoryLayout.MAIN_HAND_INDEX;
        if (isMainHandSelected || !InventoryLocks.isActiveFor(inventory.player)) {
            return;
        }
        logBlocked(inventory.player, inventory.selected, UNKNOWN_CALLER);
        inventory.selected = InventoryLayout.MAIN_HAND_INDEX;
    }

    /** A client asked to select another hotbar index; the server ignores it. */
    public static void logRefusedClientSelection(Player player, int requestedIndex) {
        logBlocked(player, requestedIndex, "client packet");
    }

    private static void logBlocked(Player player, int requestedIndex, String caller) {
        if (player.level().isClientSide()) {
            return;
        }
        LOG.debug(InventoryLogCategories.HOTBAR, "selected slot change blocked",
                "player", player.getGameProfile().getName(), "requested", requestedIndex, "caller", caller);
    }
}
