package com.impact.realisticvitality.client;

import com.impact.lib.ResourceIds;
import com.impact.lib.hud.HudLayout;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.config.VitalityNeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * A small cloud at the end of vanilla's air bubble row while the air is bad: gray for Thin, darker gray for Very Thin,
 * orange for Smoky. It sits where an eleventh bubble would, in the row vanilla just drew.
 */
public final class AirQualityIcon {
    private static final ResourceLocation TEXTURE = ResourceIds.of(VitalityMod.MOD_ID, "textures/gui/air_quality.png");
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 16;
    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = 8;
    private static final int BUBBLES = 10;
    private static final int ROW_HEIGHT = 10;
    private static final int THIN_U = 0;
    private static final int VERY_THIN_U = 9;
    private static final int SMOKY_U = 18;

    private AirQualityIcon() {
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        LocalPlayer player = Minecraft.getInstance().player;
        AirQuality quality = ClientVitality.state().airQuality();
        boolean showsBubbles = player != null && (player.isUnderWater() || player.getAirSupply() < player.getMaxAirSupply());
        if (!showsBubbles || quality == AirQuality.GOOD || !ClientVitality.isNeedOn(VitalityNeed.AIR)) {
            return;
        }
        int rowTop = HudLayout.lastRightRowTop(screenHeight, ROW_HEIGHT);
        int iconLeft = HudLayout.rightRowsRightEdge(screenWidth) - BUBBLES * ICON_SPACING - ICON_SIZE;
        graphics.blit(TEXTURE, iconLeft, rowTop, iconU(quality), 0, ICON_SIZE, ICON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static int iconU(AirQuality quality) {
        return switch (quality) {
            case VERY_THIN -> VERY_THIN_U;
            case SMOKY, VERY_SMOKY -> SMOKY_U;
            case THIN, GOOD -> THIN_U;
        };
    }
}
