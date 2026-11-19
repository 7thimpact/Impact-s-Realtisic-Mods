package com.impact.lib.gametest;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

/** 3x3 crafting grids for GameTests, filled slot by slot from the top left. */
public final class CraftingGrids {
    private static final int GRID_SIZE = 3;

    private CraftingGrids() {
    }

    /** A grid holding the stacks in its first slots; pass {@link ItemStack#EMPTY} to skip a slot. */
    public static CraftingContainer of(ItemStack... stacks) {
        NonNullList<ItemStack> items = NonNullList.withSize(GRID_SIZE * GRID_SIZE, ItemStack.EMPTY);
        for (int slot = 0; slot < stacks.length; slot++) {
            items.set(slot, stacks[slot]);
        }
        return new TransientCraftingContainer(new UnshownMenu(), GRID_SIZE, GRID_SIZE, items);
    }

    /** A menu nobody sees, which vanilla's crafting container needs to exist. */
    private static final class UnshownMenu extends AbstractContainerMenu {
        UnshownMenu() {
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
