package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.counter.ImpactsCounters;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.advancement.AchievementChecks;
import com.impact.realisticinventory.advancement.InventoryAdvancements;
import com.impact.realisticinventory.advancement.InventoryTriggers;
import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.PickupRefusal;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackWearing;
import com.impact.realisticinventory.pack.WornOn;
import net.minecraft.advancements.Advancement;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.stream.IntStream;

/** Inventory milestone 10: the "Realistic Inventory" achievements, earned through their real triggers. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryAchievementGameTests {
    private InventoryAchievementGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void switchingEarnsHandsFullJugglerAndSecondNature(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            HandSwitch.swapHands(player);
            helper.assertTrue(isDone(player, InventoryAdvancements.HANDS_FULL), "One switch earns Hands Full");
            helper.assertFalse(isDone(player, InventoryAdvancements.JUGGLER), "One switch isn't Juggler");
            IntStream.range(1, InventoryAdvancements.JUGGLER_SWITCHES).forEach(switchNumber -> HandSwitch.swapHands(player));
            helper.assertTrue(isDone(player, InventoryAdvancements.JUGGLER), "Ten switches in five seconds earn Juggler");
            ImpactsCounters.add(player, InventoryTriggers.SLOT_SWITCHES_COUNTER, null, InventoryAdvancements.SECOND_NATURE_SWITCHES);
            helper.assertTrue(isDone(player, InventoryAdvancements.SECOND_NATURE), "A thousand switches earn Second Nature");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void wearingEachPackEarnsItsAchievement(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            PackWearing.onWornItemChanged(player, WornOn.BACK, ItemStack.EMPTY, new ItemStack(InventoryItems.satchel()));
            helper.assertTrue(isDone(player, InventoryAdvancements.PACKING_LIGHT), "A satchel earns Packing Light");
            helper.assertFalse(isDone(player, InventoryAdvancements.PACK_MULE), "A satchel isn't a backpack");
            PackWearing.onWornItemChanged(player, WornOn.BACK, new ItemStack(InventoryItems.satchel()), new ItemStack(InventoryItems.backpack()));
            helper.assertTrue(isDone(player, InventoryAdvancements.PACK_MULE), "A backpack earns Pack Mule");
            PackWearing.onWornItemChanged(player, WornOn.BACK, new ItemStack(InventoryItems.backpack()), new ItemStack(InventoryItems.framePack()));
            helper.assertTrue(isDone(player, InventoryAdvancements.HEAVY_LIFTING), "A frame pack earns Heavy Lifting");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void fullPocketsEarnPocketSpaceAndNoRoomAtAll(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            IntStream.rangeClosed(InventoryLayout.FIRST_POCKET_INDEX, InventoryLayout.LAST_POCKET_INDEX)
                    .forEach(pocketIndex -> player.getInventory().setItem(pocketIndex, new ItemStack(Items.DIRT)));
            AchievementChecks.checkPockets(player);
            helper.assertTrue(isDone(player, InventoryAdvancements.POCKET_SPACE), "Nine filled pockets earn Pocket Space");
            PickupRefusal.onRefused(player, new ItemStack(Items.STONE));
            helper.assertTrue(isDone(player, InventoryAdvancements.NO_ROOM_AT_ALL), "A refused pickup earns No Room at All");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void travelLightNeedsEmptyPocketsAndNoPack(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            player.getInventory().setItem(InventoryLayout.FIRST_POCKET_INDEX, new ItemStack(Items.DIRT));
            AchievementChecks.onChangedDimension(player, Level.NETHER);
            helper.assertFalse(isDone(player, InventoryAdvancements.TRAVEL_LIGHT), "Dirt in a pocket isn't traveling light");
            player.getInventory().setItem(InventoryLayout.FIRST_POCKET_INDEX, ItemStack.EMPTY);
            AchievementChecks.onChangedDimension(player, Level.NETHER);
            helper.assertTrue(isDone(player, InventoryAdvancements.TRAVEL_LIGHT), "Empty pockets and no pack earn Travel Light");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void hittingRightAfterSwitchingEarnsQuickDraw(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        try {
            AchievementChecks.onPlayerHurtEntity(player, zombie);
            helper.assertFalse(isDone(player, InventoryAdvancements.QUICK_DRAW), "A hit without a switch isn't Quick Draw");
            HandSwitch.swapHands(player);
            AchievementChecks.onPlayerHurtEntity(player, zombie);
            helper.assertTrue(isDone(player, InventoryAdvancements.QUICK_DRAW), "A hit right after switching earns Quick Draw");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static boolean isDone(ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(advancementId);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
