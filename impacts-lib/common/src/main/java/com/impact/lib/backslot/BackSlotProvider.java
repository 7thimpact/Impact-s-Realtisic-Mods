package com.impact.lib.backslot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stores the player's back slot. Impact's Realistic Inventory implements this. */
public interface BackSlotProvider {
    ItemStack get(Player player);

    void set(Player player, ItemStack stack);

    /** Whether the stack is one of the provider's own back-slot items, such as Inventory's packs. */
    boolean acceptsOwnItem(ItemStack stack);
}
