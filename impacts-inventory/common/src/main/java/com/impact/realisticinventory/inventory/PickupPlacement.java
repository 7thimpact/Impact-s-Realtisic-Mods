package com.impact.realisticinventory.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Where an item goes when it's added to a locked inventory without a target slot: pickups, Lib's Give or drop and
 * other mods' adds. Like vanilla, it first stacks onto what's already carried, then uses empty slots, each pass going
 * through the places in {@code pickup.order}.
 */
public final class PickupPlacement {
    private PickupPlacement() {
    }

    /** Moves as much of the stack as fits. Returns whether anything moved, as vanilla's {@code Inventory.add} does. */
    public static boolean add(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        int startingCount = stack.getCount();
        List<PickupDestination> order = PickupDestination.configuredOrder();
        order.forEach(destination -> destination.stackOnto(inventory, stack));
        order.forEach(destination -> destination.placeInEmpty(inventory, stack));
        boolean hasMoved = stack.getCount() < startingCount;
        // Vanilla's creative rule: an inventory with infinite items swallows what doesn't fit.
        if (!hasMoved && inventory.player.getAbilities().instabuild) {
            stack.setCount(0);
            return true;
        }
        return hasMoved;
    }
}
