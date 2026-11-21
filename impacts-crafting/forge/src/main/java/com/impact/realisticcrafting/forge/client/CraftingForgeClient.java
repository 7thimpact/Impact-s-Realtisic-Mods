package com.impact.realisticcrafting.forge.client;

import com.impact.lib.client.DrawsOwnCraftingProgress;
import com.impact.lib.crafting.CraftingProgress;
import com.impact.realisticcrafting.client.CraftingArrows;
import com.impact.realisticcrafting.client.DyeVatColors;
import com.impact.realisticcrafting.client.RefusedCraftHint;
import com.impact.realisticcrafting.client.WoodTints;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.menu.CraftingMenus;
import com.impact.realisticcrafting.recipe.Hammering;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;

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
        modEventBus.addListener(CraftingForgeClient::onBlockColors);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeClient::onScreenRendered);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeClient::onRecipesUpdated);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeClient::onContainerBackground);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeClient::onContainerForeground);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.<CraftingMenu, CraftingScreen>register(CraftingMenus.tieredCrafting(), CraftingScreen::new));
    }

    // Per-wood parts tint their sprite (layer 0) by the wood.
    private static void onItemColors(RegisterColorHandlersEvent.Item event) {
        Item[] woodParts = Arrays.stream(WoodPart.values()).map(CraftingItems::woodPart).toArray(Item[]::new);
        event.register((stack, layer) -> layer == 0 ? WoodTints.tintOf(stack) : WOODLESS_TINT, woodParts);
    }

    // The client needs the hammering action for other mods' loose items too, to hold them.
    private static void onRecipesUpdated(RecipesUpdatedEvent event) {
        Hammering.registerHoldActions(event.getRecipeManager());
    }

    // The arrow sits in the screen's background, under the slots; screens that draw it themselves (Inventory's) are skipped.
    private static void onContainerBackground(ContainerScreenEvent.Render.Background event) {
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        if (screen instanceof DrawsOwnCraftingProgress) {
            return;
        }
        CraftingProgress.get(screen.getMenu()).ifPresent(progress ->
                CraftingArrows.renderArrow(event.getGuiGraphics(), screen, screen.getGuiLeft(), screen.getGuiTop(), progress));
    }

    // The faded cover goes over the output's item, which is drawn after the background.
    private static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        if (screen instanceof DrawsOwnCraftingProgress) {
            return;
        }
        CraftingProgress.get(screen.getMenu()).ifPresent(progress -> CraftingArrows.renderFadedOutput(event.getGuiGraphics(), screen, progress));
    }

    // The vat's water takes its dye color.
    private static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, position, tintIndex) -> level == null || position == null ? DyeVatColors.PLAIN_WATER
                : DyeVatColors.colorAt(level.getBlockEntity(position)), CraftingBlocks.dyeVat());
    }

    private static void onScreenRendered(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            RefusedCraftHint.render(event.getGuiGraphics(), screen.getMinecraft().font, screen.getMenu(), screen.getGuiLeft(), screen.getGuiTop(),
                    screen.getSlotUnderMouse(), event.getMouseX(), event.getMouseY());
        }
    }
}
