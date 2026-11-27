package com.impact.realisticinventory.client;

import com.impact.lib.client.CraftingProgressRenderer;
import com.impact.lib.client.DrawsOwnCraftingProgress;
import com.impact.lib.crafting.CraftingProgress;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornPack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Inventory's inventory screen, in place of vanilla's. It shows vanilla's own inventory menu with the slots moved: the
 * 9 pockets as a 3 by 3 grid with the main hand below them, and vanilla's off hand, armor and 2 by 2 crafting grid.
 * The worn slots (back and chest) are Curios's, in Curios's own screen; buttons beside the main hand open the packs. Timed crafts (Crafting's 2 by 2 timer) draw their filling arrow
 * through Lib.
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
    // The open-pack buttons sit beside the main hand's frame, centered on it: the back pack's right of it, the chest pack's left.
    private static final int SLOT_SIZE = 16;
    private static final int BUTTON_GAP = 4;
    private static final int OPEN_BACK_PACK_BUTTON_X = InventoryScreenLayout.MAIN_HAND_LEFT + SLOT_SIZE + BUTTON_GAP;
    private static final int OPEN_CHEST_PACK_BUTTON_X = InventoryScreenLayout.MAIN_HAND_LEFT - BUTTON_GAP - OpenPackButton.WIDTH;
    private static final int OPEN_PACK_BUTTON_Y = InventoryScreenLayout.MAIN_HAND_TOP + (SLOT_SIZE - OpenPackButton.HEIGHT) / 2;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private final InventoryScreenLayout layout = new InventoryScreenLayout();
    private final List<OpenPackButton> openPackButtons = new ArrayList<>();
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
        layout.moveIntoLayout(menu);
        openPackButtons.clear();
        for (WornOn place : WornOn.values()) {
            openPackButtons.add(addRenderableWidget(new OpenPackButton(leftPos + openPackButtonX(place), topPos + OPEN_PACK_BUTTON_Y, place)));
        }
        LOG.debug(InventoryLogCategories.SCREEN, "inventory screen opened");
        LOG.verbose(InventoryLogCategories.SCREEN, "inventory screen layout", "pocketsLeft", InventoryScreenLayout.POCKETS_LEFT,
                "pocketsTop", InventoryScreenLayout.POCKETS_TOP, "mainHandLeft", InventoryScreenLayout.MAIN_HAND_LEFT,
                "mainHandTop", InventoryScreenLayout.MAIN_HAND_TOP);
    }

    @Override
    public void removed() {
        layout.restore();
        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The recipe book moves the whole screen sideways, so the buttons follow leftPos every frame.
        for (OpenPackButton button : openPackButtons) {
            button.setPosition(leftPos + openPackButtonX(button.place()), topPos + OPEN_PACK_BUTTON_Y);
            button.visible = WornPack.of(minecraft.player, button.place()).isPresent();
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        InventoryScreenBackground.draw(graphics, leftPos, topPos);
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

    /** B and N (the open-pack keys) open the worn packs, unless something else took the key first (the recipe book's search). */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        Optional<WornOn> opened = InventoryKeys.placeOpenedBy(keyCode, scanCode);
        opened.ifPresent(InventoryKeys::requestOpenPack);
        return opened.isPresent();
    }

    private static int openPackButtonX(WornOn place) {
        return place == WornOn.BACK ? OPEN_BACK_PACK_BUTTON_X : OPEN_CHEST_PACK_BUTTON_X;
    }
}
