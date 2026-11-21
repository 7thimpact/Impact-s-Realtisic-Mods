package com.impact.realisticinventory.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Which vanilla inventory indexes Inventory uses. Vanilla's 36-slot inventory keeps its size, so mods that index into
 * it don't break: index 0 is the main hand, 9 to 17 are the pockets, and every other main-inventory index is locked
 * (always empty). The off hand is vanilla's off-hand slot.
 */
public final class InventoryLayout {
    public static final int MAIN_HAND_INDEX = 0;
    public static final int OFF_HAND_INDEX = Inventory.SLOT_OFFHAND;
    public static final int FIRST_POCKET_INDEX = 9;
    public static final int POCKET_COUNT = 9;
    public static final int LAST_POCKET_INDEX = FIRST_POCKET_INDEX + POCKET_COUNT - 1;
    /** In the player's inventory menu: the main hand shows as vanilla's first hotbar slot. */
    public static final int MENU_MAIN_HAND_SLOT = InventoryMenu.USE_ROW_SLOT_START;
    /** In the player's inventory menu: vanilla's off-hand slot. */
    public static final int MENU_OFF_HAND_SLOT = InventoryMenu.SHIELD_SLOT;
    private static final int MAIN_INVENTORY_SIZE = Inventory.INVENTORY_SIZE;

    private InventoryLayout() {
    }

    /** Locked indexes: the rest of vanilla's hotbar (1 to 8) and its second and third rows (18 to 35). */
    public static boolean isLocked(int inventoryIndex) {
        boolean isMainInventoryIndex = inventoryIndex >= 0 && inventoryIndex < MAIN_INVENTORY_SIZE;
        return isMainInventoryIndex && inventoryIndex != MAIN_HAND_INDEX && !isPocket(inventoryIndex);
    }

    public static boolean isPocket(int inventoryIndex) {
        return inventoryIndex >= FIRST_POCKET_INDEX && inventoryIndex <= LAST_POCKET_INDEX;
    }
}
