package com.impact.realisticinventory.forge.client;

import com.impact.lib.hud.HudLayout;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.client.RealisticInventoryScreen;
import com.impact.realisticinventory.client.TwoSlotHotbarRenderer;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Inventory's client side on Forge: the two-slot hotbar and the inventory screen in place of vanilla's. Loaded only on the client. */
public final class InventoryForgeClient {
    private InventoryForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(InventoryForgeClient::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeClient::onRenderOverlay);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeClient::onScreenOpening);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        HudLayout.publishHotbarBounds(InventoryMod.MOD_ID, TwoSlotHotbarRenderer::bounds);
    }

    /** Replaces vanilla's hotbar (and its separate off-hand slot) while the two-slot hotbar is in use. */
    private static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        boolean isHotbar = event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id());
        if (!isHotbar || !InventoryLocks.isActiveFor(Minecraft.getInstance().player)) {
            return;
        }
        event.setCanceled(true);
        TwoSlotHotbarRenderer.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }

    /** Vanilla's inventory screen becomes Inventory's while the locks apply (not in creative with creativeVanilla on). */
    private static void onScreenOpening(ScreenEvent.Opening event) {
        Screen opening = event.getNewScreen();
        LocalPlayer player = Minecraft.getInstance().player;
        boolean isVanillaInventoryScreen = opening != null && opening.getClass() == InventoryScreen.class;
        if (!isVanillaInventoryScreen || !InventoryLocks.isActiveFor(player)) {
            return;
        }
        event.setNewScreen(new RealisticInventoryScreen(player));
    }
}
