package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.lib.hud.HudBounds;
import com.impact.lib.hud.HudLayout;
import com.impact.realisticinventory.config.InventoryClientConfig;
import com.impact.realisticinventory.inventory.InventoryLocks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the hands at the bottom center: the one hotbar slot (the main hand, highlighted) and vanilla's off-hand slot
 * beside it, on the off-hand side, always shown so the off hand is never out of sight.
 */
public final class HandsHotbarRenderer {
    private static final ResourceLocation WIDGETS_TEXTURE = ResourceIds.of("minecraft", "textures/gui/widgets.png");
    // The slot is cut from vanilla's 182-pixel hotbar: its first slot with the left border, then the right border.
    private static final int SLOT_PIECE_WIDTH = 21;
    private static final int RIGHT_BORDER_U = 181;
    private static final int RIGHT_BORDER_WIDTH = 1;
    private static final int HOTBAR_WIDTH = SLOT_PIECE_WIDTH + RIGHT_BORDER_WIDTH;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int SELECTION_V = 22;
    private static final int SELECTION_SIZE = 24;
    private static final int ITEM_INSET = 3;
    // Vanilla's off-hand frame: 29 by 24, cut for the left side or the right side of the hotbar.
    private static final int OFF_HAND_FRAME_WIDTH = 29;
    private static final int OFF_HAND_FRAME_HEIGHT = 24;
    private static final int OFF_HAND_FRAME_V = 22;
    private static final int LEFT_OFF_HAND_FRAME_U = 24;
    private static final int RIGHT_OFF_HAND_FRAME_U = 53;
    private static final int LEFT_OFF_HAND_ITEM_INSET = 3;
    private static final int RIGHT_OFF_HAND_ITEM_INSET = 10;

    private HandsHotbarRenderer() {
    }

    /** Where the hands are drawn, for Lib's HUD layout. Vanilla's bounds while the locks don't apply. */
    public static HudBounds bounds(int screenWidth, int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !InventoryLocks.isActiveFor(player)) {
            return HudLayout.vanillaHotbarBounds(screenWidth, screenHeight);
        }
        InventoryClientConfig.HotbarDisplaySettings display = InventoryClientConfig.get().hotbar;
        double scale = display.scale.get();
        int scaledWidth = (int) Math.round((HOTBAR_WIDTH + OFF_HAND_FRAME_WIDTH) * scale);
        int scaledHeight = (int) Math.round(HOTBAR_HEIGHT * scale);
        int scaledSlotWidth = (int) Math.round(HOTBAR_WIDTH * scale);
        int slotLeft = screenWidth / 2 - scaledSlotWidth / 2 + display.offsetX.get();
        int left = isOffHandOnLeft(player) ? slotLeft - (scaledWidth - scaledSlotWidth) : slotLeft;
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
        boolean isOffHandOnLeft = isOffHandOnLeft(player);
        drawFrames(graphics, isOffHandOnLeft);
        drawItem(graphics, player, player.getMainHandItem(), ITEM_INSET, 1);
        int offHandItemLeft = isOffHandOnLeft ? -OFF_HAND_FRAME_WIDTH + LEFT_OFF_HAND_ITEM_INSET : HOTBAR_WIDTH + RIGHT_OFF_HAND_ITEM_INSET;
        drawItem(graphics, player, player.getOffhandItem(), offHandItemLeft, 2);
        graphics.pose().popPose();
    }

    private static void drawFrames(GuiGraphics graphics, boolean isOffHandOnLeft) {
        RenderSystem.enableBlend();
        graphics.blit(WIDGETS_TEXTURE, 0, 0, 0, 0, SLOT_PIECE_WIDTH, HOTBAR_HEIGHT);
        graphics.blit(WIDGETS_TEXTURE, SLOT_PIECE_WIDTH, 0, RIGHT_BORDER_U, 0, RIGHT_BORDER_WIDTH, HOTBAR_HEIGHT);
        graphics.blit(WIDGETS_TEXTURE, -1, -1, 0, SELECTION_V, SELECTION_SIZE, SELECTION_SIZE);
        int frameLeft = isOffHandOnLeft ? -OFF_HAND_FRAME_WIDTH : HOTBAR_WIDTH;
        int frameU = isOffHandOnLeft ? LEFT_OFF_HAND_FRAME_U : RIGHT_OFF_HAND_FRAME_U;
        graphics.blit(WIDGETS_TEXTURE, frameLeft, -1, frameU, OFF_HAND_FRAME_V, OFF_HAND_FRAME_WIDTH, OFF_HAND_FRAME_HEIGHT);
        RenderSystem.disableBlend();
    }

    private static void drawItem(GuiGraphics graphics, LocalPlayer player, ItemStack stack, int itemLeft, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(player, stack, itemLeft, ITEM_INSET, seed);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, itemLeft, ITEM_INSET);
    }

    // Like vanilla: the off hand sits on the side of the arm that holds it.
    private static boolean isOffHandOnLeft(LocalPlayer player) {
        return player.getMainArm().getOpposite() == HumanoidArm.LEFT;
    }
}
