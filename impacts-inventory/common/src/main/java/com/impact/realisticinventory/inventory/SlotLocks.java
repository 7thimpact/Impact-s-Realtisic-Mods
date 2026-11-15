package com.impact.realisticinventory.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Which menu slots show a locked player index. Every container screen, vanilla or modded, hides them and refuses
 * clicks, shift-click moves and drag-splits into them. Loader modules add rules for their own slot types (Forge's
 * item-handler slots).
 */
public final class SlotLocks {
    private static final List<Predicate<Slot>> EXTRA_LOCKED_SLOT_RULES = new CopyOnWriteArrayList<>();

    private SlotLocks() {
    }

    public static void registerLockedSlotRule(Predicate<Slot> rule) {
        EXTRA_LOCKED_SLOT_RULES.add(rule);
    }

    public static boolean isLocked(Slot slot) {
        if (slot.container instanceof Inventory inventory) {
            return InventoryLayout.isLocked(slot.getContainerSlot()) && InventoryLocks.isActiveFor(inventory.player);
        }
        return EXTRA_LOCKED_SLOT_RULES.stream().anyMatch(rule -> rule.test(slot));
    }

    /** Whether the slot holds one of the player's own unlocked indexes (hands or pockets). */
    public static boolean isOpenPlayerSlot(Slot slot, Inventory playerInventory) {
        return slot.container == playerInventory && !InventoryLayout.isLocked(slot.getContainerSlot());
    }
}
