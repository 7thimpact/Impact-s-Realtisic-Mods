package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

/**
 * Pieces of vanilla's own GUI textures that Inventory's screens are built from, so resource packs restyle them too:
 * the inventory screen's top half, its top and bottom edges, a blank panel row, a slot frame, and the hotbar's
 * selection frame.
 */
final class GuiTexturePieces {
    static final int SCREEN_WIDTH = 176;
    private static final ResourceLocation INVENTORY_TEXTURE = AbstractContainerScreen.INVENTORY_LOCATION;
    private static final ResourceLocation WIDGETS_TEXTURE = ResourceIds.of("minecraft", "textures/gui/widgets.png");
    private static final int TEXTURE_SIZE = 256;
    // Rows of vanilla's inventory texture.
    private static final int TOP_HALF_HEIGHT = 80;
    private static final int TOP_EDGE_HEIGHT = 7;
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

    private GuiTexturePieces() {
    }

    /** Vanilla's inventory screen down to its crafting area (armor, player, 2 by 2 grid), then a blank panel to the bottom edge. */
    static void drawInventoryTopHalfPanel(GuiGraphics graphics, int left, int top, int screenHeight) {
        graphics.blit(INVENTORY_TEXTURE, left, top, 0, 0, SCREEN_WIDTH, TOP_HALF_HEIGHT);
        drawBlankRows(graphics, left, top + TOP_HALF_HEIGHT, screenHeight - BOTTOM_EDGE_HEIGHT - TOP_HALF_HEIGHT);
        drawBottomEdge(graphics, left, top, screenHeight);
    }

    /** An empty panel with vanilla's edges. */
    static void drawEmptyPanel(GuiGraphics graphics, int left, int top, int screenHeight) {
        graphics.blit(INVENTORY_TEXTURE, left, top, 0, 0, SCREEN_WIDTH, TOP_EDGE_HEIGHT);
        drawBlankRows(graphics, left, top + TOP_EDGE_HEIGHT, screenHeight - BOTTOM_EDGE_HEIGHT - TOP_EDGE_HEIGHT);
        drawBottomEdge(graphics, left, top, screenHeight);
    }

    /** A slot frame around the slot whose item sits at the given screen position. */
    static void drawSlotFrame(GuiGraphics graphics, int slotLeft, int slotTop) {
        graphics.blit(INVENTORY_TEXTURE, slotLeft - 1, slotTop - 1, SLOT_FRAME_U, SLOT_FRAME_V, SLOT_FRAME_SIZE, SLOT_FRAME_SIZE);
    }

    /** The hotbar's highlight around the active hand's slot. */
    static void drawActiveHandFrame(GuiGraphics graphics, int slotLeft, int slotTop) {
        graphics.blit(WIDGETS_TEXTURE, slotLeft - SELECTION_OVERHANG, slotTop - SELECTION_OVERHANG, SELECTION_U, SELECTION_V,
                SELECTION_SIZE, SELECTION_SIZE);
    }

    private static void drawBlankRows(GuiGraphics graphics, int left, int top, int height) {
        graphics.blit(INVENTORY_TEXTURE, left, top, SCREEN_WIDTH, height, 0, BLANK_ROW_V, SCREEN_WIDTH, 1, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static void drawBottomEdge(GuiGraphics graphics, int left, int top, int screenHeight) {
        graphics.blit(INVENTORY_TEXTURE, left, top + screenHeight - BOTTOM_EDGE_HEIGHT, 0, BOTTOM_EDGE_V, SCREEN_WIDTH, BOTTOM_EDGE_HEIGHT);
    }
}
