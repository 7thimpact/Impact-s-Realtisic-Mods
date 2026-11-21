package com.impact.realisticinventory.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Shift-click moves into the player's own slots fill the pockets first, then the main hand. Vanilla would fill its
 * hotbar first, which here is the main hand. Locked slots are never targets.
 */
public final class PlayerSlotMoves {
    private PlayerSlotMoves() {
    }

    /** The slots a move may fill, in the order to try them; empty when the range isn't only the player's slots. */
    public static Optional<List<Slot>> targetsInOrder(List<Slot> slotsInRange, Inventory playerInventory) {
        boolean isOnlyPlayerSlots = !slotsInRange.isEmpty()
                && slotsInRange.stream().allMatch(slot -> slot.container == playerInventory);
        if (!isOnlyPlayerSlots) {
            return Optional.empty();
        }
        List<Slot> targets = new ArrayList<>(slotsInRange.stream().filter(slot -> SlotLocks.isOpenPlayerSlot(slot, playerInventory)).toList());
        targets.sort(Comparator.comparingInt(PlayerSlotMoves::fillOrder));
        return Optional.of(targets);
    }

    /** Moves as much of the stack as fits: first onto matching stacks, then into empty slots. Returns whether anything moved. */
    public static boolean move(ItemStack stack, List<Slot> targets) {
        int startingCount = stack.getCount();
        targets.forEach(slot -> stackOnto(slot, stack));
        targets.forEach(slot -> placeInto(slot, stack));
        return stack.getCount() < startingCount;
    }

    private static void stackOnto(Slot slot, ItemStack stack) {
        ItemStack existing = slot.getItem();
        if (stack.isEmpty() || existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, stack)) {
            return;
        }
        int room = Math.min(slot.getMaxStackSize(existing), existing.getMaxStackSize()) - existing.getCount();
        int moved = Math.min(room, stack.getCount());
        if (moved <= 0) {
            return;
        }
        existing.grow(moved);
        stack.shrink(moved);
        slot.setChanged();
    }

    private static void placeInto(Slot slot, ItemStack stack) {
        if (stack.isEmpty() || slot.hasItem() || !slot.mayPlace(stack)) {
            return;
        }
        int moved = Math.min(slot.getMaxStackSize(stack), stack.getCount());
        slot.setByPlayer(stack.split(moved));
        slot.setChanged();
    }

    // Pockets come first, the main hand after them, the off hand last.
    private static int fillOrder(Slot slot) {
        int inventoryIndex = slot.getContainerSlot();
        if (InventoryLayout.isPocket(inventoryIndex)) {
            return inventoryIndex;
        }
        return inventoryIndex == InventoryLayout.MAIN_HAND_INDEX ? InventoryLayout.LAST_POCKET_INDEX + 1 : Integer.MAX_VALUE;
    }
}
