package com.impact.realisticinventory.client;

import com.impact.realisticinventory.inventory.InventoryLayout;
import net.minecraft.client.gui.GuiGraphics;

/** Draws Inventory's inventory screen: vanilla's top half (with its off-hand frame), then a frame for each pocket and the highlighted main hand. */
final class InventoryScreenBackground {
    static final int SCREEN_HEIGHT = 166;

    private InventoryScreenBackground() {
    }

    static void draw(GuiGraphics graphics, int left, int top) {
        GuiTexturePieces.drawInventoryTopHalfPanel(graphics, left, top, SCREEN_HEIGHT);
        for (int pocket = 0; pocket < InventoryLayout.POCKET_COUNT; pocket++) {
            int column = pocket % InventoryScreenLayout.POCKET_COLUMNS;
            int row = pocket / InventoryScreenLayout.POCKET_COLUMNS;
            GuiTexturePieces.drawSlotFrame(graphics, left + InventoryScreenLayout.pocketLeft(column), top + InventoryScreenLayout.pocketTop(row));
        }
        int mainHandLeft = left + InventoryScreenLayout.MAIN_HAND_LEFT;
        int mainHandTop = top + InventoryScreenLayout.MAIN_HAND_TOP;
        GuiTexturePieces.drawSlotFrame(graphics, mainHandLeft, mainHandTop);
        GuiTexturePieces.drawActiveHandFrame(graphics, mainHandLeft, mainHandTop);
    }
}
