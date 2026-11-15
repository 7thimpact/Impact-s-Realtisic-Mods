package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.hotbar.HotbarState;
import com.impact.realisticinventory.inventory.InventoryLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws Inventory's screen from pieces of vanilla's own inventory texture, so resource packs restyle it too: vanilla's
 * top half (armor, player, crafting), a blank panel below it, a slot frame for each pocket and hand, and the hotbar's
 * highlight around the active hand.
 */
final class InventoryScreenBackground {
    private static final ResourceLocation INVENTORY_TEXTURE = AbstractContainerScreen.INVENTORY_LOCATION;
    private static final ResourceLocation WIDGETS_TEXTURE = ResourceIds.of("minecraft", "textures/gui/widgets.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int SCREEN_WIDTH = 176;
    // Rows of vanilla's inventory texture: the top half, one blank panel row, and the bottom edge.
    private static final int TOP_HALF_HEIGHT = 80;
    private static final int BLANK_ROW_V = 80;
    private static final int BOTTOM_EDGE_V = 159;
    private static final int BOTTOM_EDGE_HEIGHT = 7;
    private static final int SLOT_FRAME_U = 7;
    private static final int SLOT_FRAME_V = 83;
    private static final int SLOT_FRAME_SIZE = 18;
    // The hotbar's selection frame in vanilla's widgets texture, 4 pixels wider than a slot on every side.
    private static final int SELECTION_U = 0;
    private static final int SELECTION_V = 22;
    private static final int SELECTION_SIZE = 24;
    private static final int SELECTION_OVERHANG = 4;

    private InventoryScreenBackground() {
    }

    static void draw(GuiGraphics graphics, int left, int top, int activeSlot) {
        drawPanel(graphics, left, top);
        for (int pocket = 0; pocket < InventoryLayout.POCKET_COUNT; pocket++) {
            int column = pocket % InventoryScreenLayout.POCKET_COLUMNS;
            int row = pocket / InventoryScreenLayout.POCKET_COLUMNS;
            drawSlotFrame(graphics, left + InventoryScreenLayout.pocketLeft(column), top + InventoryScreenLayout.pocketTop(row));
        }
        int handsTop = top + InventoryScreenLayout.HANDS_TOP;
        drawSlotFrame(graphics, left + InventoryScreenLayout.handLeft(HotbarState.FIRST_SLOT), handsTop);
        drawSlotFrame(graphics, left + InventoryScreenLayout.handLeft(HotbarState.SECOND_SLOT), handsTop);
        int activeHandLeft = left + InventoryScreenLayout.handLeft(activeSlot);
        graphics.blit(WIDGETS_TEXTURE, activeHandLeft - SELECTION_OVERHANG, handsTop - SELECTION_OVERHANG, SELECTION_U, SELECTION_V,
                SELECTION_SIZE, SELECTION_SIZE);
    }

    private static void drawPanel(GuiGraphics graphics, int left, int top) {
        graphics.blit(INVENTORY_TEXTURE, left, top, 0, 0, SCREEN_WIDTH, TOP_HALF_HEIGHT);
        int blankHeight = BOTTOM_EDGE_V - TOP_HALF_HEIGHT;
        graphics.blit(INVENTORY_TEXTURE, left, top + TOP_HALF_HEIGHT, SCREEN_WIDTH, blankHeight, 0, BLANK_ROW_V, SCREEN_WIDTH, 1,
                TEXTURE_SIZE, TEXTURE_SIZE);
        graphics.blit(INVENTORY_TEXTURE, left, top + BOTTOM_EDGE_V, 0, BOTTOM_EDGE_V, SCREEN_WIDTH, BOTTOM_EDGE_HEIGHT);
    }

    /** A slot frame around the slot whose item sits at the given screen position. */
    static void drawSlotFrame(GuiGraphics graphics, int slotLeft, int slotTop) {
        graphics.blit(INVENTORY_TEXTURE, slotLeft - 1, slotTop - 1, SLOT_FRAME_U, SLOT_FRAME_V, SLOT_FRAME_SIZE, SLOT_FRAME_SIZE);
    }
}
