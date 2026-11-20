package com.impact.realisticcrafting.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/** A makeshift table's result slot: vanilla's, plus one use off the table for each craft taken. */
final class TableResultSlot extends ResultSlot {
    private static final int RESULT_CONTAINER_SLOT = 0;

    private final TieredCraftingMenu menu;

    TableResultSlot(Player player, CraftingContainer grid, Container resultContainer, int x, int y, TieredCraftingMenu menu) {
        super(player, grid, resultContainer, RESULT_CONTAINER_SLOT, x, y);
        this.menu = menu;
    }

    @Override
    public void onTake(Player player, ItemStack taken) {
        super.onTake(player, taken);
        menu.onCraftTaken(player);
    }
}
