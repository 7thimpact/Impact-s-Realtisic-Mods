package com.impact.realisticvitality.forge.client;

import com.impact.realisticvitality.client.ClientVitality;
import com.impact.realisticvitality.client.ThirstHud;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/** Vitality's client-only hooks on Forge. Loaded only on the physical client. */
public final class VitalityForgeClient {
    private static final String THIRST_OVERLAY = "thirst_level";

    private VitalityForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(VitalityForgeClient::onRegisterOverlays);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeClient::onLoggingOut);
    }

    // Right after hunger, so thirst sits directly above it and vanilla's air bubbles stack above thirst.
    private static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), THIRST_OVERLAY, (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            if (showsFoodRow(gui)) {
                gui.setupOverlayRenderState(true, false);
                ThirstHud.render(graphics, screenWidth, screenHeight, gui.getGuiTicks());
            }
        });
    }

    /** The same test Forge uses for the hunger row: hidden in creative, with the HUD off, and on a mount that shows its health. */
    private static boolean showsFoodRow(ForgeGui gui) {
        Minecraft minecraft = gui.getMinecraft();
        Entity vehicle = minecraft.player == null ? null : minecraft.player.getVehicle();
        boolean isMounted = vehicle != null && vehicle.showVehicleHealth();
        return !isMounted && !minecraft.options.hideGui && gui.shouldDrawSurvivalElements();
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientVitality.reset();
    }
}
