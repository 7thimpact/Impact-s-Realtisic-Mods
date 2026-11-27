package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.backslot.BackSlot;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.forge.curios.CuriosWornSlots;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornSlots;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Set;
import java.util.UUID;

/** Inventory milestone 4: the back slot (Curios's back slot) appended to vanilla's inventory menu, and Lib's back-slot provider. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryBackSlotGameTests {

    private InventoryBackSlotGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theInventoryMenuKeepsVanillasSlots(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            InventoryMenu menu = player.inventoryMenu;
            helper.assertTrue(menu.slots.size() == InventoryMenu.SHIELD_SLOT + 1, "The menu has vanilla's 46 slots and no extra back slot (it's Curios's)");
            helper.assertTrue(menu.slots.get(InventoryMenu.SHIELD_SLOT).getContainerSlot() == Inventory.SLOT_OFFHAND, "Vanilla's off-hand slot keeps its index");
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
            helper.assertTrue(WornSlots.get(player, WornOn.BACK).is(InventoryItems.satchel()), "Lib's write lands in Inventory's back slot");
            helper.assertTrue(BackSlot.get(player).is(InventoryItems.satchel()), "Lib reads the same item back");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void wornItemDropsOnDeath(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.satchel()));
            Set<UUID> itemsAlreadyNearby = DroppedItems.idsNear(player);
            player.kill();
            helper.assertTrue(WornSlots.get(player, WornOn.BACK).isEmpty(), "The back slot empties on death");
            helper.assertTrue(DroppedItems.countNewNear(player, InventoryItems.satchel(), itemsAlreadyNearby) == 1,
                    "Curios drops the worn pack at the player's feet");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theBackSlotIsCuriosBackSlot(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            helper.assertTrue(WornSlots.hasSlot(player, WornOn.BACK), "Inventory's data gives every player Curios's back slot");
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.satchel()));
            ItemStack inCurios = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.getStacksHandler(CuriosWornSlots.BACK_SLOT_TYPE))
                    .map(handler -> handler.getStacks().getStackInSlot(0))
                    .orElse(ItemStack.EMPTY);
            helper.assertTrue(inCurios.is(InventoryItems.satchel()), "The worn satchel is in Curios's back slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
