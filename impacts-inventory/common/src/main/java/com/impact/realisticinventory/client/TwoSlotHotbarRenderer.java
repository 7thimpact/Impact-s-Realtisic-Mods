package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.lib.hud.HudBounds;
import com.impact.lib.hud.HudLayout;
import com.impact.realisticinventory.config.InventoryClientConfig;
import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.hotbar.HotbarState;
import com.impact.realisticinventory.inventory.InventoryLocks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the two-slot hotbar: slots 1 and 2 side by side at the bottom center, with vanilla's highlight on the active
 * slot. The active slot shows the main hand and the other slot the off hand, so items stay put when switching.
 */
public final class TwoSlotHotbarRenderer {
    private static final ResourceLocation WIDGETS_TEXTURE = ResourceIds.of("minecraft", "textures/gui/widgets.png");
    // Each slot is cut from vanilla's 182-pixel hotbar: its first slot with the left border, its last with the right.
    private static final int SLOT_PIECE_WIDTH = 21;
    private static final int LAST_SLOT_PIECE_U = 161;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int HOTBAR_WIDTH = SLOT_PIECE_WIDTH * 2;

    private static final int SLOT_STRIDE = 20;
    private static final int SELECTION_V = 22;
    private static final int SELECTION_SIZE = 24;
    private static final int ITEM_INSET = 3;
    private static final int LABEL_INSET = 2;
    private static final float LABEL_SCALE = 0.5F;
    private static final int LABEL_COLOR = 0xFFE0E0E0;
    private static final int SLOT_COUNT = 2;
    // Labels sit in front of the slot items.
    private static final float LABEL_DEPTH = 200.0F;

    private TwoSlotHotbarRenderer() {
    }

    /** Where the hotbar is drawn, for Lib's HUD layout. Vanilla's bounds while the two-slot hotbar isn't in use. */
    public static HudBounds bounds(int screenWidth, int screenHeight) {
        if (!InventoryLocks.isActiveFor(Minecraft.getInstance().player)) {
            return HudLayout.vanillaHotbarBounds(screenWidth, screenHeight);
        }
        InventoryClientConfig.HotbarDisplaySettings display = InventoryClientConfig.get().hotbar;
        double scale = display.scale.get();
        int scaledWidth = (int) Math.round(HOTBAR_WIDTH * scale);
        int scaledHeight = (int) Math.round(HOTBAR_HEIGHT * scale);
        int left = screenWidth / 2 - scaledWidth / 2 + display.offsetX.get();
        int top = screenHeight - scaledHeight + display.offsetY.get();
        return new HudBounds(left, top, scaledWidth, scaledHeight);
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        InventoryClientConfig.HotbarDisplaySettings display = InventoryClientConfig.get().hotbar;
        float scale = display.scale.get().floatValue();
        graphics.pose().pushPose();
        graphics.pose().translate(screenWidth / 2.0F + display.offsetX.get(), screenHeight + display.offsetY.get(), 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-HOTBAR_WIDTH / 2.0F, -HOTBAR_HEIGHT, 0.0F);
        drawSlots(graphics, HandSwitch.stateOf(player).activeSlot());
        drawItems(graphics, player);
        if (display.areSlotLabelsShown.get()) {
            drawLabels(graphics, Minecraft.getInstance().font);
        }
        graphics.pose().popPose();
    }

    private static void drawSlots(GuiGraphics graphics, int activeSlot) {
        RenderSystem.enableBlend();
        graphics.blit(WIDGETS_TEXTURE, 0, 0, 0, 0, SLOT_PIECE_WIDTH, HOTBAR_HEIGHT);
        graphics.blit(WIDGETS_TEXTURE, SLOT_PIECE_WIDTH, 0, LAST_SLOT_PIECE_U, 0, SLOT_PIECE_WIDTH, HOTBAR_HEIGHT);
        int activeSlotIndex = activeSlot - 1;
        graphics.blit(WIDGETS_TEXTURE, activeSlotIndex * SLOT_STRIDE - 1, -1, 0, SELECTION_V, SELECTION_SIZE, SELECTION_SIZE);
        RenderSystem.disableBlend();
    }

    private static void drawItems(GuiGraphics graphics, LocalPlayer player) {
        boolean isFirstSlotActive = HandSwitch.stateOf(player).activeSlot() == HotbarState.FIRST_SLOT;
        ItemStack firstSlotItem = isFirstSlotActive ? player.getMainHandItem() : player.getOffhandItem();
        ItemStack secondSlotItem = isFirstSlotActive ? player.getOffhandItem() : player.getMainHandItem();
        drawItem(graphics, player, firstSlotItem, 0);
        drawItem(graphics, player, secondSlotItem, 1);
    }

    private static void drawItem(GuiGraphics graphics, LocalPlayer player, ItemStack stack, int slotIndex) {
        if (stack.isEmpty()) {
            return;
        }
        int itemLeft = slotIndex * SLOT_STRIDE + ITEM_INSET;
        graphics.renderItem(player, stack, itemLeft, ITEM_INSET, slotIndex + 1);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, itemLeft, ITEM_INSET);
    }

    private static void drawLabels(GuiGraphics graphics, Font font) {
        for (int slotIndex = 0; slotIndex < SLOT_COUNT; slotIndex++) {
            graphics.pose().pushPose();
            graphics.pose().translate(slotIndex * SLOT_STRIDE + LABEL_INSET, LABEL_INSET, LABEL_DEPTH);
            graphics.pose().scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
            graphics.drawString(font, String.valueOf(slotIndex + 1), 0, 0, LABEL_COLOR, true);
            graphics.pose().popPose();
        }
    }
}
