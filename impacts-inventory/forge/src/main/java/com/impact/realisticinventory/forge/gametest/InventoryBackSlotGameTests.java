package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.backslot.BackSlot;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.forge.curios.CuriosBackSlot;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.pack.BackMenuSlot;
import com.impact.realisticinventory.pack.BackSlotStorage;
import com.impact.realisticinventory.pack.InventoryItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Set;
import java.util.UUID;

/** Inventory milestone 4: the back slot (Curios's back slot) appended to vanilla's inventory menu, and Lib's back-slot provider. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryBackSlotGameTests {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int STONE_COUNT = 5;

    private InventoryBackSlotGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void backSlotIsAppendedAfterVanillaSlots(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            InventoryMenu menu = player.inventoryMenu;
            helper.assertTrue(menu.slots.size() == InventoryLayout.MENU_BACK_SLOT + 1, "The menu should have vanilla's 46 slots plus the back slot");
            helper.assertTrue(menu.slots.get(InventoryLayout.MENU_BACK_SLOT) instanceof BackMenuSlot, "Slot 46 should be the back slot");
            helper.assertTrue(menu.slots.get(InventoryMenu.SHIELD_SLOT).getContainerSlot() == Inventory.SLOT_OFFHAND,
                    "Vanilla's off-hand slot keeps its index");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void libReachesTheBackSlotThroughInventory(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            helper.assertTrue(BackSlot.isAvailable(), "Inventory provides Lib's back slot");
            helper.assertFalse(BackSlot.accepts(new ItemStack(Items.STONE)), "Stone doesn't fit the back slot");
            helper.assertTrue(BackSlot.set(player, new ItemStack(InventoryItems.satchel())), "Setting through Lib works when a provider exists");
            helper.assertTrue(BackSlotStorage.get(player).is(InventoryItems.satchel()), "Lib's write lands in Inventory's back slot");
            helper.assertTrue(BackSlot.get(player).is(InventoryItems.satchel()), "Lib reads the same item back");
            helper.assertTrue(player.inventoryMenu.getSlot(InventoryLayout.MENU_BACK_SLOT).getItem().is(InventoryItems.satchel()),
                    "The menu's back slot shows the worn item");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void backSlotRefusesItemsThatDontFit(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            InventoryMenu menu = player.inventoryMenu;
            player.getInventory().setItem(InventoryLayout.MAIN_HAND_INDEX, new ItemStack(Items.STONE, STONE_COUNT));
            menu.clicked(InventoryLayout.MENU_MAIN_HAND_SLOT, LEFT_MOUSE_BUTTON, ClickType.PICKUP, player);
            menu.clicked(InventoryLayout.MENU_BACK_SLOT, LEFT_MOUSE_BUTTON, ClickType.PICKUP, player);
            helper.assertTrue(BackSlotStorage.get(player).isEmpty(), "Clicking stone into the back slot is refused");
            helper.assertTrue(menu.getCarried().getCount() == STONE_COUNT, "The refused stone stays on the cursor");
            menu.clicked(InventoryLayout.MENU_MAIN_HAND_SLOT, LEFT_MOUSE_BUTTON, ClickType.PICKUP, player);
            menu.clicked(InventoryLayout.MENU_MAIN_HAND_SLOT, LEFT_MOUSE_BUTTON, ClickType.QUICK_MOVE, player);
            helper.assertTrue(BackSlotStorage.get(player).isEmpty(), "Shift-clicking stone never puts it on the back");
            helper.assertTrue(player.getInventory().getItem(InventoryLayout.FIRST_POCKET_INDEX).getCount() == STONE_COUNT,
                    "Shift-clicking the main hand moves the stone into a pocket");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void wornItemDropsOnDeath(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            BackSlotStorage.set(player, new ItemStack(InventoryItems.satchel()));
            Set<UUID> itemsAlreadyNearby = DroppedItems.idsNear(player);
            player.kill();
            helper.assertTrue(BackSlotStorage.get(player).isEmpty(), "The back slot empties on death");
            helper.assertTrue(DroppedItems.countNewNear(player, InventoryItems.satchel(), itemsAlreadyNearby) == 1,
                    "Curios drops the worn pack at the player's feet");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void creativeEditsFollowTheBackSlotRules(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            player.setGameMode(GameType.CREATIVE);
            player.connection.handleSetCreativeModeSlot(new ServerboundSetCreativeModeSlotPacket(InventoryLayout.MENU_BACK_SLOT, new ItemStack(Items.STONE)));
            helper.assertTrue(BackSlotStorage.get(player).isEmpty(), "Creative mode can't put stone on the back either");
            BackSlotStorage.set(player, new ItemStack(InventoryItems.satchel()));
            player.connection.handleSetCreativeModeSlot(new ServerboundSetCreativeModeSlotPacket(InventoryLayout.MENU_BACK_SLOT, ItemStack.EMPTY));
            helper.assertTrue(BackSlotStorage.get(player).isEmpty(), "Creative mode can take the worn item off");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theBackSlotIsCuriosBackSlot(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            helper.assertTrue(BackSlotStorage.hasBackSlot(player), "Inventory's data gives every player Curios's back slot");
            BackSlotStorage.set(player, new ItemStack(InventoryItems.satchel()));
            ItemStack inCurios = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.getStacksHandler(CuriosBackSlot.SLOT_TYPE))
                    .map(handler -> handler.getStacks().getStackInSlot(0))
                    .orElse(ItemStack.EMPTY);
            helper.assertTrue(inCurios.is(InventoryItems.satchel()), "The worn satchel is in Curios's back slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
