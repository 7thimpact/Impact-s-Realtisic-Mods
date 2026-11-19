package com.impact.realisticinventory.client;

import com.impact.realisticinventory.mixin.SlotPositionAccessor;
import com.impact.realisticinventory.pack.BackSlotContainer;
import com.impact.realisticinventory.pack.BackSlotStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * The back slot in creative mode's inventory tab. Vanilla lays that tab out by slot index and would put the back slot
 * on top of a hotbar slot, so it moves to the right of the player, mirroring the off-hand slot on the left.
 */
public final class CreativeInventoryBackSlot {
    private static final int SLOT_X = 127;
    private static final int SLOT_Y = 20;

    private CreativeInventoryBackSlot() {
    }

    public static void placeBackSlot(AbstractContainerMenu creativeMenu) {
        creativeMenu.slots.stream()
                .filter(slot -> slot.container instanceof BackSlotContainer)
                .forEach(CreativeInventoryBackSlot::moveToBackSlotPosition);
    }

    public static void drawSlotFrame(GuiGraphics graphics, int left, int top) {
        if (!BackSlotStorage.isEnabled()) {
            return;
        }
        GuiTexturePieces.drawSlotFrame(graphics, left + SLOT_X, top + SLOT_Y);
    }

    private static void moveToBackSlotPosition(Slot slot) {
        SlotPositionAccessor position = (SlotPositionAccessor) slot;
        position.impactsinventory$setX(SLOT_X);
        position.impactsinventory$setY(SLOT_Y);
    }
}
