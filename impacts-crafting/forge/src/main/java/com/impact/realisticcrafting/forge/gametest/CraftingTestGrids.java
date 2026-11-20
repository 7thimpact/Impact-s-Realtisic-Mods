package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.menu.TieredCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.BiConsumer;

/** Shared steps for Crafting's GameTests: a mock player at a placed Makeshift Table, and taking a craft from a grid. */
final class CraftingTestGrids {
    static final int RESULT_SLOT = 0;
    private static final int TEST_CONTAINER_ID = 1;

    private CraftingTestGrids() {
    }

    /** Runs the check with a mock player at a Makeshift Table placed in the test area, then cleans both up. */
    static void atTable(GameTestHelper helper, MakeshiftTable table, BiConsumer<ServerPlayer, TieredCraftingMenu> check) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(position, table.block().defaultBlockState());
        try {
            TieredCraftingMenu menu = new TieredCraftingMenu(TEST_CONTAINER_ID, player.getInventory(),
                    ContainerLevelAccess.create(helper.getLevel(), position), table);
            check.accept(player, menu);
            menu.grid().clearContent();
        } finally {
            helper.getLevel().setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            MockPlayers.leaveWorld(player);
        }
    }

    /** Fills the menu's grid from slot 0 on (empty stacks skip a slot), takes one craft and returns it. */
    static ItemStack craft(AbstractContainerMenu menu, ServerPlayer player, ItemStack... gridStacks) {
        for (int slot = 0; slot < gridStacks.length; slot++) {
            menu.getSlot(RESULT_SLOT + 1 + slot).set(gridStacks[slot]);
        }
        menu.clicked(RESULT_SLOT, 0, ClickType.PICKUP, player);
        ItemStack taken = menu.getCarried();
        menu.setCarried(ItemStack.EMPTY);
        return taken;
    }
}
