package com.impact.realisticinventory.client;

import com.impact.realisticinventory.hotbar.HotbarState;
import com.impact.realisticinventory.inventory.InventoryLayout;
import net.minecraft.client.gui.GuiGraphics;

/** Draws Inventory's inventory screen: vanilla's top half, then a frame for each pocket and hand, the active hand highlighted. */
final class InventoryScreenBackground {
    static final int SCREEN_HEIGHT = 166;

    private InventoryScreenBackground() {
    }

    static void draw(GuiGraphics graphics, int left, int top, int activeSlot) {
        GuiTexturePieces.drawInventoryTopHalfPanel(graphics, left, top, SCREEN_HEIGHT);
        for (int pocket = 0; pocket < InventoryLayout.POCKET_COUNT; pocket++) {
            int column = pocket % InventoryScreenLayout.POCKET_COLUMNS;
            int row = pocket / InventoryScreenLayout.POCKET_COLUMNS;
            GuiTexturePieces.drawSlotFrame(graphics, left + InventoryScreenLayout.pocketLeft(column), top + InventoryScreenLayout.pocketTop(row));
        }
        int handsTop = top + InventoryScreenLayout.HANDS_TOP;
        GuiTexturePieces.drawSlotFrame(graphics, left + InventoryScreenLayout.handLeft(HotbarState.FIRST_SLOT), handsTop);
        GuiTexturePieces.drawSlotFrame(graphics, left + InventoryScreenLayout.handLeft(HotbarState.SECOND_SLOT), handsTop);
        GuiTexturePieces.drawActiveHandFrame(graphics, left + InventoryScreenLayout.handLeft(activeSlot), handsTop);
    }
}
