package com.impact.realisticcrafting.forge.client;

import com.impact.realisticcrafting.client.RefusedCraftHint;
import com.impact.realisticcrafting.client.WoodTints;
import com.impact.realisticcrafting.menu.CraftingMenus;
import com.impact.realisticcrafting.recipe.Hammering;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Crafting's client side on Forge: the makeshift tables use vanilla's crafting screen, refused crafts name the table
 * needed, and per-wood parts are tinted by their wood.
 */
public final class CraftingForgeClient {
    private static final int WOODLESS_TINT = 0xFFFFFF;

    private CraftingForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CraftingForgeClient::onClientSetup);
        modEventBus.addListener(CraftingForgeClient::onItemColors);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeClient::onScreenRendered);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeClient::onRecipesUpdated);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.<CraftingMenu, CraftingScreen>register(CraftingMenus.tieredCrafting(), CraftingScreen::new));
    }

    // Per-wood parts tint their sprite (layer 0) by the wood.
    private static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 ? WoodTints.tintOf(stack) : WOODLESS_TINT, CraftingItems.trimmedPlanks());
    }

    // The client needs the hammering action for other mods' loose items too, to hold them.
    private static void onRecipesUpdated(RecipesUpdatedEvent event) {
        Hammering.registerHoldActions(event.getRecipeManager());
    }

    private static void onScreenRendered(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            RefusedCraftHint.render(event.getGuiGraphics(), screen.getMinecraft().font, screen.getMenu(), screen.getGuiLeft(), screen.getGuiTop(),
                    screen.getSlotUnderMouse(), event.getMouseX(), event.getMouseY());
        }
    }
}
