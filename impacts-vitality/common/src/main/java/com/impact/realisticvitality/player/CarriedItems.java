package com.impact.realisticvitality.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Gadgets that work while carried, like the Thermometer: anywhere in the inventory, or in Curios's charm slot when
 * Curios is installed (the loader module registers it), so they needn't take an inventory slot.
 */
public final class CarriedItems {
    private static volatile ExtraCarrySlots extraSlots = player -> List.of();

    private CarriedItems() {
    }

    /** Called once by the loader module when Curios is installed. */
    public static void useExtraSlots(ExtraCarrySlots slots) {
        extraSlots = slots;
    }

    /** Whether the player carries the item: in the inventory, or in an extra carry slot. Works on either side. */
    public static boolean isCarried(Player player, Item item) {
        return player.getInventory().hasAnyMatching(stack -> stack.is(item))
                || extraSlots.carriedIn(player).stream().anyMatch(stack -> stack.is(item));
    }

    /** Carry slots beyond the inventory. */
    @FunctionalInterface
    public interface ExtraCarrySlots {
        List<ItemStack> carriedIn(Player player);
    }
}
