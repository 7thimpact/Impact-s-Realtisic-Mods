package com.impact.realisticinventory.forge;

import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

/** Hides Forge item-handler slots that show a locked player index, as modded menus sometimes build them. */
final class ForgeSlotLocks {
    private ForgeSlotLocks() {
    }

    static boolean isLockedItemHandlerSlot(Slot slot) {
        if (!(slot instanceof SlotItemHandler handlerSlot) || !(handlerSlot.getItemHandler() instanceof PlayerMainInvWrapper wrapper)) {
            return false;
        }
        return InventoryLayout.isLocked(slot.getContainerSlot()) && InventoryLocks.isActiveFor(wrapper.getInventoryPlayer().player);
    }
}
