package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.CraftingProgress;
import com.impact.lib.dump.DumpSection;
import com.impact.lib.dump.ImpactsDump;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.hold.HoldMotion;
import com.impact.lib.hold.WorkMotions;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.timing.CraftDepthTable;
import com.impact.realisticcrafting.timing.CraftTimers;
import com.impact.realisticcrafting.timing.CraftTimes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.RESULT_SLOT;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.runTicks;

/** Crafting milestone 5: the runtime depth table, craft times, and the timer in every grid (the 2×2 included). */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingTimerGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final int PLANKS_TICKS = 10;

    private CraftingTimerGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void depthsFollowTheCraftingTree(GameTestHelper helper) {
        assertDepth(helper, Items.OAK_LOG, 0);
        assertDepth(helper, Items.COBBLESTONE, 0);
        assertDepth(helper, Items.IRON_INGOT, 0);
        assertDepth(helper, Items.OAK_PLANKS, 1);
        assertDepth(helper, Items.IRON_BLOCK, 1);
        assertDepth(helper, Items.IRON_NUGGET, 0);
        assertDepth(helper, CraftingItems.trimmedPlanks(), 2);
        assertDepth(helper, CraftingItems.part(CraftingPart.TRIMMED_STICK), 3);
        assertDepth(helper, CraftingItems.toolPart(ToolMaterial.WOODEN, ToolKind.PICKAXE), 3);
        assertDepth(helper, CraftingItems.looseTool(ToolMaterial.WOODEN, ToolKind.PICKAXE), 4);
        assertDepth(helper, Items.WOODEN_PICKAXE, 4);
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void craftTimesFollowDepth(GameTestHelper helper) {
        assertTicks(helper, "impactscrafting:oak_planks_by_hand", PLANKS_TICKS);
        assertTicks(helper, "impactscrafting:trimmed_oak_planks", 20);
        assertTicks(helper, "impactscrafting:loose_wooden_pickaxe", 40);
        assertTicks(helper, "minecraft:firework_rocket", 20);
        CraftingServerConfig.TimedCraftingSettings settings = CraftingServerConfig.get().timedCrafting;
        ConfigOverrides.withValue(settings.ticksPerDepth, 50, () -> assertTicks(helper, "impactscrafting:loose_wooden_pickaxe", 100));
        ConfigOverrides.withValue(settings.recipeOverrides, List.of("impactscrafting:oak_planks_by_hand=7"), () ->
                assertTicks(helper, "impactscrafting:oak_planks_by_hand", 7));
        ConfigOverrides.withValue(settings.modMultipliers, List.of("impactscrafting=2.0"), () ->
                assertTicks(helper, "impactscrafting:trimmed_oak_planks", 40));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theOutputWaitsForTheTimer(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            table.grid().setItem(0, new ItemStack(Items.OAK_LOG));
            helper.assertTrue(table.getSlot(RESULT_SLOT).getItem().is(Items.OAK_PLANKS), "The output shows at once");
            helper.assertTrue(CraftTimers.isOutputLocked(table), "but is locked while the craft runs");
            Optional<CraftingProgress.Progress> progress = CraftingProgress.get(table);
            helper.assertTrue(progress.isPresent() && progress.get().totalTicks() == PLANKS_TICKS, "Lib's progress shows the 10-tick craft");
            table.clicked(RESULT_SLOT, 0, ClickType.PICKUP, player);
            helper.assertTrue(table.getCarried().isEmpty(), "Clicking the output takes nothing yet");
            runTicks(player, PLANKS_TICKS - 1);
            helper.assertTrue(CraftTimers.isOutputLocked(table), "Still locked after 9 ticks");
            runTicks(player, 1);
            helper.assertFalse(CraftTimers.isOutputLocked(table), "Unlocked after 10");
            table.clicked(RESULT_SLOT, 0, ClickType.PICKUP, player);
            helper.assertTrue(table.getCarried().is(Items.OAK_PLANKS), "and then it can be taken");
            table.setCarried(ItemStack.EMPTY);
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void aRunningCraftShowsTheCraftingMotion(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            runTicks(player, 1);
            helper.assertTrue(WorkMotions.shownMotion(player).isEmpty(), "An empty grid shows no crafting motion");
            table.grid().setItem(0, new ItemStack(Items.OAK_LOG));
            runTicks(player, 1);
            helper.assertTrue(WorkMotions.shownMotion(player).equals(Optional.of(HoldMotion.CRAFT)), "A running craft shows Lib's crafting motion");
            runTicks(player, PLANKS_TICKS);
            helper.assertTrue(WorkMotions.shownMotion(player).isEmpty(), "and it stops when the craft is done");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void changingTheGridStartsOver(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            table.grid().setItem(0, new ItemStack(Items.OAK_LOG));
            runTicks(player, PLANKS_TICKS / 2);
            table.grid().setItem(0, new ItemStack(Items.OAK_LOG, 2));
            Optional<CraftingProgress.Progress> progress = CraftingProgress.get(table);
            helper.assertTrue(progress.isPresent() && progress.get().elapsedTicks() == 0, "A grid change resets the craft");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void shiftClickRepeatsWithTheTimer(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            table.grid().setItem(0, new ItemStack(Items.OAK_LOG, 3));
            table.clicked(RESULT_SLOT, 0, ClickType.QUICK_MOVE, player);
            helper.assertTrue(player.getInventory().countItem(Items.OAK_PLANKS) == 0, "Shift-clicking a running craft takes nothing yet");
            runTicks(player, PLANKS_TICKS);
            helper.assertTrue(player.getInventory().countItem(Items.OAK_PLANKS) == 1, "Each craft is taken when its timer finishes");
            runTicks(player, 2 * PLANKS_TICKS + 2);
            helper.assertTrue(player.getInventory().countItem(Items.OAK_PLANKS) == 3, "until the inputs run out");
            helper.assertTrue(table.grid().isEmpty(), "leaving the grid empty");
            player.getInventory().clearContent();
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theInventoryGridIsTimedToo(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.inventoryMenu.getCraftSlots().setItem(0, new ItemStack(Items.SPRUCE_LOG));
            helper.assertTrue(CraftTimers.isOutputLocked(player.inventoryMenu), "The 2x2 grid's output is locked while it crafts");
            runTicks(player, PLANKS_TICKS);
            helper.assertFalse(CraftTimers.isOutputLocked(player.inventoryMenu), "and opens after 10 ticks");
            player.inventoryMenu.getCraftSlots().clearContent();
            CraftingServerConfig.TimedCraftingSettings settings = CraftingServerConfig.get().timedCrafting;
            ConfigOverrides.withValue(settings.timesInventoryGrid, false, () -> {
                player.inventoryMenu.getCraftSlots().setItem(0, new ItemStack(Items.SPRUCE_LOG));
                helper.assertFalse(CraftTimers.isOutputLocked(player.inventoryMenu), "timedCrafting.inventoryGrid switches the 2x2 timer off");
                player.inventoryMenu.getCraftSlots().clearContent();
            });
            ConfigOverrides.withValue(settings.isOn, false, () -> {
                player.inventoryMenu.getCraftSlots().setItem(0, new ItemStack(Items.SPRUCE_LOG));
                helper.assertFalse(CraftTimers.isOutputLocked(player.inventoryMenu), "timedCrafting.enabled switches every timer off");
                player.inventoryMenu.getCraftSlots().clearContent();
            });
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void dumpHeldShowsDepthAndTime(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ItemStack trimmed = WoodData.of(CraftingItems.trimmedPlanks(), ResourceIds.of("minecraft", "oak"));
            List<DumpSection> sections = ImpactsDump.describeItem(trimmed, player);
            DumpSection crafting = sections.stream().filter(section -> section.name().equals("crafting")).findFirst().orElseThrow();
            helper.assertTrue("2".equals(String.valueOf(crafting.entries().get("depth"))), "dump held shows the depth");
            helper.assertTrue("20".equals(String.valueOf(crafting.entries().get("craftTicks"))), "and the craft time");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static void assertDepth(GameTestHelper helper, Item item, int expected) {
        int actual = CraftDepthTable.depthOf(item).orElse(-1);
        helper.assertTrue(actual == expected, ResourceIds.itemId(item) + " should be depth " + expected + ", is " + actual);
    }

    private static void assertTicks(GameTestHelper helper, String recipeId, int expected) {
        Recipe<?> recipe = helper.getLevel().getRecipeManager().byKey(ResourceIds.tryParse(recipeId).orElseThrow()).orElseThrow();
        int actual = CraftTimes.ticksFor(recipe);
        helper.assertTrue(actual == expected, recipeId + " should take " + expected + " ticks, takes " + actual);
    }
}
