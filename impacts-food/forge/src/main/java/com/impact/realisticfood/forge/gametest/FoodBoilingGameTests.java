package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingPot;
import com.impact.realisticfood.boiling.CauldronPotHost;
import com.impact.realisticfood.boiling.CauldronPots;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

/** Food milestone 3: heat, warm-up and cool-down, single-item boiling, evaporation, Lib's boiling answer and pass-through. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodBoilingGameTests {
    private static final BlockPos HEAT_SOURCE = BlockPos.ZERO;
    private static final BlockPos CAULDRON = HEAT_SOURCE.above();
    private static final int SPEC_HEAT_UP_TICKS = 200;
    private static final int SPEC_COOL_DOWN_TICKS = 200;
    private static final int EGG_BOIL_TICKS = 200;
    private static final int FULL = 3;
    /** Salty's Realistic Forging's raw gold ore, which it washes in water cauldrons. */
    private static final String SRF_RAW_GOLD_ORE = "realisticforgingores:rawgoldore";

    private FoodBoilingGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void potWarmsUpBoilsAndCoolsOff(GameTestHelper helper) {
        StandInPotHost host = new StandInPotHost(helper.getLevel(), helper.absolutePos(CAULDRON), 0);
        BoilingPot pot = new BoilingPot();
        host.setHeated(true);
        tick(pot, host, SPEC_HEAT_UP_TICKS - 1);
        helper.assertFalse(pot.isBoiling(), "Water over heat isn't boiling before 200 ticks");
        tick(pot, host, 1);
        helper.assertTrue(pot.isBoiling(), "It boils after 200 ticks");
        host.setHeated(false);
        tick(pot, host, SPEC_COOL_DOWN_TICKS - 1);
        helper.assertTrue(pot.isBoiling(), "With the heat gone it keeps boiling for a while");
        tick(pot, host, 1);
        helper.assertFalse(pot.isBoiling(), "It stops boiling 200 ticks after the heat is gone");
        helper.assertTrue(pot.isIdle(host), "A cold empty pot has nothing left to track");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void potBoilsItemsAndEvaporates(GameTestHelper helper) {
        int evaporationTicks = 50;
        StandInPotHost host = new StandInPotHost(helper.getLevel(), helper.absolutePos(CAULDRON), evaporationTicks);
        BoilingPot pot = new BoilingPot();
        host.setHeated(true);
        helper.assertFalse(pot.tryAdd(host, new ItemStack(Items.STICK)), "No recipe boils a stick");
        pot.boilNow(host);
        helper.assertTrue(pot.tryAdd(host, new ItemStack(Items.EGG, 3)), "A boiling pot takes an egg");
        helper.assertTrue(pot.items().size() == 1, "One egg goes in at a time");
        tick(pot, host, EGG_BOIL_TICKS - 1);
        helper.assertTrue(host.released().isEmpty(), "The egg isn't done before its boil time");
        tick(pot, host, 1);
        helper.assertTrue(host.released().size() == 1 && host.released().get(0).is(FoodItems.fixedFood(FixedFood.BOILED_EGG)),
                "After 200 ticks a Boiled Egg comes out");
        helper.assertTrue(host.evaporatedLevels() == EGG_BOIL_TICKS / evaporationTicks, "A boiling pot loses a level every evaporation interval");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE, timeoutTicks = 400)
    public static void cauldronOverCampfireBoils(GameTestHelper helper) {
        BlockPos cauldron = placeCauldronOverCampfire(helper, FULL);
        helper.runAtTickTime(SPEC_HEAT_UP_TICKS / 2, () ->
                helper.assertFalse(ImpactsLib.isBoiling(helper.getLevel(), cauldron), "Still warming up halfway through"));
        helper.succeedWhen(() -> {
            helper.assertTrue(ImpactsLib.isBoiling(helper.getLevel(), cauldron), "A water cauldron over a lit campfire boils");
            helper.assertTrue(helper.getTick() >= SPEC_HEAT_UP_TICKS, "Boiling takes the full warm-up");
        });
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE, timeoutTicks = 400)
    public static void droppedEggBecomesBoiledEgg(GameTestHelper helper) {
        StrayItems.clearAround(helper, CAULDRON);
        BlockPos cauldron = placeCauldronOverCampfire(helper, FULL);
        boilNow(helper, cauldron);
        helper.spawnItem(Items.EGG, 0.5F, 1.5F, 0.5F);
        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(FoodItems.fixedFood(FixedFood.BOILED_EGG), CAULDRON, 3.0);
            helper.assertItemEntityNotPresent(Items.EGG, CAULDRON, 3.0);
        });
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void clicksAddOnlyItemsFoodBoils(GameTestHelper helper) {
        BlockPos cauldron = placeCauldronOverCampfire(helper, FULL);
        BoilingPot pot = boilNow(helper, cauldron);
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EGG, 2));
            helper.assertTrue(useCauldron(helper, cauldron, player) == InteractionResult.CONSUME, "Food takes an egg into a boiling cauldron");
            helper.assertTrue(player.getMainHandItem().getCount() == 1 && pot.items().size() == 1, "One egg moves from the hand to the pot");
            assertPassesThrough(helper, cauldron, player, pot, Items.STICK);
            Optional<Item> srfOre = ResourceIds.tryParse(SRF_RAW_GOLD_ORE).map(ForgeRegistries.ITEMS::getValue).filter(item -> item != Items.AIR);
            srfOre.ifPresent(ore -> assertPassesThrough(helper, cauldron, player, pot, ore));
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void droppedUnknownItemsStayPut(GameTestHelper helper) {
        BlockPos cauldron = placeCauldronOverCampfire(helper, FULL);
        BoilingPot pot = boilNow(helper, cauldron);
        helper.spawnItem(Items.STICK, 0.5F, 1.5F, 0.5F);
        helper.runAfterDelay(20, () -> {
            helper.assertItemEntityPresent(Items.STICK, CAULDRON, 2.0);
            helper.assertTrue(pot.items().isEmpty(), "The pot doesn't take a stick");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void boilingDryDropsTheItems(GameTestHelper helper) {
        BlockPos cauldron = placeCauldronOverCampfire(helper, 1);
        BoilingPot pot = boilNow(helper, cauldron);
        pot.tryAdd(new CauldronPotHost(helper.getLevel(), cauldron), new ItemStack(Items.EGG));
        ConfigOverrides.withValue(FoodServerConfig.get().boiling.evaporationTicks, 1, () ->
                CauldronPots.of(helper.getLevel()).tickPot(helper.getLevel(), cauldron, false));
        helper.assertBlockPresent(Blocks.CAULDRON, CAULDRON);
        helper.assertItemEntityPresent(Items.EGG, CAULDRON, 2.0);
        helper.assertTrue(CauldronPots.of(helper.getLevel()).pot(cauldron).isEmpty(), "A dry cauldron has no pot");
        helper.assertFalse(ImpactsLib.isBoiling(helper.getLevel(), cauldron), "A dry cauldron isn't boiling");
        helper.succeed();
    }

    private static void assertPassesThrough(GameTestHelper helper, BlockPos cauldron, ServerPlayer player, BoilingPot pot, Item item) {
        int itemsBefore = pot.items().size();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        BoilingPotSnapshot before = BoilingPotSnapshot.of(helper.getLevel(), cauldron);
        helper.assertTrue(useCauldron(helper, cauldron, player) == InteractionResult.PASS,
                ResourceIds.itemId(item) + " should pass through Food's cauldron handler");
        helper.assertTrue(player.getMainHandItem().is(item) && player.getMainHandItem().getCount() == 1, "The item stays in the hand");
        helper.assertTrue(pot.items().size() == itemsBefore && before.equals(BoilingPotSnapshot.of(helper.getLevel(), cauldron)),
                "The cauldron doesn't change");
    }

    private static InteractionResult useCauldron(GameTestHelper helper, BlockPos cauldron, ServerPlayer player) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(cauldron), Direction.UP, cauldron, false);
        return helper.getLevel().getBlockState(cauldron).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
    }

    private static BlockPos placeCauldronOverCampfire(GameTestHelper helper, int waterLevel) {
        helper.setBlock(HEAT_SOURCE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.setBlock(CAULDRON, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, waterLevel));
        return helper.absolutePos(CAULDRON);
    }

    private static BoilingPot boilNow(GameTestHelper helper, BlockPos cauldron) {
        ServerLevel level = helper.getLevel();
        BoilingPot pot = CauldronPots.of(level).track(cauldron);
        pot.boilNow(new CauldronPotHost(level, cauldron));
        return pot;
    }

    private static void tick(BoilingPot pot, StandInPotHost host, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            pot.tick(host);
        }
    }

    /** The parts of a cauldron a pass-through must leave alone: its block state and its pot's water. */
    private record BoilingPotSnapshot(BlockState state, boolean isBoiling) {
        static BoilingPotSnapshot of(ServerLevel level, BlockPos cauldron) {
            return new BoilingPotSnapshot(level.getBlockState(cauldron), CauldronPots.isBoiling(level, cauldron));
        }
    }
}
