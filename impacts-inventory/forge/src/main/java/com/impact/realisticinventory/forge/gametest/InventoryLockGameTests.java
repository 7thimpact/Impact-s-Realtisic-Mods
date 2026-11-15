package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.inventory.InventoryLayout;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import java.util.Set;
import java.util.UUID;

/** Inventory milestone 3: locked slots, pockets and pickups. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryLockGameTests {
    private static final int LOCKED_INDEX = 20;
    private static final int LOCKED_HOTBAR_INDEX = 3;
    private static final int SOME_POCKET_INDEX = 12;
    private static final int GIVEN_COUNT = 3;
    private static final int FIRST_CHEST_SLOT = 0;
    private static final int UNLOCKED_SLOT_COUNT = 1 + InventoryLayout.POCKET_COUNT;

    private InventoryLockGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void pickupsGoToTheHandsThenThePockets(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            Inventory inventory = player.getInventory();
            inventory.add(new ItemStack(Items.DIRT, GIVEN_COUNT));
            helper.assertTrue(inventory.getItem(InventoryLayout.MAIN_HAND_INDEX).is(Items.DIRT), "The first item goes into the empty main hand");
            inventory.add(new ItemStack(Items.STONE, GIVEN_COUNT));
            helper.assertTrue(inventory.getItem(InventoryLayout.FIRST_POCKET_INDEX).is(Items.STONE), "The next item goes into the first pocket");
            inventory.add(new ItemStack(Items.DIRT, GIVEN_COUNT));
            helper.assertTrue(inventory.getItem(InventoryLayout.MAIN_HAND_INDEX).getCount() == GIVEN_COUNT * 2, "Matching items stack onto the hand");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void fullPocketsRefuseNewItems(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            fillWithSwords(player.getInventory());
            ItemStack stone = new ItemStack(Items.STONE, GIVEN_COUNT);
            helper.assertFalse(player.getInventory().add(stone), "A full inventory takes nothing more");
            helper.assertTrue(stone.getCount() == GIVEN_COUNT, "The refused stone keeps its count");
            helper.assertTrue(countLockedItems(player.getInventory()) == 0, "Nothing ends up in a locked slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void writesIntoLockedSlotsAreRedirected(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            Inventory inventory = player.getInventory();
            inventory.setItem(LOCKED_INDEX, new ItemStack(Items.APPLE, GIVEN_COUNT));
            helper.assertTrue(inventory.getItem(LOCKED_INDEX).isEmpty(), "setItem(20, ...) must not store the item there");
            helper.assertTrue(inventory.countItem(Items.APPLE) == GIVEN_COUNT, "The apples should land in a hand or a pocket");
            inventory.add(LOCKED_HOTBAR_INDEX, new ItemStack(Items.BREAD, GIVEN_COUNT));
            helper.assertTrue(inventory.getItem(LOCKED_HOTBAR_INDEX).isEmpty(), "add(3, ...) must not use the locked index");
            helper.assertTrue(inventory.countItem(Items.BREAD) == GIVEN_COUNT, "The bread should be placed normally");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void giveItemToPlayerDropsWhenPocketsAreFull(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            fillWithSwords(player.getInventory());
            Set<UUID> itemsAlreadyNearby = DroppedItems.idsNear(player);
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.APPLE, GIVEN_COUNT));
            helper.assertTrue(countLockedItems(player.getInventory()) == 0, "giveItemToPlayer must not use a locked slot");
            helper.assertTrue(DroppedItems.countNewNear(player, Items.APPLE, itemsAlreadyNearby) == GIVEN_COUNT, "The apples should drop at the player's feet");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void itemHandlerRefusesLockedSlots(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.getInventory());
            ItemStack apples = new ItemStack(Items.APPLE, GIVEN_COUNT);
            helper.assertTrue(wrapper.insertItem(LOCKED_INDEX, apples, false).getCount() == GIVEN_COUNT, "A locked index hands the stack back");
            helper.assertFalse(wrapper.isItemValid(LOCKED_INDEX, apples), "A locked index accepts nothing");
            helper.assertTrue(wrapper.insertItem(SOME_POCKET_INDEX, apples, false).isEmpty(), "A pocket takes the stack");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void chestScreenHidesLockedSlots(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            ChestMenu menu = ChestMenu.threeRows(1, player.getInventory());
            Slot lockedSlot = playerSlot(menu, player.getInventory(), LOCKED_INDEX);
            Slot pocketSlot = playerSlot(menu, player.getInventory(), SOME_POCKET_INDEX);
            helper.assertFalse(lockedSlot.isActive(), "Locked slots are hidden in a chest screen");
            helper.assertFalse(lockedSlot.mayPlace(new ItemStack(Items.APPLE)), "Locked slots refuse items");
            helper.assertTrue(pocketSlot.isActive(), "Pockets show in a chest screen");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void numberKeySwapIntoLockedSlotIsRefused(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            ChestMenu menu = ChestMenu.threeRows(1, player.getInventory());
            menu.getContainer().setItem(FIRST_CHEST_SLOT, new ItemStack(Items.DIAMOND));
            menu.clicked(FIRST_CHEST_SLOT, LOCKED_HOTBAR_INDEX, ClickType.SWAP, player);
            helper.assertTrue(menu.getContainer().getItem(FIRST_CHEST_SLOT).is(Items.DIAMOND), "Key 4 in a chest must not move the diamond");
            helper.assertTrue(player.getInventory().getItem(LOCKED_HOTBAR_INDEX).isEmpty(), "The locked index stays empty");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void shiftClickFillsPocketsBeforeTheHand(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            ChestMenu menu = ChestMenu.threeRows(1, player.getInventory());
            menu.getContainer().setItem(FIRST_CHEST_SLOT, new ItemStack(Items.DIAMOND));
            menu.quickMoveStack(player, FIRST_CHEST_SLOT);
            helper.assertTrue(player.getInventory().getItem(InventoryLayout.FIRST_POCKET_INDEX).is(Items.DIAMOND), "The diamond goes to the first pocket");
            helper.assertTrue(player.getInventory().getItem(InventoryLayout.MAIN_HAND_INDEX).isEmpty(), "The hand stays free");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void pickBlockFillsTheMainHand(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            Inventory inventory = player.getInventory();
            inventory.items.set(InventoryLayout.MAIN_HAND_INDEX, new ItemStack(Items.IRON_SWORD));
            inventory.items.set(SOME_POCKET_INDEX, new ItemStack(Items.TORCH, GIVEN_COUNT));
            inventory.pickSlot(SOME_POCKET_INDEX);
            helper.assertTrue(inventory.getItem(InventoryLayout.MAIN_HAND_INDEX).is(Items.TORCH), "The picked torches go into the main hand");
            helper.assertTrue(inventory.countItem(Items.IRON_SWORD) == 1, "The sword moves to a pocket");
            helper.assertTrue(countLockedItems(inventory) == 0, "Nothing ends up in a locked slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static void fillWithSwords(Inventory inventory) {
        for (int filled = 0; filled < UNLOCKED_SLOT_COUNT; filled++) {
            inventory.add(new ItemStack(Items.IRON_SWORD));
        }
    }

    private static int countLockedItems(Inventory inventory) {
        int lockedItems = 0;
        for (int inventoryIndex = 0; inventoryIndex < inventory.items.size(); inventoryIndex++) {
            lockedItems += InventoryLayout.isLocked(inventoryIndex) ? inventory.items.get(inventoryIndex).getCount() : 0;
        }
        return lockedItems;
    }

    private static Slot playerSlot(ChestMenu menu, Inventory inventory, int inventoryIndex) {
        return menu.slots.stream()
                .filter(slot -> slot.container == inventory && slot.getContainerSlot() == inventoryIndex)
                .findFirst()
                .orElseThrow();
    }
}
