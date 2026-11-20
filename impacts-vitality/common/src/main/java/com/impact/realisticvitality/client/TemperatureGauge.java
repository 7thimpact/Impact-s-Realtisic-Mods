package com.impact.realisticvitality.client;

import com.impact.lib.ResourceIds;
import com.impact.lib.hud.HudBounds;
import com.impact.lib.hud.HudLayout;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.TemperatureUnit;
import com.impact.realisticvitality.config.VitalityClientConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.player.TemperatureState;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.BodyState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The body temperature gauge: a round dial centered just above the hotbar, between the health and hunger columns. Its
 * face is blue when cold, white when comfortable and red when hot; a needle swings from left (−100) over the top to
 * right (+100), and a ring pulses at Cold, Hot and beyond. Carrying a Thermometer adds the ambient readout under it.
 * It centers on the hotbar bounds Lib publishes.
 */
public final class TemperatureGauge {
    private static final ResourceLocation TEXTURE = ResourceIds.of(VitalityMod.MOD_ID, "textures/gui/temperature_gauge.png");
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 16;
    private static final int DIAL_SIZE = 15;
    private static final int FACE_U = 16;
    private static final int RING_U = 32;
    private static final int NEEDLE_LENGTH = 5;
    private static final int NEEDLE_COLOR = 0xFF202020;
    /** Room for vanilla's experience bar between the hotbar and the dial. */
    private static final int EXPERIENCE_BAR_GAP = 7;
    /** Room for the experience level number, drawn centered above the bar when the player has levels. */
    private static final int LEVEL_NUMBER_HEIGHT = 9;
    private static final int COLD_COLOR = 0x3A7BFF;
    private static final int COMFORTABLE_COLOR = 0xFFFFFF;
    private static final int HOT_COLOR = 0xFF3A2A;
    private static final float PULSE_TICKS = 20.0F;
    private static final int READOUT_COLOR = 0xFFFFFF;
    private static final String NO_READING = "--";

    private TemperatureGauge() {
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int guiTicks, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ClientVitality.isNeedOn(VitalityNeed.TEMPERATURE)) {
            return;
        }
        HudBounds hotbar = HudLayout.hotbarBounds(screenWidth, screenHeight);
        int dialLeft = hotbar.left() + hotbar.width() / 2 - DIAL_SIZE / 2;
        int levelRoom = player.experienceLevel > 0 ? LEVEL_NUMBER_HEIGHT : 0;
        boolean hasThermometer = player.getInventory().hasAnyMatching(stack -> stack.is(VitalityItems.thermometer()));
        float readoutScale = VitalityClientConfig.get().display.readoutScale.get().floatValue();
        int readoutRoom = hasThermometer ? Mth.ceil(Minecraft.getInstance().font.lineHeight * readoutScale) : 0;
        int dialTop = hotbar.top() - EXPERIENCE_BAR_GAP - levelRoom - readoutRoom - DIAL_SIZE;
        float body = ClientVitality.state().bodyTemperature();
        if (hasThermometer) {
            drawReadout(graphics, dialLeft + DIAL_SIZE / 2, dialTop + DIAL_SIZE, readoutScale);
        }
        drawDial(graphics, dialLeft, dialTop, body);
        drawNeedle(graphics, dialLeft + DIAL_SIZE / 2, dialTop + DIAL_SIZE / 2, body);
        if (BodyState.of(body).isWarning()) {
            drawPulse(graphics, dialLeft, dialTop, body, guiTicks + partialTick);
        }
    }

    /** The face's color: white at 0, blending to blue at −100 and red at +100. */
    public static int faceColor(float body) {
        float share = Mth.clamp(Math.abs(body) / TemperatureState.HOTTEST, 0.0F, 1.0F);
        int extreme = body < 0 ? COLD_COLOR : HOT_COLOR;
        return blend(COMFORTABLE_COLOR, extreme, share);
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

    private static void drawDial(GuiGraphics graphics, int left, int top, float body) {
        graphics.blit(TEXTURE, left, top, 0, 0, DIAL_SIZE, DIAL_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int color = faceColor(body);
        RenderSystem.setShaderColor(red(color), green(color), blue(color), 1.0F);
        graphics.blit(TEXTURE, left, top, FACE_U, 0, DIAL_SIZE, DIAL_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // The needle points straight up at 0 and swings a quarter turn either way at ±100.
    private static void drawNeedle(GuiGraphics graphics, int centerX, int centerY, float body) {
        double angle = Mth.clamp(body / TemperatureState.HOTTEST, -1.0F, 1.0F) * Math.PI / 2;
        for (int step = 0; step <= NEEDLE_LENGTH; step++) {
            int x = centerX + (int) Math.round(Math.sin(angle) * step);
            int y = centerY - (int) Math.round(Math.cos(angle) * step);
            graphics.fill(x, y, x + 1, y + 1, NEEDLE_COLOR);
        }
    }

    private static void drawPulse(GuiGraphics graphics, int left, int top, float body, float time) {
        float strength = (float) (0.5 + 0.5 * Math.sin(time / PULSE_TICKS * 2.0 * Math.PI));
        int color = body < 0 ? COLD_COLOR : HOT_COLOR;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(red(color), green(color), blue(color), strength);
        graphics.blit(TEXTURE, left, top, RING_U, 0, DIAL_SIZE, DIAL_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static int blend(int from, int to, float share) {
        int red = Math.round(Mth.lerp(share, from >> 16 & 0xFF, to >> 16 & 0xFF));
        int green = Math.round(Mth.lerp(share, from >> 8 & 0xFF, to >> 8 & 0xFF));
        int blue = Math.round(Mth.lerp(share, from & 0xFF, to & 0xFF));
        return red << 16 | green << 8 | blue;
    }

    private static float red(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    private static float green(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    private static float blue(int color) {
        return (color & 0xFF) / 255.0F;
    }
}
