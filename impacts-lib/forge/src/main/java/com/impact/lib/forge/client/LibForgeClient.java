package com.impact.lib.forge.client;

import com.impact.lib.client.HoldProgressRenderer;
import com.impact.lib.forge.client.animation.PlayerAnimatorHoldMotions;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Lib's client side on Forge: the hold-to-use progress bar, and playerAnimator arm motions when that mod is installed. */
public final class LibForgeClient {
    private static final String PLAYER_ANIMATOR_MOD_ID = "playeranimator";
    private static final String HOLD_PROGRESS_OVERLAY = "hold_progress";

    private LibForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(LibForgeClient::onRegisterOverlays);
        modEventBus.addListener(LibForgeClient::onClientSetup);
    }

    private static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), HOLD_PROGRESS_OVERLAY,
                (gui, graphics, partialTick, screenWidth, screenHeight) -> HoldProgressRenderer.render(graphics, screenWidth, screenHeight));
    }

    // The playerAnimator class is only touched when that mod is present, so Lib never needs it.
    private static void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded(PLAYER_ANIMATOR_MOD_ID)) {
            PlayerAnimatorHoldMotions.register();
        }
    }
}
