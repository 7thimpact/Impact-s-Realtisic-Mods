package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Inventory milestone 2: the two-slot hotbar. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryHotbarGameTests {
    private static final int SOME_LOCKED_HOTBAR_INDEX = 5;

    private InventoryHotbarGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void swappingTradesTheTwoHands(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            player.getInventory().items.set(InventoryLayout.MAIN_HAND_INDEX, Items.IRON_SWORD.getDefaultInstance());
            player.getInventory().offhand.set(0, Items.IRON_PICKAXE.getDefaultInstance());
            HandSwitch.swapHands(player);
            helper.assertTrue(player.getMainHandItem().is(Items.IRON_PICKAXE), "The off hand's pickaxe moves to the main hand");
            helper.assertTrue(player.getOffhandItem().is(Items.IRON_SWORD), "and the sword to the off hand, with no copy left behind");
            helper.assertTrue(player.getInventory().countItem(Items.IRON_SWORD) == 1 && player.getInventory().countItem(Items.IRON_PICKAXE) == 1,
                    "Each item is in exactly one hand");
            HandSwitch.swapHands(player);
            helper.assertTrue(player.getMainHandItem().is(Items.IRON_SWORD), "Swapping again brings the sword back");
            helper.assertTrue(HandSwitch.stateOf(player).hasEverSwitched(), "The swap is recorded for the achievements");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void emptyHandsStayEmpty(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            HandSwitch.swapHands(player);
            helper.assertTrue(player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty(), "Two empty slots are two free hands");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void selectedIndexStaysOnTheMainHand(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            player.getInventory().selected = SOME_LOCKED_HOTBAR_INDEX;
            player.getInventory().tick();
            helper.assertTrue(player.getInventory().selected == InventoryLayout.MAIN_HAND_INDEX, "Other code's selection change should be undone");
            player.connection.handleSetCarriedItem(new ServerboundSetCarriedItemPacket(SOME_LOCKED_HOTBAR_INDEX));
            helper.assertTrue(player.getInventory().selected == InventoryLayout.MAIN_HAND_INDEX, "A client's hotbar selection should be refused");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void creativeKeepsVanillaHotbar(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setGameMode(GameType.CREATIVE);
            helper.assertFalse(InventoryLocks.isActiveFor(player), "Creative mode keeps vanilla's inventory by default");
            player.getInventory().selected = SOME_LOCKED_HOTBAR_INDEX;
            player.getInventory().tick();
            helper.assertTrue(player.getInventory().selected == SOME_LOCKED_HOTBAR_INDEX, "Creative players may select any hotbar slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
