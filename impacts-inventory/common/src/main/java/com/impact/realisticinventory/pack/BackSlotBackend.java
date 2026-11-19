package com.impact.realisticinventory.pack;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Where the back slot's item is kept. The loader module supplies it: on Forge it is Curios's {@code back} slot, so
 * Curios saves it, syncs it to every client, drops it on death and shows it to Curios-aware mods.
 */
public interface BackSlotBackend {
    /** Whether the player has a back slot right now (Curios gives players one through Inventory's data). */
    boolean hasBackSlot(Player player);

    ItemStack get(Player player);

    /** Stores the stack. Returns false, changing nothing, when the player has no back slot. */
    boolean set(Player player, ItemStack stack);
}
