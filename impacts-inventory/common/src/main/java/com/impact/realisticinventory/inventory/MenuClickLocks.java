package com.impact.realisticinventory.inventory;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/** Container clicks and shift-click moves under the locks, for every menu (vanilla or modded). */
public final class MenuClickLocks {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private MenuClickLocks() {
    }

    /**
     * Refuses clicks on a locked slot, and number-key swaps with a locked hotbar index (keys 2 to 9 in a container).
     * The client can't normally make these clicks; the server refuses them anyway and the menu resyncs.
     */
    public static boolean isRefused(AbstractContainerMenu menu, int slotId, int button, ClickType clickType, Player player) {
        if (!InventoryLocks.isActiveFor(player)) {
            return false;
        }
        boolean isLockedHotbarSwap = clickType == ClickType.SWAP && InventoryLayout.isLocked(button);
        boolean isLockedSlotClick = slotId >= 0 && slotId < menu.slots.size() && SlotLocks.isLocked(menu.slots.get(slotId));
        if (!isLockedHotbarSwap && !isLockedSlotClick) {
            return false;
        }
        if (!player.level().isClientSide()) {
            LOG.debug(InventoryLogCategories.LOCKS, "locked slot click refused", "player", player.getGameProfile().getName(),
                    "slot", slotId, "button", button, "type", clickType);
        }
        return true;
    }

    /**
     * A shift-click move into a range made only of the player's slots fills the pockets first, then the main hand.
     * Empty when the range holds other slots too, so vanilla's move runs.
     */
    public static Optional<Boolean> moveIntoPlayerSlots(List<Slot> menuSlots, ItemStack stack, int startIndex, int endIndex) {
        boolean isValidRange = startIndex >= 0 && endIndex <= menuSlots.size() && startIndex < endIndex;
        if (!isValidRange) {
            return Optional.empty();
        }
        List<Slot> slotsInRange = menuSlots.subList(startIndex, endIndex);
        Optional<Inventory> playerInventory = slotsInRange.stream()
                .map(slot -> slot.container)
                .filter(Inventory.class::isInstance)
                .map(Inventory.class::cast)
                .findFirst();
        if (playerInventory.isEmpty() || !InventoryLocks.isActiveFor(playerInventory.get().player)) {
            return Optional.empty();
        }
        return PlayerSlotMoves.targetsInOrder(slotsInRange, playerInventory.get())
                .map(targets -> PlayerSlotMoves.move(stack, targets));
    }
}
