package com.impact.lib.hud;

import com.impact.lib.ImpactsLib;
import com.impact.lib.config.LibClientConfig;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.ClientPlatform;

/**
 * Lays out Impact HUD elements around the hotbar without overlapping. Inventory publishes the hotbar's bounds;
 * Vitality's rows and gauge position themselves from them. Vanilla bars keep their positions. Client side only.
 */
public final class HudLayout {
    private static final int VANILLA_HOTBAR_WIDTH = 182;
    private static final int VANILLA_HOTBAR_HEIGHT = 22;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private static volatile HotbarBoundsProvider hotbarBoundsProvider = HudLayout::vanillaHotbarBounds;
    private static volatile String hotbarBoundsModId = "minecraft";

    private HudLayout() {
    }

    /** Replaces vanilla's hotbar bounds with a mod's own, for example Inventory's two-slot hotbar. */
    public static void publishHotbarBounds(String modId, HotbarBoundsProvider provider) {
        hotbarBoundsProvider = provider;
        hotbarBoundsModId = modId;
        LOG.debug(LibLogCategories.LAYOUT, "hotbar bounds published", "mod", modId);
    }

    public static HudBounds hotbarBounds(int screenWidth, int screenHeight) {
        return hotbarBoundsProvider.boundsFor(screenWidth, screenHeight);
    }

    public static String hotbarBoundsModId() {
        return hotbarBoundsModId;
    }

    /** Where vanilla draws its hotbar: centered, at the bottom of the screen. */
    public static HudBounds vanillaHotbarBounds(int screenWidth, int screenHeight) {
        return new HudBounds(screenWidth / 2 - VANILLA_HOTBAR_WIDTH / 2, screenHeight - VANILLA_HOTBAR_HEIGHT,
                VANILLA_HOTBAR_WIDTH, VANILLA_HOTBAR_HEIGHT);
    }

    /** Claims the next row on the right (above hunger) and returns its top edge. Call while rendering a HUD overlay. */
    public static int claimRightRow(int screenHeight, int rowHeightPixels) {
        StatusRowHeights heights = ClientPlatform.current().statusRowHeights();
        int rowTop = screenHeight - heights.rightHeight() + rowOffsetY();
        heights.raiseRight(rowHeightPixels);
        return rowTop;
    }

    /** Claims the next row on the left (above health and armor) and returns its top edge. */
    public static int claimLeftRow(int screenHeight, int rowHeightPixels) {
        StatusRowHeights heights = ClientPlatform.current().statusRowHeights();
        int rowTop = screenHeight - heights.leftHeight() + rowOffsetY();
        heights.raiseLeft(rowHeightPixels);
        return rowTop;
    }

    /** The configured shift for Impact rows (client config {@code hud.rowOffsetX}). */
    public static int rowOffsetX() {
        return LibClientConfig.get().hud.rowOffsetX.get();
    }

    private static int rowOffsetY() {
        return LibClientConfig.get().hud.rowOffsetY.get();
    }
}
