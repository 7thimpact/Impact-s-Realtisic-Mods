package com.impact.realisticinventory.pack;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Where the worn slots' items are kept. The loader module supplies it: on Forge the back is Curios's {@code back} slot
 * and the chest Curios's {@code body} slot, so Curios saves them, syncs them to every client, drops them on death and
 * shows them to Curios-aware mods.
 */
public interface WornSlotBackend {
    /** Whether the player has that slot right now (Curios gives players them through Inventory's data). */
    boolean hasSlot(Player player, WornOn place);

    ItemStack get(Player player, WornOn place);

    /** Stores the stack. Returns false, changing nothing, when the player has no such slot. */
    boolean set(Player player, WornOn place, ItemStack stack);
}
