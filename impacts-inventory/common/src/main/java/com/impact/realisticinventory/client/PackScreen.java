package com.impact.realisticinventory.client;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.pack.PackMenu;
import com.impact.realisticinventory.pack.PackMenuLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** The open pack: its slots in 3 by 3 grids above the pockets and hands. B (or the inventory key) closes it. */
public final class PackScreen extends AbstractContainerScreen<PackMenu> {
    private static final int LABEL_ABOVE_POCKETS = 11;
    private static final int LABEL_COLOR = 0x404040;
    private static final int MAIN_HAND_AFTER_POCKETS = InventoryLayout.POCKET_COUNT;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    public PackScreen(PackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = PackMenuLayout.SCREEN_WIDTH;
        imageHeight = menu.layout().screenHeight();
        titleLabelY = PackMenuLayout.TITLE_Y;
        inventoryLabelY = menu.layout().pocketsTop() - LABEL_ABOVE_POCKETS;
    }

    @Override
    protected void init() {
        super.init();
        LOG.debug(InventoryLogCategories.SCREEN, "pack screen opened", "slots", menu.packSlotCount());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiTexturePieces.drawEmptyPanel(graphics, leftPos, topPos, imageHeight);
        menu.slots.forEach(slot -> GuiTexturePieces.drawSlotFrame(graphics, leftPos + slot.x, topPos + slot.y));
        Slot mainHand = menu.slots.get(menu.packSlotCount() + MAIN_HAND_AFTER_POCKETS);
        GuiTexturePieces.drawActiveHandFrame(graphics, leftPos + mainHand.x, topPos + mainHand.y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (InventoryKeys.isOpenPackKey(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
