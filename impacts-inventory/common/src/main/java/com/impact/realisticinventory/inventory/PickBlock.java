package com.impact.realisticinventory.inventory;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Pick block (middle click) puts the item in the active slot, the main hand. Whatever was there goes to the pockets
 * through Lib's Give or drop, or drops at the player's feet when they are full.
 */
public final class PickBlock {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private PickBlock() {
    }

    /** Replaces vanilla's swap with a hotbar slot. Called with a pocket index the client found the item in. */
    public static void pickFromPocket(Inventory inventory, int pocketIndex) {
        if (!InventoryLayout.isPocket(pocketIndex)) {
            return;
        }
        ItemStack pickedItem = inventory.items.get(pocketIndex);
        ItemStack previousMainHandItem = inventory.items.get(InventoryLayout.MAIN_HAND_INDEX);
        inventory.items.set(pocketIndex, ItemStack.EMPTY);
        inventory.items.set(InventoryLayout.MAIN_HAND_INDEX, pickedItem);
        inventory.selected = InventoryLayout.MAIN_HAND_INDEX;
        inventory.setChanged();
        if (!previousMainHandItem.isEmpty()) {
            ImpactsItems.giveOrDrop(inventory.player, previousMainHandItem);
        }
        LOG.debug(InventoryLogCategories.HOTBAR, "pick block", "player", inventory.player.getGameProfile().getName(),
                "item", ResourceIds.itemId(pickedItem.getItem()), "fromPocket", pocketIndex);
    }
}
