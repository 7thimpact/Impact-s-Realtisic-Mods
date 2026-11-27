package com.impact.realisticvitality.client;

import com.impact.lib.hud.HudBounds;
import com.impact.lib.hud.HudLayout;
import com.impact.realisticvitality.config.TemperatureUnit;
import com.impact.realisticvitality.config.VitalityClientConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.player.CarriedItems;
import com.impact.realisticvitality.player.TemperatureState;
import com.impact.realisticvitality.player.VitalityExemptions;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.BodyState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * The body temperature gauge, drawn like a boiler's pressure gauge just above the hotbar: a brass bezel around a cream
 * face, a 270° scale whose colored zones are always shown (blues for the cold bands, green for comfortable, then
 * yellow, orange and red for the hot bands), and a needle that sweeps from the coldest end at the lower left over the
 * top to the hottest at the lower right. A small arrow in the face says whether you're warming (red, up) or cooling
 * (blue, down), and the bezel flashes in the needle's zone color at Cold, Hot and beyond. Carrying a Thermometer adds
 * the ambient readout under it. It centers on the hotbar bounds Lib publishes.
 */
public final class TemperatureGauge {
    private static final int DIAL_SIZE = 21;
    private static final float DIAL_RADIUS = DIAL_SIZE / 2.0F;
    private static final float BEZEL_INNER_RADIUS = DIAL_RADIUS - 1.5F;
    private static final float SCALE_OUTER_RADIUS = BEZEL_INNER_RADIUS;
    private static final float SCALE_INNER_RADIUS = SCALE_OUTER_RADIUS - 3.0F;
    /** The scale runs 135° either side of straight up, leaving the bottom quarter open. */
    private static final double SCALE_HALF_SWEEP = Math.toRadians(135.0);
    private static final int NEEDLE_LENGTH = 7;
    private static final int BEZEL_COLOR = 0xFF8C6A2E;
    private static final int BEZEL_SHINE_COLOR = 0xFFC9A55A;
    private static final int FACE_COLOR = 0xFFF2EAD3;
    private static final int NEEDLE_COLOR = 0xFF1A1A1A;
    private static final int HUB_COLOR = 0xFF3A3A3A;
    private static final int FREEZING_COLOR = 0xFF1E3A8A;
    private static final int COLD_COLOR = 0xFF2F6FE0;
    private static final int CHILLY_COLOR = 0xFF7FC1FF;
    private static final int COMFORTABLE_COLOR = 0xFF4CB84C;
    private static final int WARM_COLOR = 0xFFF2D024;
    private static final int HOT_COLOR = 0xFFF28C1E;
    private static final int OVERHEATING_COLOR = 0xFFD9261C;
    private static final int WARMING_ARROW_COLOR = 0xFFD9261C;
    private static final int COOLING_ARROW_COLOR = 0xFF2F6FE0;
    private static final int ARROW_BELOW_CENTER = 4;
    /** Room for vanilla's experience bar between the hotbar and the dial. */
    private static final int EXPERIENCE_BAR_GAP = 7;
    /** Room for the experience level number, drawn centered above the bar when the player has levels. */
    private static final int LEVEL_NUMBER_HEIGHT = 9;
    private static final float PULSE_TICKS = 20.0F;
    private static final int READOUT_COLOR = 0xFFFFFF;
    private static final String NO_READING = "--";

