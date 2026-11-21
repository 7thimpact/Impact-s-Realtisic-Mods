package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.audit.RecipeAudit;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingPot;
import com.impact.realisticfood.boiling.CauldronInteractions;
import com.impact.realisticfood.boiling.CauldronPotHost;
import com.impact.realisticfood.boiling.CauldronPots;
import com.impact.realisticfood.boiling.PotHost;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import com.impact.realisticfood.soup.SoupBatch;
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
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Food milestone 5: soup batches, the biggest recipe winning, sets, Watery Soup, ladling, eating from the pot, bottles, grid stews off. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodSoupGameTests {
    private static final BlockPos HEAT_SOURCE = BlockPos.ZERO;
    private static final BlockPos CAULDRON = HEAT_SOURCE.above();
    private static final int FULL = 3;
    private static final int HUNGRY = 10;

    private FoodSoupGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void threeBonesMakeBoneBroth(GameTestHelper helper) {
        StandInPotHost host = boilingStandIn(helper);
        BoilingPot pot = new BoilingPot();
        pot.boilNow(host);
        addAll(helper, pot, host, new ItemStack(Items.BONE, 3));
        settle(pot, host);
        SoupBatch soup = pot.soup().orElseThrow();
        helper.assertTrue(soup.serving().is(FoodItems.fixedFood(FixedFood.BONE_BROTH)) && soup.servings() == 2, "3 bones make 2 servings of Bone Broth");
        helper.assertFalse(pot.isSoupReady(), "It cooks before it can be served");
        tick(pot, host, FoodServerConfig.get().soup.cookTicks.get());
        helper.assertTrue(pot.isSoupReady(), "After 400 ticks it is ready");
        helper.assertTrue(pot.takeServing(host).orElseThrow().is(FoodItems.fixedFood(FixedFood.BONE_BROTH)), "The first serving is Bone Broth");
        helper.assertTrue(host.evaporatedLevels() == 0, "One serving uses no water yet");
        pot.takeServing(host).orElseThrow();
        helper.assertTrue(host.evaporatedLevels() == 1, "Every 2 servings use a level of water");
        helper.assertTrue(pot.soup().isEmpty() && pot.takeServing(host).isEmpty(), "The batch is used up");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void theBiggestSoupWins(GameTestHelper helper) {
        StandInPotHost host = boilingStandIn(helper);
        BoilingPot pot = new BoilingPot();
        pot.boilNow(host);
        addAll(helper, pot, host, new ItemStack(FoodItems.piece(FoodPiece.RAW_RABBIT_PIECE)), new ItemStack(FoodItems.piece(FoodPiece.CARROT_SLICES)),
                new ItemStack(FoodItems.piece(FoodPiece.POTATO_SLICES)), new ItemStack(Items.BROWN_MUSHROOM));
        settle(pot, host);
        helper.assertTrue(pot.soup().orElseThrow().serving().is(Items.RABBIT_STEW), "Rabbit stew wins over the hearty stew inside it");
        helper.assertTrue(pot.items().isEmpty(), "The stew uses every ingredient");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void moreSetsMakeMoreBowls(GameTestHelper helper) {
        StandInPotHost host = boilingStandIn(helper);
        BoilingPot pot = new BoilingPot();
        pot.boilNow(host);
        addAll(helper, pot, host, new ItemStack(Items.BEETROOT, 9));
        settle(pot, host);
        helper.assertTrue(pot.soup().orElseThrow().servings() == 6, "3 sets of beetroot make 6 servings, the cauldron's most");
        BoilingPot eggAndBroth = new BoilingPot();
        eggAndBroth.boilNow(host);
        addAll(helper, eggAndBroth, host, new ItemStack(Items.EGG), new ItemStack(Items.BONE, 3));
        settle(eggAndBroth, host);
        helper.assertTrue(eggAndBroth.items().size() == 1 && eggAndBroth.items().get(0).input().is(Items.EGG), "An egg boils on beside the soup");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void unmatchedIngredientsMakeWaterySoup(GameTestHelper helper) {
        StandInPotHost host = boilingStandIn(helper);
        BoilingPot pot = new BoilingPot();
        pot.boilNow(host);
        addAll(helper, pot, host, new ItemStack(Items.BONE), new ItemStack(Items.RED_MUSHROOM));
        settle(pot, host);
        helper.assertTrue(pot.soup().isEmpty(), "A bone and a mushroom make no soup yet");
        tick(pot, host, FoodServerConfig.get().soup.cookTicks.get());
        SoupBatch watery = pot.soup().orElseThrow();
        helper.assertTrue(watery.serving().is(FoodItems.fixedFood(FixedFood.WATERY_SOUP)) && watery.isReady(), "After a cook's wait they make Watery Soup");
        helper.assertTrue(FoodCuttingGameTests.foodOf(FoodItems.fixedFood(FixedFood.WATERY_SOUP)).getNutrition() == 2, "Watery Soup restores 2 hunger");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void suspiciousStewTakesTheFlowersEffect(GameTestHelper helper) {
        StandInPotHost host = boilingStandIn(helper);
        BoilingPot pot = new BoilingPot();
        pot.boilNow(host);
        addAll(helper, pot, host, new ItemStack(Items.BROWN_MUSHROOM), new ItemStack(Items.RED_MUSHROOM), new ItemStack(Items.DANDELION));
        settle(pot, host);
        ItemStack serving = pot.soup().orElseThrow().serving();
        helper.assertTrue(serving.is(Items.SUSPICIOUS_STEW), "Mushrooms and a flower make suspicious stew, not mushroom stew");
        helper.assertTrue(serving.hasTag() && serving.getTag().contains(SuspiciousStewItem.EFFECTS_TAG), "It carries the flower's effect");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void soupIsLadledOrEatenFromTheCauldron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos cauldron = placeCauldronOverCampfire(helper);
        CauldronPotHost host = new CauldronPotHost(level, cauldron);
        BoilingPot pot = CauldronPots.of(level).track(cauldron);
        pot.boilNow(host);
        addAll(helper, pot, host, new ItemStack(Items.BONE, 3));
        helper.assertTrue(CauldronInteractions.isBottleRefused(level, cauldron, new ItemStack(Items.GLASS_BOTTLE)), "A soup's ingredients refuse bottles");
        int ticks = FoodServerConfig.get().soup.settleTicks.get() + FoodServerConfig.get().soup.cookTicks.get();
        for (int tick = 0; tick < ticks; tick++) {
            CauldronPots.of(level).tickPot(level, cauldron, false);
        }
        helper.assertTrue(pot.isSoupReady(), "The broth is ready");
        ServerPlayer player = MockPlayers.joinWorld(level);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 2));
            helper.assertTrue(useCauldron(helper, cauldron, player) == InteractionResult.CONSUME, "A bowl ladles a serving");
            helper.assertTrue(player.getInventory().countItem(FoodItems.fixedFood(FixedFood.BONE_BROTH)) == 1, "The player gets Bone Broth");
            helper.assertTrue(player.getInventory().countItem(Items.BOWL) == 1, "One bowl is used");
            player.getInventory().clearContent();
            player.getFoodData().setFoodLevel(HUNGRY);
            helper.assertTrue(useCauldron(helper, cauldron, player) == InteractionResult.CONSUME, "An empty hand eats a serving from the pot");
            helper.assertTrue(player.getFoodData().getFoodLevel() == HUNGRY + 3, "The serving fills like a bowl of Bone Broth");
            helper.assertTrue(player.getInventory().countItem(Items.BOWL) == 0, "Eating from the pot needs no bowl and leaves none");
            helper.assertTrue(level.getBlockState(cauldron).getValue(LayeredCauldronBlock.LEVEL) == FULL - 1, "Two servings used one level of water");
            helper.assertFalse(CauldronInteractions.isBottleRefused(level, cauldron, new ItemStack(Items.GLASS_BOTTLE)), "With the soup gone, bottles fill again");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void gridStewsAreOff(GameTestHelper helper) {
        for (Item stew : List.of(Items.MUSHROOM_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW)) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.itemId(stew)).isEmpty(),
                    ResourceIds.itemId(stew) + "'s grid recipe should be off");
        }
        long turnedOffByFood = RecipeAudit.audit(helper.getLevel().getServer().getResourceManager(), helper.getLevel().getRecipeManager()).stream()
                .filter(entry -> entry.overridingMods().contains(FoodMod.MOD_ID) && entry.isTurnedOff()).count();
        helper.assertTrue(turnedOffByFood == 11, "Food turns off 11 vanilla recipes: 2 seeds, 5 stews, bread, cake, cookies and pie (found "
                + turnedOffByFood + ")");
        helper.assertTrue(new ItemStack(FoodItems.fixedFood(FixedFood.HEARTY_STEW)).is(FoodTags.SOUPS), "Hearty Stew is in #impactsfood:soups");
        helper.assertFalse(new ItemStack(Items.MUSHROOM_STEW).is(FoodTags.SOUPS), "Vanilla's stews aren't");
        helper.assertTrue(new ItemStack(FoodItems.fixedFood(FixedFood.FISH_SOUP)).getMaxStackSize() == 1, "Soups stack to 1");
        helper.succeed();
    }

    private static StandInPotHost boilingStandIn(GameTestHelper helper) {
        StandInPotHost host = new StandInPotHost(helper.getLevel(), helper.absolutePos(CAULDRON), 0);
        host.setHeated(true);
        return host;
    }

    private static void addAll(GameTestHelper helper, BoilingPot pot, PotHost host, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            for (int item = 0; item < stack.getCount(); item++) {
                helper.assertTrue(pot.tryAdd(host, stack), "The pot should take " + ResourceIds.itemId(stack.getItem()));
            }
        }
    }

    private static void settle(BoilingPot pot, StandInPotHost host) {
        tick(pot, host, FoodServerConfig.get().soup.settleTicks.get());
    }

    private static void tick(BoilingPot pot, StandInPotHost host, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            pot.tick(host);
        }
    }

    private static BlockPos placeCauldronOverCampfire(GameTestHelper helper) {
        helper.setBlock(HEAT_SOURCE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.setBlock(CAULDRON, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, FULL));
        return helper.absolutePos(CAULDRON);
    }

    private static InteractionResult useCauldron(GameTestHelper helper, BlockPos cauldron, ServerPlayer player) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(cauldron), Direction.UP, cauldron, false);
        return helper.getLevel().getBlockState(cauldron).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
    }
}
