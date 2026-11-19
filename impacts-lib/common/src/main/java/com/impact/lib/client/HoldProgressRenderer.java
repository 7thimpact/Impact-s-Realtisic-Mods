package com.impact.lib.client;

import com.impact.lib.config.LibClientConfig;
import com.impact.lib.hold.HoldToUse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

import java.util.Optional;

/** A thin bar under the crosshair that fills during each hold-to-use cycle (one ground item, one hammer blow). */
public final class HoldProgressRenderer {
    private static final int BAR_WIDTH = 32;
    private static final int BAR_HEIGHT = 3;
    private static final int GAP_BELOW_CROSSHAIR = 10;
    private static final int BACKGROUND_COLOR = 0x80000000;
    private static final int FILL_COLOR = 0xFFE0E0E0;

    private HoldProgressRenderer() {
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !LibClientConfig.get().holdToUse.isProgressBarShown.get()) {
            return;
        }
        Optional<Float> progress = HoldToUse.clientProgress(player);
        if (progress.isEmpty()) {
            return;
        }
        int left = (screenWidth - BAR_WIDTH) / 2;
        int top = screenHeight / 2 + GAP_BELOW_CROSSHAIR;
        graphics.fill(left, top, left + BAR_WIDTH, top + BAR_HEIGHT, BACKGROUND_COLOR);
        graphics.fill(left, top, left + Math.round(BAR_WIDTH * progress.get()), top + BAR_HEIGHT, FILL_COLOR);
    }
}
