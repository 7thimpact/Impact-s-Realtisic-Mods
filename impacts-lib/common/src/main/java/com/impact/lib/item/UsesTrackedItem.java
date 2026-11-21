package com.impact.lib.item;

import net.minecraft.world.item.ItemStack;

/**
 * An item that wears out by uses counted in its NBT instead of vanilla damage (Crafting's pebbles and Wooden Buckets).
 * Unused, it carries no tag and stacks normally; once used it stacks to 1 and shows a durability bar. Its uses change
 * only through {@link UsesDurability}.
 */
public interface UsesTrackedItem {
    /** How many uses a fresh item has. Read when it's used, so a config value applies right away. */
    int maxUses(ItemStack stack);
}
