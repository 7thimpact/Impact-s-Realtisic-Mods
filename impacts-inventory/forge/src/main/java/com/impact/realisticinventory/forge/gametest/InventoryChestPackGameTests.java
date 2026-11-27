package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.forge.curios.CuriosWornSlots;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackContents;
import com.impact.realisticinventory.pack.PackEffects;
import com.impact.realisticinventory.pack.PackMenu;
import com.impact.realisticinventory.pack.PackMenus;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornPack;
import com.impact.realisticinventory.pack.WornSlots;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/** Packs on the chest (Curios's body slot): a second pack worn alongside the one on the back (requested by the user, Nov 2026). */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryChestPackGameTests {
    private static final int FIRST_SLOT = 0;
    private static final boolean IS_COSMETIC = false;
    private static final boolean IS_VISIBLE = true;
    private static final int SATCHEL_SLOTS = 3;
    private static final int FULL_STACK = 64;
    private static final int COBBLESTONE_COUNT = 5 * FULL_STACK;
    private static final double FRAME_PACK_SPEED_FACTOR = 0.9;
    private static final double SPEED_TOLERANCE = 0.0001;

    private InventoryChestPackGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theChestIsCuriosBodySlot(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            helper.assertTrue(WornSlots.hasSlot(player, WornOn.CHEST), "Inventory's data gives every player Curios's body slot");
            WornSlots.set(player, WornOn.CHEST, new ItemStack(InventoryItems.backpack()));
            ItemStack inCurios = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.getStacksHandler(CuriosWornSlots.CHEST_SLOT_TYPE))
                    .map(handler -> handler.getStacks().getStackInSlot(FIRST_SLOT))
                    .orElse(ItemStack.EMPTY);
            helper.assertTrue(inCurios.is(InventoryItems.backpack()), "The chest pack is in Curios's body slot");
            SlotContext chest = new SlotContext(CuriosWornSlots.CHEST_SLOT_TYPE, player, FIRST_SLOT, IS_COSMETIC, IS_VISIBLE);
            helper.assertTrue(CuriosApi.isStackValid(chest, new ItemStack(InventoryItems.framePack())), "Every pack fits the chest in Curios's screen");
            helper.assertFalse(CuriosApi.isStackValid(chest, new ItemStack(Items.STONE)), "Stone doesn't");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void sneakRightClickWearsThePackOnTheChest(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            player.setShiftKeyDown(true);
            useInMainHand(player, new ItemStack(InventoryItems.backpack()));
            helper.assertTrue(WornSlots.get(player, WornOn.CHEST).is(InventoryItems.backpack()), "Sneak-right-click puts the pack on the chest");
            helper.assertTrue(WornSlots.get(player, WornOn.BACK).isEmpty(), "and leaves the back alone");
            player.setShiftKeyDown(false);
            useInMainHand(player, new ItemStack(InventoryItems.satchel()));
            helper.assertTrue(WornSlots.get(player, WornOn.BACK).is(InventoryItems.satchel()), "A plain right-click still wears it on the back");
            helper.assertTrue(WornSlots.get(player, WornOn.CHEST).is(InventoryItems.backpack()), "with the chest pack still on");
            helper.assertTrue(WornPack.worn(player).size() == 2, "Two packs are worn at once");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void pickupsFillTheBackPackThenTheChestPack(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.satchel()));
            WornSlots.set(player, WornOn.CHEST, new ItemStack(InventoryItems.backpack()));
            ItemStack cobblestone = new ItemStack(Items.COBBLESTONE, COBBLESTONE_COUNT);
            int movedCount = WornPack.placeInEmptySlots(player, cobblestone);
            int inBack = countIn(WornSlots.get(player, WornOn.BACK));
            int inChest = countIn(WornSlots.get(player, WornOn.CHEST));
            helper.assertTrue(movedCount == COBBLESTONE_COUNT && cobblestone.isEmpty(), "Both packs together take all five stacks");
            helper.assertTrue(inBack == SATCHEL_SLOTS * FULL_STACK, "The pack on the back fills first");
            helper.assertTrue(inChest == COBBLESTONE_COUNT - SATCHEL_SLOTS * FULL_STACK, "and the rest goes into the chest pack");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theChestPackOpensOnItsOwn(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.satchel()));
            ItemStack chestPack = new ItemStack(InventoryItems.backpack());
            WornSlots.set(player, WornOn.CHEST, chestPack);
            PackMenus.openWornPack(player, WornOn.CHEST);
            helper.assertTrue(player.containerMenu instanceof PackMenu, "N opens the chest pack's menu");
            PackMenu menu = (PackMenu) player.containerMenu;
            helper.assertTrue(menu.packSlotCount() == WornPack.slotCount(chestPack), "with the chest pack's 9 slots, not the satchel's 3");
            menu.getSlot(FIRST_SLOT).set(new ItemStack(Items.APPLE));
            helper.assertTrue(PackContents.stacksIn(chestPack).stream().anyMatch(stack -> stack.is(Items.APPLE)), "What goes in is saved in the chest pack");
            WornSlots.set(player, WornOn.CHEST, ItemStack.EMPTY);
            helper.assertFalse(menu.stillValid(player), "Taking the chest pack off closes its menu");
            player.closeContainer();
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void aFramePackOnTheChestSlowsToo(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            double normalSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            WornSlots.set(player, WornOn.CHEST, new ItemStack(InventoryItems.framePack()));
            PackEffects.tick(player);
            double chestSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            helper.assertTrue(Math.abs(chestSpeed - normalSpeed * FRAME_PACK_SPEED_FACTOR) < SPEED_TOLERANCE, "A frame pack on the chest slows walking");
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.framePack()));
            PackEffects.tick(player);
            double bothSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            helper.assertTrue(Math.abs(bothSpeed - chestSpeed) < SPEED_TOLERANCE, "Two frame packs slow you no more than one");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static void useInMainHand(ServerPlayer player, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        player.gameMode.useItem(player, player.level(), held, InteractionHand.MAIN_HAND);
    }

    private static int countIn(ItemStack pack) {
        return PackContents.stacksIn(pack).stream().mapToInt(ItemStack::getCount).sum();
    }
}
