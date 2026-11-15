package com.impact.lib.client;

import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.CraftingProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a timed craft's progress: a furnace-style arrow that fills from left to right, and a faded output slot
 * while the craft runs. Client side only. Each screen passes the positions of its own arrow and output slot.
 */
public final class CraftingProgressRenderer {
    private static final ResourceLocation FURNACE_TEXTURE = ResourceIds.of("minecraft", "textures/gui/container/furnace.png");
    // The filled arrow in vanilla's furnace texture.
    private static final int FILLED_ARROW_U = 176;
    private static final int FILLED_ARROW_V = 14;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int SLOT_SIZE = 16;
    private static final int FADED_SLOT_COLOR = 0x99C6C6C6;
    // Items in slots are drawn up to this depth; the faded cover has to sit in front of them.
    private static final float ABOVE_ITEMS_DEPTH = 300.0F;

    private CraftingProgressRenderer() {
    }

    /** Draws the filled part of the arrow over a screen's own empty arrow, whose top-left corner is given. */
    public static void renderArrow(GuiGraphics graphics, int arrowLeft, int arrowTop, CraftingProgress.Progress progress) {
        int filledWidth = Math.round(ARROW_WIDTH * progress.fraction());
        if (filledWidth <= 0) {
            return;
        }
        graphics.blit(FURNACE_TEXTURE, arrowLeft, arrowTop, FILLED_ARROW_U, FILLED_ARROW_V, filledWidth, ARROW_HEIGHT);
    }

    /** Covers the output slot, whose top-left corner is given, so its item looks faded until the craft finishes. */
    public static void renderFadedOutput(GuiGraphics graphics, int slotLeft, int slotTop, CraftingProgress.Progress progress) {
        if (progress.isFinished()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, ABOVE_ITEMS_DEPTH);
        graphics.fill(slotLeft, slotTop, slotLeft + SLOT_SIZE, slotTop + SLOT_SIZE, FADED_SLOT_COLOR);
        graphics.pose().popPose();
    }
}
