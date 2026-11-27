package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackContents;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornSlots;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Inventory milestone 9: every way Salty's Realistic Forging touches the player's inventory (see docs/srf-notes.md).
 * Each test repeats the same vanilla or Forge call SRF makes, so it runs with or without SRF installed.
 */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventorySrfGameTests {
    private static final int GIVEN_COUNT = 3;
    private static final int CLEAR_ALL = -1;

    private InventorySrfGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void srfGivesLandInPocketsPackOrFeet(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            fillHandsAndPocketsWith(player.getInventory(), Items.IRON_SWORD);
            Set<UUID> itemsAlreadyNearby = DroppedItems.idsNear(player);
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.RAW_IRON, GIVEN_COUNT));
            helper.assertTrue(DroppedItems.countNewNear(player, Items.RAW_IRON, itemsAlreadyNearby) == GIVEN_COUNT,
                    "With full pockets and no pack, SRF's give drops at the player's feet");
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.satchel()));
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.RAW_IRON, GIVEN_COUNT));
            helper.assertTrue(PackContents.stacksIn(WornSlots.get(player, WornOn.BACK)).stream().anyMatch(stack -> stack.is(Items.RAW_IRON)),
                    "With a worn pack, SRF's give lands in the pack");
            helper.assertTrue(countLockedStacks(player.getInventory()) == 0, "Nothing ever lands in a locked slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void srfHandWritesReachTheHands(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            for (int swaps = 0; swaps < 2; swaps++) {
                HandSwitch.swapHands(player);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
                player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
                helper.assertTrue(player.getInventory().getItem(InventoryLayout.MAIN_HAND_INDEX).is(Items.IRON_INGOT),
                        "setItemInHand(MAIN_HAND) writes index 0, after a swap too");
                helper.assertTrue(player.getInventory().offhand.get(0).is(Items.STICK), "setItemInHand(OFF_HAND) writes the off-hand slot");
                helper.assertTrue(player.getInventory().selected == InventoryLayout.MAIN_HAND_INDEX, "The selected index stays 0");
            }
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void srfClearingMatchingItemsReachesHandsAndPockets(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            Inventory inventory = player.getInventory();
            inventory.setItem(InventoryLayout.MAIN_HAND_INDEX, new ItemStack(Items.COAL, GIVEN_COUNT));
            inventory.setItem(InventoryLayout.FIRST_POCKET_INDEX, new ItemStack(Items.COAL, GIVEN_COUNT));
            inventory.offhand.set(0, new ItemStack(Items.COAL, GIVEN_COUNT));
            int cleared = inventory.clearOrCountMatchingItems(stack -> stack.is(Items.COAL), CLEAR_ALL, player.inventoryMenu.getCraftSlots());
            helper.assertTrue(cleared == GIVEN_COUNT * 3, "All the coal in the hands and pockets is counted and cleared");
            helper.assertTrue(inventory.countItem(Items.COAL) == 0, "No coal is left");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void srfPickupConversionLeavesLockedSlotsEmpty(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            Inventory inventory = player.getInventory();
            inventory.add(new ItemStack(Items.RAW_IRON, GIVEN_COUNT));
            // SRF's pickup handler removes the vanilla ore and gives its own item in its place.
            inventory.clearOrCountMatchingItems(stack -> stack.is(Items.RAW_IRON), CLEAR_ALL, player.inventoryMenu.getCraftSlots());
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.IRON_NUGGET, GIVEN_COUNT));
            helper.assertTrue(inventory.countItem(Items.RAW_IRON) == 0, "The vanilla ore is gone");
            helper.assertTrue(inventory.countItem(Items.IRON_NUGGET) == GIVEN_COUNT, "The converted item is carried");
            helper.assertTrue(countLockedStacks(inventory) == 0, "Nothing lands in a locked slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static void fillHandsAndPocketsWith(Inventory inventory, Item item) {
        inventory.setItem(InventoryLayout.MAIN_HAND_INDEX, new ItemStack(item));
        inventory.offhand.set(0, new ItemStack(item));
        IntStream.rangeClosed(InventoryLayout.FIRST_POCKET_INDEX, InventoryLayout.LAST_POCKET_INDEX)
                .forEach(pocketIndex -> inventory.setItem(pocketIndex, new ItemStack(item)));
    }

    private static long countLockedStacks(Inventory inventory) {
        return IntStream.range(0, inventory.items.size())
                .filter(InventoryLayout::isLocked)
                .filter(inventoryIndex -> !inventory.items.get(inventoryIndex).isEmpty())
                .count();
    }
}
