package com.impact.lib.attachment;

/** What happens to a player attachment when the player dies and respawns. Returning from the End keeps everything. */
public enum DeathCopyRule {
    /** Always kept, like statistics (Lib's counters). */
    ALWAYS,
    /** Kept only when the keepInventory game rule is on, like the inventory (Inventory's back slot). */
    LIKE_INVENTORY,
    /** Reset on death, like hunger (Vitality's thirst). */
    NEVER
}
