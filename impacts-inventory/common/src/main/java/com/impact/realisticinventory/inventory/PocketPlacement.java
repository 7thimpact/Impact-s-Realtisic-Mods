package com.impact.realisticinventory.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Where items go in a locked inventory. Picked-up items first stack onto the hands, then onto the pockets; failing
 * that they go into an empty main hand (the active slot), then an empty pocket. Locked indexes are never used.
 */
public final class PocketPlacement {
    public static final int NO_SLOT = -1;

    private PocketPlacement() {
    }

    /** An index whose stack the item can join, or {@link #NO_SLOT}. Replaces vanilla's search over all 36 indexes. */
    public static int slotWithRemainingSpace(Inventory inventory, ItemStack stack) {
        if (canStackOnto(inventory.getItem(InventoryLayout.MAIN_HAND_INDEX), stack, inventory)) {
            return InventoryLayout.MAIN_HAND_INDEX;
        }
        if (canStackOnto(inventory.getItem(InventoryLayout.OFF_HAND_INDEX), stack, inventory)) {
            return InventoryLayout.OFF_HAND_INDEX;
        }
        for (int pocketIndex = InventoryLayout.FIRST_POCKET_INDEX; pocketIndex <= InventoryLayout.LAST_POCKET_INDEX; pocketIndex++) {
            if (canStackOnto(inventory.getItem(pocketIndex), stack, inventory)) {
                return pocketIndex;
            }
        }
        return NO_SLOT;
    }

    /** An empty index for a new stack: the main hand, then the pockets. {@link #NO_SLOT} when everything is full. */
    public static int freeSlot(Inventory inventory) {
        if (inventory.getItem(InventoryLayout.MAIN_HAND_INDEX).isEmpty()) {
            return InventoryLayout.MAIN_HAND_INDEX;
        }
        for (int pocketIndex = InventoryLayout.FIRST_POCKET_INDEX; pocketIndex <= InventoryLayout.LAST_POCKET_INDEX; pocketIndex++) {
            if (inventory.getItem(pocketIndex).isEmpty()) {
                return pocketIndex;
            }
        }
        return NO_SLOT;
    }

    private static boolean canStackOnto(ItemStack existing, ItemStack incoming, Inventory inventory) {
        return !existing.isEmpty()
                && ItemStack.isSameItemSameTags(existing, incoming)
                && existing.isStackable()
                && existing.getCount() < existing.getMaxStackSize()
                && existing.getCount() < inventory.getMaxStackSize();
    }
}
