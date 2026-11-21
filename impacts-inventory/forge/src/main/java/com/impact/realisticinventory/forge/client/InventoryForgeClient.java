package com.impact.realisticinventory.forge.client;

import com.impact.lib.hud.HudLayout;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.client.ContainerPocketsLayout;
import com.impact.realisticinventory.client.HandsHotbarRenderer;
import com.impact.realisticinventory.client.InventoryKeys;
import com.impact.realisticinventory.client.PackItemColors;
import com.impact.realisticinventory.client.PackScreen;
import com.impact.realisticinventory.client.PlacedPackRenderer;
import com.impact.realisticinventory.client.RealisticInventoryScreen;
import com.impact.realisticinventory.client.WornPackModels;
import com.impact.realisticinventory.inventory.InventoryLocks;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackKind;
import com.impact.realisticinventory.pack.PackMenus;
import com.impact.realisticinventory.pack.PlacedPacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Inventory's client side on Forge: the two-slot hotbar and the inventory screen in place of vanilla's, the pack
 * screen, the open-pack key, dyed pack items and the pack on the player's back (drawn through Curios). Loaded only on
 * the client.
 */
public final class InventoryForgeClient {
    private InventoryForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(InventoryForgeClient::onClientSetup);
        modEventBus.addListener(InventoryForgeClient::onRegisterKeyMappings);
        modEventBus.addListener(InventoryForgeClient::onRegisterItemColors);
        modEventBus.addListener(InventoryForgeClient::onRegisterLayerDefinitions);
        modEventBus.addListener(InventoryForgeClient::onRegisterRenderers);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeClient::onRenderOverlay);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeClient::onScreenOpening);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeClient::onScreenInit);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeClient::onContainerBackground);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeClient::onClientTick);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        HudLayout.publishHotbarBounds(InventoryMod.MOD_ID, HandsHotbarRenderer::bounds);
        event.enqueueWork(() -> MenuScreens.register(PackMenus.packMenuType(), PackScreen::new));
        PackCurioRenderer packRenderer = new PackCurioRenderer();
        InventoryItems.packs().forEach(pack -> CuriosRendererRegistry.register(pack, () -> packRenderer));
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(InventoryKeys.OPEN_PACK);
    }

    private static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(PackItemColors.DYED_FIRST_LAYER, InventoryItems.packs().toArray(Item[]::new));
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (PackKind kind : PackKind.values()) {
            event.registerLayerDefinition(WornPackModels.layerFor(kind), () -> WornPackModels.definitionFor(kind));
        }
    }


    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PlacedPacks.blockEntityType(), PlacedPackRenderer::new);
    }

    /** Replaces vanilla's hotbar (and its separate off-hand slot) with the hands while the locks apply. */
    private static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        boolean isHotbar = event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id());
        if (!isHotbar || !InventoryLocks.isActiveFor(Minecraft.getInstance().player)) {
            return;
        }
        event.setCanceled(true);
        HandsHotbarRenderer.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
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

    /** Other containers' screens show the pockets as a 3 by 3 grid (creative mode's screen keeps vanilla's layout). */
    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && !(screen instanceof CreativeModeInventoryScreen)) {
            ContainerPocketsLayout.applyTo(screen.getMenu(), Minecraft.getInstance().player);
        }
    }

    private static void onContainerBackground(ContainerScreenEvent.Render.Background event) {
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        if (screen instanceof CreativeModeInventoryScreen) {
            return;
        }
        ContainerPocketsLayout.applyTo(screen.getMenu(), Minecraft.getInstance().player)
                .ifPresent(layout -> layout.drawBackground(event.getGuiGraphics(), screen.getGuiLeft(), screen.getGuiTop()));
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
            InventoryKeys.onClientTick();
        }
    }
}
