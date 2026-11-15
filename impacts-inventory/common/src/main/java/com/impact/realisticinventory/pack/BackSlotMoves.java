package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLayout;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Shift-click in the inventory screen: a pack (or other back-slot item) goes on the back when the slot is free. */
public final class BackSlotMoves {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private BackSlotMoves() {
    }

    /**
     * Moves one item from the clicked slot into the empty back slot. Returns an empty stack when it moved (so the
     * menu's repeat loop stops), or nothing to let vanilla's shift-click run.
     */
    public static Optional<ItemStack> quickMoveIntoBackSlot(InventoryMenu menu, Player player, int clickedSlotIndex) {
        boolean isBackOrResultSlot = clickedSlotIndex == InventoryLayout.MENU_BACK_SLOT || clickedSlotIndex == InventoryMenu.RESULT_SLOT;
        boolean isKnownSlot = clickedSlotIndex >= 0 && clickedSlotIndex < menu.slots.size() && menu.slots.size() > InventoryLayout.MENU_BACK_SLOT;
        if (isBackOrResultSlot || !isKnownSlot) {
            return Optional.empty();
        }
        Slot clickedSlot = menu.slots.get(clickedSlotIndex);
        Slot backSlot = menu.slots.get(InventoryLayout.MENU_BACK_SLOT);
        ItemStack clickedStack = clickedSlot.getItem();
        if (clickedStack.isEmpty() || backSlot.hasItem() || !backSlot.mayPlace(clickedStack)) {
            return Optional.empty();
        }
        backSlot.setByPlayer(clickedStack.split(1));
        if (clickedStack.isEmpty()) {
            clickedSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }
        if (!player.level().isClientSide()) {
            LOG.debug(InventoryLogCategories.PACKS, "worn from shift-click", "player", player.getGameProfile().getName(),
                    "item", ResourceIds.itemId(backSlot.getItem().getItem()));
        }
        return Optional.of(ItemStack.EMPTY);
    }
}
