package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.pack.BackMenuSlot;
import com.impact.realisticinventory.pack.BackSlotContents;
import com.impact.realisticinventory.pack.BackSlotStorage;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

import java.util.Set;
import java.util.UUID;

/** Inventory milestone 4: the back slot appended to vanilla's inventory menu, and Lib's back-slot provider. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryBackSlotGameTests {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int STONE_COUNT = 5;
    private static final String WORN_ITEM_NAME = "Test pack";

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
            helper.assertTrue(BackSlot.set(player, new ItemStack(Items.SADDLE)), "Setting through Lib works when a provider exists");
            helper.assertTrue(BackSlotStorage.get(player).is(Items.SADDLE), "Lib's write lands in Inventory's back slot");
            helper.assertTrue(BackSlot.get(player).is(Items.SADDLE), "Lib reads the same item back");
            helper.assertTrue(player.inventoryMenu.getSlot(InventoryLayout.MENU_BACK_SLOT).getItem().is(Items.SADDLE),
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
            BackSlotStorage.set(player, new ItemStack(Items.SADDLE));
            Set<UUID> itemsAlreadyNearby = DroppedItems.idsNear(player);
            player.getInventory().dropAll();
            helper.assertTrue(BackSlotStorage.get(player).isEmpty(), "The back slot empties on death");
            helper.assertTrue(DroppedItems.countNewNear(player, Items.SADDLE, itemsAlreadyNearby) == 1, "The worn item drops at the player's feet");
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
            BackSlotStorage.set(player, new ItemStack(Items.SADDLE));
            player.connection.handleSetCreativeModeSlot(new ServerboundSetCreativeModeSlotPacket(InventoryLayout.MENU_BACK_SLOT, ItemStack.EMPTY));
            helper.assertTrue(BackSlotStorage.get(player).isEmpty(), "Creative mode can take the worn item off");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void wornItemIsSavedWithThePlayer(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            ItemStack namedSaddle = new ItemStack(Items.SADDLE);
            namedSaddle.setHoverName(Component.literal(WORN_ITEM_NAME));
            BackSlotStorage.set(player, namedSaddle);
            CompoundTag playerTag = player.saveWithoutId(new CompoundTag());
            CompoundTag savedBackSlot = playerTag.getCompound(PlayerAttachments.NBT_KEY).getCompound(BackSlotStorage.BACK_SLOT.id().toString());
            BackSlotContents loaded = new BackSlotContents();
            loaded.load(savedBackSlot);
            helper.assertTrue(ItemStack.matches(loaded.stack(), namedSaddle), "The worn item, name included, survives saving and loading");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
