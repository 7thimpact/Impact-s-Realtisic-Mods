package com.impact.realisticcrafting.forge.client;

import com.impact.realisticcrafting.client.RefusedCraftHint;
import com.impact.realisticcrafting.menu.CraftingMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Crafting's client side on Forge: the makeshift tables use vanilla's crafting screen, and refused crafts name the table needed. */
public final class CraftingForgeClient {
    private CraftingForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CraftingForgeClient::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeClient::onScreenRendered);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.<CraftingMenu, CraftingScreen>register(CraftingMenus.tieredCrafting(), CraftingScreen::new));
    }

    private static void onScreenRendered(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            RefusedCraftHint.render(event.getGuiGraphics(), screen.getMinecraft().font, screen.getMenu(), screen.getGuiLeft(), screen.getGuiTop(),
                    screen.getSlotUnderMouse(), event.getMouseX(), event.getMouseY());
        }
    }
}
