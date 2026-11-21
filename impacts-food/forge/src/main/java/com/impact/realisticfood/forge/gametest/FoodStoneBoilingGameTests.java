package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.water.WaterVessels;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.advancement.FoodAdvancements;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.item.HotPebbleItem;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.stoneboiling.PebbleHeating;
import com.impact.realisticfood.stoneboiling.StoneBoilingPotBlock;
import com.impact.realisticfood.stoneboiling.StoneBoilingPotBlockEntity;
import com.impact.realisticfood.stoneboiling.StoneBoilingPots;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Food milestone 10, stone boiling (only with Impact's Realistic Crafting): heating pebbles on a campfire, the Hot
 * Pebble cooling back, and the stone-boiling pot made from Crafting's Water-filled Wooden Bucket. Crafting's items are
 * looked up by registry ID; the tests that need them pass at once without Crafting.
 */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodStoneBoilingGameTests {
    private static final BlockPos GROUND = BlockPos.ZERO;
    private static final BlockPos POT = GROUND.above();
    private static final String OTHER_MOD_KEY = "othermod:mark";
    private static final int SPENT_USES = 2;
    private static final int CLICK_GAP_TICKS = 4;
    private static final int SHORT_BOIL_TICKS = 5;
    private static final int SETTLE_TICKS = 15;
    private static final ResourceLocation STONE_PEBBLE = ResourceIds.of(FoodMod.CRAFTING_MOD_ID, "stone_pebble");

    private FoodStoneBoilingGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aHotPebbleCoolsBackIntoItsPebble(GameTestHelper helper) {
        ItemStack pebble = markedStack(new ItemStack(Items.FLINT));
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ConfigOverrides.withValue(FoodServerConfig.get().stoneBoiling.pebbleCoolDownTicks, 2, () ->
                    player.getInventory().setItem(0, HotPebbleItem.heatedFrom(FoodItems.hotPebble(), pebble)));
            player.getInventory().tick();
            helper.assertTrue(player.getInventory().getItem(0).is(FoodItems.hotPebble()), "Still hot after one tick");
            player.getInventory().tick();
            ItemStack cooled = player.getInventory().getItem(0);
            helper.assertTrue(cooled.is(Items.FLINT), "It cools back into the item it was heated from");
            helper.assertTrue(UsesDurability.usesSpent(cooled) == SPENT_USES && cooled.getTag().contains(OTHER_MOD_KEY),
                    "The pebble keeps its uses and other mods' NBT");
            player.getInventory().setItem(1, new ItemStack(FoodItems.hotPebble()));
            player.getInventory().tick();
            helper.assertTrue(player.getInventory().getItem(1).is(FoodItems.hotPebble()), "A Hot Pebble from a command starts its cool-down");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void holdingAPebbleOnALitCampfireHeatsIt(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        helper.setBlock(GROUND, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(crafting(STONE_PEBBLE), 2));
        Runnable click = () -> PebbleHeating.tryHeat(helper.getLevel(), helper.absolutePos(GROUND), player, InteractionHand.MAIN_HAND);
        int clicks = FoodServerConfig.get().stoneBoiling.heatTicks.get() / CLICK_GAP_TICKS;
        GameTestSequence sequence = helper.startSequence().thenExecute(click).thenExecute(() ->
                helper.assertTrue(player.getInventory().countItem(FoodItems.hotPebble()) == 0, "One click doesn't heat a pebble"));
        for (int clickNumber = 0; clickNumber < clicks; clickNumber++) {
            sequence.thenExecuteAfter(CLICK_GAP_TICKS, click);
        }
        sequence.thenExecute(() -> {
            try {
                helper.assertTrue(player.getInventory().countItem(FoodItems.hotPebble()) == 1, "Holding use for 2 seconds heats one pebble");
                helper.assertTrue(player.getMainHandItem().is(crafting(STONE_PEBBLE)) && player.getMainHandItem().getCount() == 1,
                        "The other pebble stays in the hand");
                helper.setBlock(GROUND, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
                helper.assertTrue(PebbleHeating.tryHeat(helper.getLevel(), helper.absolutePos(GROUND), player, InteractionHand.MAIN_HAND)
                        == InteractionResult.PASS, "An unlit campfire heats nothing");
            } finally {
                MockPlayers.leaveWorld(player);
            }
        }).thenSucceed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aWoodenWaterBucketSetsDownAsAPot(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        helper.setBlock(GROUND, Blocks.STONE);
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, markedStack(new ItemStack(crafting(StoneBoilingPots.CRAFTING_WATER_BUCKET))));
            helper.assertTrue(place(helper, player) == InteractionResult.PASS, "Without sneaking, Crafting pours the water");
            player.setShiftKeyDown(true);
            helper.assertTrue(place(helper, player) == InteractionResult.CONSUME, "Sneaking sets the bucket down");
            helper.assertBlockPresent(StoneBoilingPots.block(), POT);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "The bucket leaves the hand");
            helper.assertTrue(useWithMainHand(helper, player) == InteractionResult.CONSUME, "Sneaking with an empty hand takes it back");
            ItemStack returned = player.getMainHandItem();
            helper.assertTrue(returned.is(crafting(StoneBoilingPots.CRAFTING_WATER_BUCKET)), "It's still the Water-filled Wooden Bucket");
            helper.assertTrue(UsesDurability.usesSpent(returned) == SPENT_USES && returned.getTag().contains(OTHER_MOD_KEY),
                    "It keeps its uses and other mods' NBT");
            helper.setBlock(POT, Blocks.AIR);
            helper.setBlock(GROUND, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
            player.setShiftKeyDown(true);
            helper.assertTrue(place(helper, player) == InteractionResult.PASS, "A wooden bucket never goes on a campfire");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aHotPebbleBoilsThePotForAWhile(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        StoneBoilingPotBlockEntity pot = setPotDown(helper, player, new ItemStack(crafting(StoneBoilingPots.CRAFTING_WATER_BUCKET)));
        helper.assertFalse(ImpactsLib.isBoiling(helper.getLevel(), helper.absolutePos(POT)), "A pot without pebbles is cold");
        player.setItemInHand(InteractionHand.MAIN_HAND, HotPebbleItem.heatedFrom(FoodItems.hotPebble(), new ItemStack(crafting(STONE_PEBBLE))));
        ConfigOverrides.withValue(FoodServerConfig.get().stoneBoiling.boilTicks, SHORT_BOIL_TICKS, () ->
                helper.assertTrue(useWithMainHand(helper, player) == InteractionResult.CONSUME, "The Hot Pebble drops in"));
        helper.assertTrue(ImpactsLib.isBoiling(helper.getLevel(), helper.absolutePos(POT)), "A Hot Pebble boils the water at once");
        helper.assertTrue(UsesDurability.usesSpent(pot.bucketToReturn()) == 1, "The pebble costs the bucket 1 use");
        helper.assertTrue(isDone(helper, player, FoodAdvancements.STONE_SOUP), "Stone boiling grants Stone Soup");
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            try {
                helper.assertFalse(ImpactsLib.isBoiling(helper.getLevel(), helper.absolutePos(POT)), "The boil ends with the pebble's heat");
                helper.assertItemEntityPresent(crafting(STONE_PEBBLE), POT, 3.0);
                helper.assertTrue(pot.pebbles().isEmpty(), "The spent pebble comes back out, cooled");
            } finally {
                MockPlayers.leaveWorld(player);
            }
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void theBucketsLastUseBreaksThePot(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        ItemStack bucket = new ItemStack(crafting(StoneBoilingPots.CRAFTING_WATER_BUCKET));
        bucket.getOrCreateTag().putInt(UsesDurability.USES_KEY, UsesDurability.get(bucket) - 1);
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            setPotDown(helper, player, bucket);
            player.setItemInHand(InteractionHand.MAIN_HAND, HotPebbleItem.heatedFrom(FoodItems.hotPebble(), new ItemStack(crafting(STONE_PEBBLE))));
            useWithMainHand(helper, player);
            helper.assertBlockNotPresent(StoneBoilingPots.block(), POT);
            helper.assertItemEntityPresent(crafting(STONE_PEBBLE), POT, 2.0);
            helper.assertItemEntityNotPresent(crafting(StoneBoilingPots.CRAFTING_WATER_BUCKET), POT, 2.0);
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aDryPotGivesBackTheEmptyWoodenBucket(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            StoneBoilingPotBlockEntity pot = setPotDown(helper, player, markedStack(new ItemStack(crafting(StoneBoilingPots.CRAFTING_WATER_BUCKET))));
            int bottles = pot.boilsLeft();
            for (int bottle = 0; bottle < bottles; bottle++) {
                helper.assertTrue(WaterVessels.takeBottle(helper.getLevel(), helper.absolutePos(POT)), "Bottle " + (bottle + 1) + " fills from the pot");
            }
            helper.assertFalse(helper.getBlockState(POT).getValue(StoneBoilingPotBlock.HAS_WATER), "The pot shows empty");
            helper.assertFalse(WaterVessels.canFillBottle(helper.getLevel(), helper.absolutePos(POT)), "A dry pot fills no bottle");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            useWithMainHand(helper, player);
            ItemStack returned = player.getMainHandItem();
            helper.assertTrue(returned.is(crafting(StoneBoilingPots.CRAFTING_WOODEN_BUCKET)), "It comes back as Crafting's empty Wooden Bucket");
            helper.assertTrue(UsesDurability.usesSpent(returned) == SPENT_USES && returned.getTag().contains(OTHER_MOD_KEY),
                    "With its uses and other mods' NBT");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    // The player sneaks and stands beside the pot, close enough for its achievements.
    private static StoneBoilingPotBlockEntity setPotDown(GameTestHelper helper, ServerPlayer player, ItemStack waterBucket) {
        helper.setBlock(GROUND, Blocks.STONE);
        player.moveTo(Vec3.atCenterOf(helper.absolutePos(POT)).add(2.0, 0.0, 0.0));
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, waterBucket);
        place(helper, player);
        player.setShiftKeyDown(false);
        return (StoneBoilingPotBlockEntity) helper.getBlockEntity(POT);
    }

    private static InteractionResult place(GameTestHelper helper, ServerPlayer player) {
        return StoneBoilingPots.tryPlace(helper.getLevel(), helper.absolutePos(GROUND), Direction.UP, player, InteractionHand.MAIN_HAND);
    }

    private static InteractionResult useWithMainHand(GameTestHelper helper, ServerPlayer player) {
        BlockPos pot = helper.absolutePos(POT);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pot), Direction.UP, pot, false);
        player.setShiftKeyDown(player.getMainHandItem().isEmpty());
        InteractionResult result = helper.getLevel().getBlockState(pot).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        player.setShiftKeyDown(false);
        return result;
    }

    private static ItemStack markedStack(ItemStack stack) {
        stack.getOrCreateTag().putInt(UsesDurability.USES_KEY, SPENT_USES);
        stack.getOrCreateTag().putBoolean(OTHER_MOD_KEY, true);
        return stack;
    }

    private static Item crafting(ResourceLocation itemId) {
        return ResourceIds.item(itemId).orElseThrow(() -> new IllegalStateException("Crafting has no item " + itemId));
    }

    private static boolean isDone(GameTestHelper helper, ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = helper.getLevel().getServer().getAdvancements().getAdvancement(advancementId);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