    private TemperatureGauge() {
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int guiTicks, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ClientVitality.isNeedOn(VitalityNeed.TEMPERATURE) || VitalityExemptions.isExempt(player)) {
            return;
        }
        HudBounds hotbar = HudLayout.hotbarBounds(screenWidth, screenHeight);
        int dialLeft = hotbar.left() + hotbar.width() / 2 - DIAL_SIZE / 2;
        int levelRoom = player.experienceLevel > 0 ? LEVEL_NUMBER_HEIGHT : 0;
        boolean hasThermometer = CarriedItems.isCarried(player, VitalityItems.thermometer());
        float readoutScale = VitalityClientConfig.get().display.readoutScale.get().floatValue();
        int readoutRoom = hasThermometer ? Mth.ceil(Minecraft.getInstance().font.lineHeight * readoutScale) : 0;
        int dialTop = hotbar.top() - EXPERIENCE_BAR_GAP - levelRoom - readoutRoom - DIAL_SIZE;
        float body = ClientVitality.state().bodyTemperature();
        boolean isWarning = BodyState.of(body).isWarning();
        float pulse = (float) (0.5 + 0.5 * Math.sin((guiTicks + partialTick) / PULSE_TICKS * 2.0 * Math.PI));
        drawDial(graphics, dialLeft, dialTop, isWarning ? blend(BEZEL_COLOR, zoneColor(body), pulse) : BEZEL_COLOR);
        int centerX = dialLeft + DIAL_SIZE / 2;
        int centerY = dialTop + DIAL_SIZE / 2;
        drawTrendArrow(graphics, centerX, centerY + ARROW_BELOW_CENTER, ClientVitality.bodyTrend());
        drawNeedle(graphics, centerX, centerY, body);
        if (hasThermometer) {
            drawReadout(graphics, centerX, dialTop + DIAL_SIZE, readoutScale);
        }
    }

    /** The zone color the scale shows at a body temperature, from the server's band edges. */
    public static int zoneColor(float body) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        float magnitude = Math.abs(body);
        if (magnitude <= settings.warmAt.get()) {
            return COMFORTABLE_COLOR;
        }
        boolean isHot = body > 0;
        if (magnitude <= settings.hotAt.get()) {
            return isHot ? WARM_COLOR : CHILLY_COLOR;
        }
        if (magnitude <= settings.veryHotAt.get()) {
            return isHot ? HOT_COLOR : COLD_COLOR;
        }
        return isHot ? OVERHEATING_COLOR : FREEZING_COLOR;
    }

    // Pixel by pixel: the bezel, the cream face, and the scale's ring colored by the body temperature each angle stands for.
    private static void drawDial(GuiGraphics graphics, int left, int top, int bezelColor) {
        for (int row = 0; row < DIAL_SIZE; row++) {
            for (int column = 0; column < DIAL_SIZE; column++) {
                float offsetX = column + 0.5F - DIAL_RADIUS;
                float offsetY = row + 0.5F - DIAL_RADIUS;
                float distance = Mth.sqrt(offsetX * offsetX + offsetY * offsetY);
                int color = dialPixelColor(offsetX, offsetY, distance, bezelColor);
                if (color != 0) {
                    graphics.fill(left + column, top + row, left + column + 1, top + row + 1, color);
                }
            }
        }
    }

    private static int dialPixelColor(float offsetX, float offsetY, float distance, int bezelColor) {
        if (distance > DIAL_RADIUS) {
            return 0;
        }
        if (distance > BEZEL_INNER_RADIUS) {
            boolean isLitEdge = offsetX + offsetY < 0;
            return bezelColor == BEZEL_COLOR && isLitEdge ? BEZEL_SHINE_COLOR : bezelColor;
        }
        // Angle from straight up, clockwise: negative on the left (cold), positive on the right (hot).
        double angle = Math.atan2(offsetX, -offsetY);
        boolean isOnScale = distance >= SCALE_INNER_RADIUS && Math.abs(angle) <= SCALE_HALF_SWEEP;
        if (!isOnScale) {
            return FACE_COLOR;
        }
        float body = (float) (angle / SCALE_HALF_SWEEP * TemperatureState.HOTTEST);
        return zoneColor(body);
    }

    private static void drawNeedle(GuiGraphics graphics, int centerX, int centerY, float body) {
        double angle = Mth.clamp(body / TemperatureState.HOTTEST, -1.0F, 1.0F) * SCALE_HALF_SWEEP;
        for (int step = 0; step <= NEEDLE_LENGTH; step++) {
            int x = centerX + (int) Math.round(Math.sin(angle) * step);
            int y = centerY - (int) Math.round(Math.cos(angle) * step);
            graphics.fill(x, y, x + 1, y + 1, NEEDLE_COLOR);
        }
        graphics.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, HUB_COLOR);
    }

    // A 3-pixel-wide chevron: up and red while warming, down and blue while cooling, nothing while steady.
    private static void drawTrendArrow(GuiGraphics graphics, int centerX, int top, float trend) {
        if (trend == 0.0F) {
            return;
        }
        boolean isWarming = trend > 0.0F;
        int color = isWarming ? WARMING_ARROW_COLOR : COOLING_ARROW_COLOR;
        int tipRow = isWarming ? top : top + 1;
        int wideRow = isWarming ? top + 1 : top;
        graphics.fill(centerX, tipRow, centerX + 1, tipRow + 1, color);
        graphics.fill(centerX - 1, wideRow, centerX + 2, wideRow + 1, color);
    }

    // The ambient temperature in the units the client config asks for, centered under the dial.
    private static void drawReadout(GuiGraphics graphics, int centerX, int top, float scale) {
        float ambient = ClientVitality.state().ambientTemperature();
        TemperatureUnit unit = VitalityClientConfig.get().display.units.get();
        String readout = Float.isNaN(ambient) ? NO_READING + unit.symbol : Math.round(unit.fromFahrenheit(ambient)) + unit.symbol;
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, top, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, readout, -font.width(readout) / 2, 0, READOUT_COLOR, true);
        graphics.pose().popPose();
    }

    private static int blend(int from, int to, float share) {
        int red = Math.round(Mth.lerp(share, from >> 16 & 0xFF, to >> 16 & 0xFF));
        int green = Math.round(Mth.lerp(share, from >> 8 & 0xFF, to >> 8 & 0xFF));
        int blue = Math.round(Mth.lerp(share, from & 0xFF, to & 0xFF));
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}
