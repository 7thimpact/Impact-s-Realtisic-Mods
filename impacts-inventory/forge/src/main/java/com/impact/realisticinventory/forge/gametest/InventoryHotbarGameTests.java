package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.hotbar.HotbarState;
import com.impact.realisticinventory.hotbar.SwitchSource;
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
    public static void switchingSwapsHandsAndKeepsItemsInTheirSlots(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            player.getInventory().items.set(InventoryLayout.MAIN_HAND_INDEX, Items.IRON_SWORD.getDefaultInstance());
            player.getInventory().offhand.set(0, Items.IRON_PICKAXE.getDefaultInstance());
            HandSwitch.selectSlot(player, HotbarState.SECOND_SLOT, SwitchSource.NUMBER_KEY);
            helper.assertTrue(player.getMainHandItem().is(Items.IRON_PICKAXE), "Slot 2's pickaxe should move to the main hand");
            helper.assertTrue(player.getOffhandItem().is(Items.IRON_SWORD), "Slot 1's sword should move to the off hand");
            helper.assertTrue(HandSwitch.stateOf(player).activeSlot() == HotbarState.SECOND_SLOT, "Slot 2 should be active");
            HandSwitch.selectSlot(player, HotbarState.SECOND_SLOT, SwitchSource.NUMBER_KEY);
            helper.assertTrue(player.getMainHandItem().is(Items.IRON_PICKAXE), "Selecting the active slot again should change nothing");
            HandSwitch.switchToOtherSlot(player, SwitchSource.SCROLL_WHEEL);
            helper.assertTrue(player.getMainHandItem().is(Items.IRON_SWORD), "Scrolling should switch back to the sword");
            helper.assertTrue(HandSwitch.stateOf(player).activeSlot() == HotbarState.FIRST_SLOT, "Slot 1 should be active again");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void emptyHandsStayEmpty(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            HandSwitch.switchToOtherSlot(player, SwitchSource.SWAP_HANDS_KEY);
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
