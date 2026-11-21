package com.impact.realisticcrafting.client;

import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.menu.TableTierCheck;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Over a crafting grid whose station refuses the recipe in it: the empty result slot is tinted red, and hovering it
 * names the station that's needed. The client works this out from its own recipes, tags and the synced config.
 */
public final class RefusedCraftHint {
    private static final int RESULT_SLOT = 0;
    private static final int FIRST_GRID_SLOT = 1;
    private static final int SLOT_SIZE = 16;
    private static final int REFUSED_TINT = 0x80C02020;

    private static AbstractContainerMenu lastMenu;
    private static List<ItemStack> lastGrid = List.of();
    private static Optional<CraftingStation> lastNeededStation = Optional.empty();

    private RefusedCraftHint() {
    }

    public static void render(GuiGraphics graphics, Font font, AbstractContainerMenu menu, int guiLeft, int guiTop, Slot hoveredSlot,
                              int mouseX, int mouseY) {
        Optional<CraftingStation> neededStation = neededStation(menu);
        if (neededStation.isEmpty()) {
            return;
        }
        Slot resultSlot = menu.getSlot(RESULT_SLOT);
        int left = guiLeft + resultSlot.x;
        int top = guiTop + resultSlot.y;
        graphics.fill(left, top, left + SLOT_SIZE, top + SLOT_SIZE, REFUSED_TINT);
        if (hoveredSlot == resultSlot) {
            graphics.renderTooltip(font, Component.translatable(neededStation.get().needsTranslationKey()).withStyle(ChatFormatting.RED),
                    mouseX, mouseY);
        }
    }

    private static Optional<CraftingStation> neededStation(AbstractContainerMenu menu) {
        Level level = Minecraft.getInstance().level;
        boolean hasCraftingGrid = menu.slots.size() > FIRST_GRID_SLOT && menu.getSlot(RESULT_SLOT) instanceof ResultSlot
                && menu.getSlot(FIRST_GRID_SLOT).container instanceof CraftingContainer;
        if (level == null || !hasCraftingGrid || menu.getSlot(RESULT_SLOT).hasItem() || CraftingStation.of(menu) == CraftingStation.CRAFTING_TABLE) {
            return Optional.empty();
        }
        CraftingContainer grid = (CraftingContainer) menu.getSlot(FIRST_GRID_SLOT).container;
        if (menu != lastMenu || !isSameGrid(grid)) {
            lastMenu = menu;
            lastGrid = copyOf(grid);
            lastNeededStation = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level)
                    .flatMap(recipe -> TableTierCheck.neededStation(menu, recipe));
        }
        return lastNeededStation;
    }

    private static boolean isSameGrid(CraftingContainer grid) {
        if (lastGrid.size() != grid.getContainerSize()) {
            return false;
        }
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            if (!ItemStack.matches(lastGrid.get(slot), grid.getItem(slot))) {
                return false;
            }
        }
        return true;
    }

    private static List<ItemStack> copyOf(CraftingContainer grid) {
        List<ItemStack> copy = new ArrayList<>(grid.getContainerSize());
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            copy.add(grid.getItem(slot).copy());
        }
        return copy;
    }
}
