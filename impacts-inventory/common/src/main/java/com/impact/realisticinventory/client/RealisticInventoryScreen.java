package com.impact.realisticinventory.client;

import com.impact.lib.client.CraftingProgressRenderer;
import com.impact.lib.client.DrawsOwnCraftingProgress;
import com.impact.lib.crafting.CraftingProgress;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.pack.BackMenuSlot;
import com.impact.realisticinventory.pack.WornPack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;

/**
 * Inventory's inventory screen, in place of vanilla's. It shows vanilla's own inventory menu with the slots moved: the
 * 9 pockets as a 3 by 3 grid, the two hands below them, the back slot beside the player, and vanilla's armor and 2 by 2
 * crafting grid. Timed crafts (Crafting's 2 by 2 timer) draw their filling arrow through Lib.
 */
public final class RealisticInventoryScreen extends InventoryScreen implements DrawsOwnCraftingProgress {
    // Vanilla's player preview: its anchor in the screen, its size, and how far above the anchor the eyes look from.
    private static final int PLAYER_ANCHOR_X = 51;
    private static final int PLAYER_ANCHOR_Y = 75;
    private static final int PLAYER_SCALE = 30;
    private static final int PLAYER_EYE_HEIGHT = 50;
    // Vanilla's small crafting arrow and the 2 by 2 grid's output slot.
    private static final int CRAFTING_ARROW_X = 135;
    private static final int CRAFTING_ARROW_Y = 29;
    private static final int CRAFTING_OUTPUT_X = 154;
    private static final int CRAFTING_OUTPUT_Y = 28;
    private static final int FIRST_HAND_KEY_INDEX = 0;
    private static final int SECOND_HAND_KEY_INDEX = 1;
    // The open-pack button sits centered just above the back slot's frame.
    private static final int SLOT_FRAME_SIZE = 18;
    private static final int OPEN_PACK_BUTTON_X = BackMenuSlot.SCREEN_X - 1 + (SLOT_FRAME_SIZE - OpenPackButton.WIDTH) / 2;
    private static final int OPEN_PACK_BUTTON_Y = BackMenuSlot.SCREEN_Y - 2 - OpenPackButton.HEIGHT;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private final InventoryScreenLayout layout = new InventoryScreenLayout();
    private OpenPackButton openPackButton;
    private float lastMouseX;
    private float lastMouseY;

    public RealisticInventoryScreen(Player player) {
        super(player);
    }

    @Override
    protected void init() {
        super.init();
        // In creative mode vanilla's init opens the creative screen instead; this screen is already closed then.
        if (minecraft.screen != this) {
            return;
        }
        layout.moveIntoLayout(menu, ClientHotbar.activeSlot());
        openPackButton = addRenderableWidget(new OpenPackButton(leftPos + OPEN_PACK_BUTTON_X, topPos + OPEN_PACK_BUTTON_Y));
        LOG.debug(InventoryLogCategories.SCREEN, "inventory screen opened", "activeSlot", ClientHotbar.activeSlot());
        LOG.verbose(InventoryLogCategories.SCREEN, "inventory screen layout", "pocketsLeft", InventoryScreenLayout.POCKETS_LEFT,
                "pocketsTop", InventoryScreenLayout.POCKETS_TOP, "handsLeft", InventoryScreenLayout.HANDS_LEFT,
                "handsTop", InventoryScreenLayout.HANDS_TOP);
    }

    @Override
    public void removed() {
        layout.restore();
        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        layout.placeHands(menu, ClientHotbar.activeSlot());
        // The recipe book moves the whole screen sideways, so the button follows leftPos every frame.
        openPackButton.setPosition(leftPos + OPEN_PACK_BUTTON_X, topPos + OPEN_PACK_BUTTON_Y);
        openPackButton.visible = WornPack.of(minecraft.player).isPresent();
        super.render(graphics, mouseX, mouseY, partialTick);
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        InventoryScreenBackground.draw(graphics, leftPos, topPos, ClientHotbar.activeSlot());
        int playerX = leftPos + PLAYER_ANCHOR_X;
        int playerY = topPos + PLAYER_ANCHOR_Y;
        renderEntityInInventoryFollowsMouse(graphics, playerX, playerY, PLAYER_SCALE, playerX - lastMouseX,
                playerY - PLAYER_EYE_HEIGHT - lastMouseY, minecraft.player);
        CraftingProgress.get(menu).ifPresent(progress ->
                CraftingProgressRenderer.renderInventoryArrow(graphics, leftPos + CRAFTING_ARROW_X, topPos + CRAFTING_ARROW_Y, progress));
    }

    // Labels are drawn after the slots' items, so the faded cover over a running craft's output belongs here.
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        CraftingProgress.get(menu).ifPresent(progress ->
                CraftingProgressRenderer.renderFadedOutput(graphics, CRAFTING_OUTPUT_X, CRAFTING_OUTPUT_Y, progress));
    }

    /** B (the open-pack key) opens the worn pack, unless something else took the key first (the recipe book's search). */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (InventoryKeys.isOpenPackKey(keyCode, scanCode)) {
            InventoryKeys.requestOpenPack();
            return true;
        }
        return false;
    }

    /** Keys 1 and 2 over a slot swap it with the hand drawn as hotbar slot 1 or 2 here, matching the HUD. */
    @Override
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        if (!menu.getCarried().isEmpty() || hoveredSlot == null) {
            return super.checkHotbarKeyPressed(keyCode, scanCode);
        }
        KeyMapping[] hotbarKeys = minecraft.options.keyHotbarSlots;
        if (hotbarKeys[FIRST_HAND_KEY_INDEX].matches(keyCode, scanCode)) {
            swapHoveredSlotWithHand(FIRST_HAND_KEY_INDEX + 1);
            return true;
        }
        if (hotbarKeys[SECOND_HAND_KEY_INDEX].matches(keyCode, scanCode)) {
            swapHoveredSlotWithHand(SECOND_HAND_KEY_INDEX + 1);
            return true;
        }
        return super.checkHotbarKeyPressed(keyCode, scanCode);
    }

    private void swapHoveredSlotWithHand(int hotbarSlot) {
        boolean isMainHand = hotbarSlot == ClientHotbar.activeSlot();
        int handIndex = isMainHand ? InventoryLayout.MAIN_HAND_INDEX : InventoryLayout.OFF_HAND_INDEX;
        slotClicked(hoveredSlot, hoveredSlot.index, handIndex, ClickType.SWAP);
    }
}
