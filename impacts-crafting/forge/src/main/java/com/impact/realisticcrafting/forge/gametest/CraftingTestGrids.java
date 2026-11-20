package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.menu.TieredCraftingMenu;
import com.impact.realisticcrafting.timing.CraftTimers;
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
    private static final int LONGEST_CRAFT_TICKS = 400;

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
            player.containerMenu = menu;
            check.accept(player, menu);
            menu.grid().clearContent();
        } finally {
            player.containerMenu = player.inventoryMenu;
            helper.getLevel().setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            MockPlayers.leaveWorld(player);
        }
    }

    /** Fills the menu's grid from slot 0 on (empty stacks skip a slot), takes one craft and returns it. */
    static ItemStack craft(AbstractContainerMenu menu, ServerPlayer player, ItemStack... gridStacks) {
        for (int slot = 0; slot < gridStacks.length; slot++) {
            menu.getSlot(RESULT_SLOT + 1 + slot).set(gridStacks[slot]);
        }
        finishCraft(menu, player);
        menu.clicked(RESULT_SLOT, 0, ClickType.PICKUP, player);
        ItemStack taken = menu.getCarried();
        menu.setCarried(ItemStack.EMPTY);
        return taken;
    }

    /** Runs the menu's craft timer to the end, as the server's player ticks would (a mock player doesn't tick). */
    static void finishCraft(AbstractContainerMenu menu, ServerPlayer player) {
        AbstractContainerMenu openBefore = player.containerMenu;
        player.containerMenu = menu;
        for (int tick = 0; tick < LONGEST_CRAFT_TICKS && CraftTimers.isOutputLocked(menu); tick++) {
            CraftTimers.tick(player);
        }
        player.containerMenu = openBefore;
    }

    /** Runs the open menu's timers for a number of ticks. */
    static void runTicks(ServerPlayer player, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            CraftTimers.tick(player);
        }
    }
}
