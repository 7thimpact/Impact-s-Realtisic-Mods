package com.impact.realisticvitality.forge.client;

import com.impact.realisticvitality.client.ClientVitality;
import com.impact.realisticvitality.client.FrostOverlay;
import com.impact.realisticvitality.client.LiningTooltips;
import com.impact.realisticvitality.client.TemperatureGauge;
import com.impact.realisticvitality.client.ThirstHud;
import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.network.VitalityNetwork;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

/** Vitality's client-only hooks on Forge. Loaded only on the physical client. */
public final class VitalityForgeClient {
    private static final String THIRST_OVERLAY = "thirst_level";
    private static final String GAUGE_OVERLAY = "temperature_gauge";
    private static final String FROST_OVERLAY = "temperature_frost";
    private static final int LIQUID_LAYER = 0;
    private static final int NO_TINT = -1;
    private static final KeyMapping SIP_PACK_KEY = new KeyMapping(VitalityTranslations.SIP_PACK_KEY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, VitalityTranslations.KEY_CATEGORY);

    private VitalityForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(VitalityForgeClient::onRegisterOverlays);
        modEventBus.addListener(VitalityForgeClient::onRegisterItemColors);
        modEventBus.addListener(VitalityForgeClient::onRegisterKeys);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeClient::onLoggingOut);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeClient::onItemTooltip);
    }

    // Right after hunger, so thirst sits directly above it and vanilla's air bubbles stack above thirst.
    private static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), THIRST_OVERLAY, (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            if (showsFoodRow(gui)) {
                gui.setupOverlayRenderState(true, false);
                ThirstHud.render(graphics, screenWidth, screenHeight, gui.getGuiTicks());
            }
        });
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), GAUGE_OVERLAY, (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            if (!gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
                gui.setupOverlayRenderState(true, false);
                TemperatureGauge.render(graphics, screenWidth, screenHeight, gui.getGuiTicks(), partialTick);
            }
        });
        event.registerAbove(VanillaGuiOverlay.FROSTBITE.id(), FROST_OVERLAY,
                (gui, graphics, partialTick, screenWidth, screenHeight) -> FrostOverlay.render(graphics, screenWidth, screenHeight));
    }

    /** The same test Forge uses for the hunger row: hidden in creative, with the HUD off, and on a mount that shows its health. */
    private static boolean showsFoodRow(ForgeGui gui) {
        Minecraft minecraft = gui.getMinecraft();
        Entity vehicle = minecraft.player == null ? null : minecraft.player.getVehicle();
        boolean isMounted = vehicle != null && vehicle.showVehicleHealth();
        return !isMounted && !minecraft.options.hideGui && gui.shouldDrawSurvivalElements();
    }

    // The liquid layer (layer 0, vanilla's potion overlay) takes each water's color; the glass stays untinted.
    private static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        for (BottledWater water : BottledWater.values()) {
            event.register((stack, layer) -> layer == LIQUID_LAYER ? water.liquidColor : NO_TINT, VitalityItems.waterBottle(water));
        }
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(SIP_PACK_KEY);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        while (SIP_PACK_KEY.consumeClick()) {
            VitalityNetwork.sendPackSip();
        }
    }

    private static void onItemTooltip(ItemTooltipEvent event) {
        LiningTooltips.addLines(event.getItemStack(), event.getToolTip());
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientVitality.reset();
    }
}
