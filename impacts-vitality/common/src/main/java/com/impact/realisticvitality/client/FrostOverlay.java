package com.impact.realisticvitality.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.temperature.BodyState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** While Freezing, the frost around the screen's edges that powder snow gives, drawn from vanilla's own texture. */
public final class FrostOverlay {
    private static final ResourceLocation POWDER_SNOW_OUTLINE = ResourceIds.of("minecraft", "textures/misc/powder_snow_outline.png");

    private FrostOverlay() {
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        boolean isFreezing = ClientVitality.isNeedOn(VitalityNeed.TEMPERATURE) && BodyState.of(ClientVitality.state().bodyTemperature()) == BodyState.FREEZING;
        if (!isFreezing) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        graphics.blit(POWDER_SNOW_OUTLINE, 0, 0, -90, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
