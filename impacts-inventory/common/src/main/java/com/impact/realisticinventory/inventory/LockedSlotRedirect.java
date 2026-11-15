package com.impact.realisticinventory.inventory;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Items that any code (another mod, an MCreator procedure, a command) tries to put into a locked index never stay
 * there. They go to the hands, the pockets or a worn pack, or drop at the player's feet, through Lib's Give or drop,
 * and the redirect is logged with the calling class. Server side only; the client follows the server.
 */
public final class LockedSlotRedirect {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private LockedSlotRedirect() {
    }

    /** Whether a write of this stack into this index must be redirected. Empty writes are always allowed. */
    public static boolean isRedirected(Inventory inventory, int inventoryIndex, ItemStack stack) {
        return !stack.isEmpty()
                && InventoryLayout.isLocked(inventoryIndex)
                && !inventory.player.level().isClientSide()
                && InventoryLocks.isActiveFor(inventory.player);
    }

    public static void redirect(Inventory inventory, int inventoryIndex, ItemStack stack) {
        String caller = CallerFinder.outsideCaller();
        LOG.debug(InventoryLogCategories.LOCKS, "locked slot write redirected",
                "player", inventory.player.getGameProfile().getName(), "index", inventoryIndex,
                "item", ResourceIds.itemId(stack.getItem()), "count", stack.getCount(), "caller", caller, "to", "give or drop");
        ImpactsItems.giveOrDrop(inventory.player, stack.copy());
    }

    /** {@code Inventory.add(index, stack)} aimed at a locked index places the stack normally instead. */
    public static int placementIndexFor(Inventory inventory, int requestedIndex) {
        boolean isLockedRequest = requestedIndex >= 0 && InventoryLayout.isLocked(requestedIndex) && InventoryLocks.isActiveFor(inventory.player);
        if (!isLockedRequest) {
            return requestedIndex;
        }
        LOG.debug(InventoryLogCategories.LOCKS, "locked slot add redirected",
                "player", inventory.player.getGameProfile().getName(), "index", requestedIndex, "caller", CallerFinder.outsideCaller());
        return PocketPlacement.NO_SLOT;
    }
}
