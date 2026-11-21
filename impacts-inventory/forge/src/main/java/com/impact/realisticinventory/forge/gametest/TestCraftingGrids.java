package com.impact.realisticinventory.forge.gametest;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Crafting grids built by hand, for checking recipes without a player or a screen. */
final class TestCraftingGrids {
    private static final int GRID_SIZE = 3;

    private TestCraftingGrids() {
    }

    /** A 3×3 grid holding the stacks in order, row by row. */
    static CraftingContainer holding(List<ItemStack> layout) {
        CraftingContainer grid = new TransientCraftingContainer(new DisabledMenu(), GRID_SIZE, GRID_SIZE);
        for (int gridSlot = 0; gridSlot < layout.size(); gridSlot++) {
            grid.setItem(gridSlot, layout.get(gridSlot));
        }
        return grid;
    }

    /** A menu that's never shown. */
    private static final class DisabledMenu extends AbstractContainerMenu {
        DisabledMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }
}
