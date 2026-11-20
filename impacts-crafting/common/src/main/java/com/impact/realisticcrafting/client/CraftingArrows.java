package com.impact.realisticcrafting.client;

import com.impact.lib.client.CraftingProgressRenderer;
import com.impact.lib.crafting.CraftingProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Where vanilla's crafting screens have their arrow and output, for Lib's progress renderer: the crafting table's big
 * arrow (the makeshift tables use the same screen) and the inventory's small one.
 */
public final class CraftingArrows {
    private static final int TABLE_ARROW_X = 89;
    private static final int TABLE_ARROW_Y = 34;
    private static final int TABLE_OUTPUT_X = 124;
    private static final int TABLE_OUTPUT_Y = 35;
    private static final int INVENTORY_ARROW_X = 135;
    private static final int INVENTORY_ARROW_Y = 29;
    private static final int INVENTORY_OUTPUT_X = 154;
    private static final int INVENTORY_OUTPUT_Y = 28;

    private CraftingArrows() {
    }

    /** In the screen's background pass, at screen coordinates from the screen's corner ({@code guiLeft}, {@code guiTop}). */
    public static void renderArrow(GuiGraphics graphics, AbstractContainerScreen<?> screen, int guiLeft, int guiTop, CraftingProgress.Progress progress) {
        if (screen instanceof CraftingScreen) {
            CraftingProgressRenderer.renderArrow(graphics, guiLeft + TABLE_ARROW_X, guiTop + TABLE_ARROW_Y, progress);
        } else if (screen instanceof InventoryScreen) {
            CraftingProgressRenderer.renderInventoryArrow(graphics, guiLeft + INVENTORY_ARROW_X, guiTop + INVENTORY_ARROW_Y, progress);
        }
    }

    /** In the screen's foreground pass, where coordinates are relative to the screen's corner. */
    public static void renderFadedOutput(GuiGraphics graphics, AbstractContainerScreen<?> screen, CraftingProgress.Progress progress) {
        if (screen instanceof CraftingScreen) {
            CraftingProgressRenderer.renderFadedOutput(graphics, TABLE_OUTPUT_X, TABLE_OUTPUT_Y, progress);
        } else if (screen instanceof InventoryScreen) {
            CraftingProgressRenderer.renderFadedOutput(graphics, INVENTORY_OUTPUT_X, INVENTORY_OUTPUT_Y, progress);
        }
    }
}
