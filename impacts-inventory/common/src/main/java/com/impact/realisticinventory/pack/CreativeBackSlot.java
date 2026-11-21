package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLayout;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Creative mode's inventory tab writes slots directly by index. Vanilla's server only accepts indexes up to 45, so
 * Inventory accepts the back slot's index itself, with the back slot's own rules.
 */
public final class CreativeBackSlot {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private CreativeBackSlot() {
    }

    public static void handleCreativeEdit(ServerPlayer player, ItemStack requested) {
        if (!player.gameMode.isCreative()) {
            return;
        }
        Slot backSlot = player.inventoryMenu.getSlot(InventoryLayout.MENU_BACK_SLOT);
        boolean isAllowed = requested.isEmpty() || backSlot.mayPlace(requested) && requested.getCount() <= backSlot.getMaxStackSize();
        if (!isAllowed) {
            LOG.debug(InventoryLogCategories.LOCKS, "creative back slot edit refused", "player", player.getGameProfile().getName(),
                    "item", ResourceIds.itemId(requested.getItem()), "count", requested.getCount());
            return;
        }
        backSlot.setByPlayer(requested);
        player.inventoryMenu.broadcastChanges();
    }
}
