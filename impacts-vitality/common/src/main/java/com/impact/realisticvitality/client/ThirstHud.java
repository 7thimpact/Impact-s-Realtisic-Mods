package com.impact.realisticvitality.client;

import com.impact.lib.ResourceIds;
import com.impact.lib.hud.HudLayout;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityClientConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.registry.VitalityEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/**
 * The thirst row: 10 droplets directly above the hunger row, mirroring it. They shake when thirst is low and turn green
 * while Parched. The row claims its place through Lib's HUD layout, so the air bubbles stack above it.
 */
public final class ThirstHud {
    private static final ResourceLocation TEXTURE = ResourceIds.of(VitalityMod.MOD_ID, "textures/gui/thirst.png");
    private static final int TEXTURE_SIZE = 32;
    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = 8;
    private static final int ROW_HEIGHT = 10;
    private static final int DROPLETS = 10;
    private static final int EMPTY_U = 0;
    private static final int FULL_U = 9;
    private static final int HALF_U = 18;
    private static final int PARCHED_V = 9;
    /** Vanilla seeds its HUD shake from the tick count with this number. */
    private static final long SHAKE_SEED = 312871L;

    private ThirstHud() {
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int guiTicks) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ClientVitality.isNeedOn(VitalityNeed.THIRST)) {
            return;
        }
        int rowTop = HudLayout.claimRightRow(screenHeight, ROW_HEIGHT);
        int rightEdge = HudLayout.rightRowsRightEdge(screenWidth);
        int thirst = ClientVitality.state().thirstLevel();
        int iconV = player.hasEffect(VitalityEffects.parched()) ? PARCHED_V : 0;
        boolean isShaking = isShaking(thirst, guiTicks);
        RandomSource shake = RandomSource.create(guiTicks * SHAKE_SEED);
        for (int droplet = 0; droplet < DROPLETS; droplet++) {
            int iconLeft = rightEdge - droplet * ICON_SPACING - ICON_SIZE;
            int iconTop = isShaking ? rowTop + shake.nextInt(3) - 1 : rowTop;
            drawDroplet(graphics, iconLeft, iconTop, iconV, droplet * 2 + 1, thirst);
        }
    }

    private static boolean isShaking(int thirst, int guiTicks) {
        boolean isLow = thirst <= VitalityServerConfig.get().thirst.noSprintAtOrBelow.get();
        return isLow && VitalityClientConfig.get().display.isDropletShakeOn.get() && guiTicks % (thirst * 3 + 1) == 0;
    }

    private static void drawDroplet(GuiGraphics graphics, int iconLeft, int iconTop, int iconV, int halfPoint, int thirst) {
        graphics.blit(TEXTURE, iconLeft, iconTop, EMPTY_U, iconV, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        if (halfPoint < thirst) {
            graphics.blit(TEXTURE, iconLeft, iconTop, FULL_U, iconV, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if (halfPoint == thirst) {
            graphics.blit(TEXTURE, iconLeft, iconTop, HALF_U, iconV, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
