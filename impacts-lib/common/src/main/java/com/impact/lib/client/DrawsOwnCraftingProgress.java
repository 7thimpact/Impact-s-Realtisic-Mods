package com.impact.lib.client;

/**
 * Marks a screen that draws timed-craft progress itself, such as Inventory's inventory screen. Mods that draw the
 * progress over other screens (Crafting, over vanilla's inventory and crafting screens) skip these, so the arrow is
 * never drawn twice.
 */
public interface DrawsOwnCraftingProgress {
}
