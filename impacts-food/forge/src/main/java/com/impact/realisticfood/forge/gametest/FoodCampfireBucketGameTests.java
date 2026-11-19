package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.bucket.BucketPotHost;
import com.impact.realisticfood.bucket.CampfireBucketBlock;
import com.impact.realisticfood.bucket.CampfireBucketBlockEntity;
import com.impact.realisticfood.bucket.CampfireBuckets;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Food milestone 6: setting a water bucket on a campfire, its boils, taking it back, and popping off. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodCampfireBucketGameTests {
    private static final BlockPos CAMPFIRE = BlockPos.ZERO;
    private static final BlockPos BUCKET = CAMPFIRE.above();
    private static final int SPEC_BOILS = 8;

    private FoodCampfireBucketGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aWaterBucketSitsOnALitCampfire(GameTestHelper helper) {
        helper.setBlock(CAMPFIRE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            helper.assertTrue(place(helper, player) == InteractionResult.PASS, "Without sneaking, the water bucket does its vanilla thing");
            player.setShiftKeyDown(true);
            helper.assertTrue(place(helper, player) == InteractionResult.CONSUME, "Sneaking sets the bucket down on the campfire");
            helper.assertBlockPresent(CampfireBuckets.block(), BUCKET);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "The bucket leaves the hand");
            helper.assertTrue(useBucket(helper, player) == InteractionResult.CONSUME, "Sneaking with an empty hand takes it back");
            helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET), "It's still a water bucket");
            helper.assertBlockNotPresent(CampfireBuckets.block(), BUCKET);
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.setBlock(CAMPFIRE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        ServerPlayer secondPlayer = MockPlayers.joinWorld(helper.getLevel());
        try {
            secondPlayer.setShiftKeyDown(true);
            secondPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            helper.assertTrue(place(helper, secondPlayer) == InteractionResult.PASS, "An unlit campfire takes no bucket");
        } finally {
            MockPlayers.leaveWorld(secondPlayer);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void theWaterLastsEightBoils(GameTestHelper helper) {
        CampfireBucketBlockEntity bucket = setBucketDown(helper);
        ServerLevel level = helper.getLevel();
        BucketPotHost host = new BucketPotHost(level, helper.absolutePos(BUCKET), bucket);
        bucket.pot().boilNow(host);
        helper.assertTrue(ImpactsLib.isBoiling(level, helper.absolutePos(BUCKET)), "Lib hears that the bucket boils");
        helper.assertTrue(bucket.boilsLeft() == SPEC_BOILS, "A bucket's water lasts 8 boils");
        ConfigOverrides.withValue(FoodServerConfig.get().boiling.boilTickOverrides, List.of("minecraft:egg=1"), () -> {
            for (int boil = 0; boil < SPEC_BOILS; boil++) {
                helper.assertTrue(bucket.pot().tryAdd(host, new ItemStack(Items.EGG)), "Boil " + (boil + 1) + " takes an egg");
                bucket.pot().tick(host);
            }
        });
        helper.assertFalse(bucket.hasWater(), "After 8 boils the water is gone");
        helper.assertFalse(level.getBlockState(helper.absolutePos(BUCKET)).getValue(CampfireBucketBlock.HAS_WATER), "The bucket shows empty");
        helper.assertFalse(bucket.pot().tryAdd(host, new ItemStack(Items.EGG)), "An empty bucket boils nothing");
        helper.assertTrue(bucket.bucketToReturn().is(Items.BUCKET), "It comes back as an empty bucket, with no wear");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void theBucketPopsOffWhenTheCampfireGoesOut(GameTestHelper helper) {
        CampfireBucketBlockEntity bucket = setBucketDown(helper);
        BucketPotHost host = new BucketPotHost(helper.getLevel(), helper.absolutePos(BUCKET), bucket);
        bucket.pot().boilNow(host);
        bucket.pot().tryAdd(host, new ItemStack(Items.EGG));
        helper.setBlock(CAMPFIRE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        helper.assertBlockNotPresent(CampfireBuckets.block(), BUCKET);
        helper.assertItemEntityPresent(Items.WATER_BUCKET, BUCKET, 2.0);
        helper.assertItemEntityPresent(Items.EGG, BUCKET, 2.0);
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aBucketHoldsLessThanACauldron(GameTestHelper helper) {
        CampfireBucketBlockEntity bucket = setBucketDown(helper);
        BucketPotHost host = new BucketPotHost(helper.getLevel(), helper.absolutePos(BUCKET), bucket);
        bucket.pot().boilNow(host);
        helper.assertTrue(bucket.pot().tryAdd(host, new ItemStack(Items.EGG)) && bucket.pot().tryAdd(host, new ItemStack(Items.EGG)),
                "A bucket boils 2 eggs");
        helper.assertFalse(bucket.pot().tryAdd(host, new ItemStack(Items.EGG)), "But not 3");
        CampfireBucketBlockEntity soupBucket = bucket;
        soupBucket.pot().takeAllInputs();
        for (int beetroot = 0; beetroot < 4; beetroot++) {
            soupBucket.pot().tryAdd(host, new ItemStack(Items.BEETROOT));
        }
        for (int tick = 0; tick < FoodServerConfig.get().soup.settleTicks.get(); tick++) {
            soupBucket.pot().tick(host);
        }
        helper.assertTrue(soupBucket.pot().soup().orElseThrow().servings() == 2, "A bucket's soup makes 2 servings at most");
        helper.succeed();
    }

    private static CampfireBucketBlockEntity setBucketDown(GameTestHelper helper) {
        helper.setBlock(CAMPFIRE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.setBlock(BUCKET, CampfireBuckets.block().defaultBlockState());
        return (CampfireBucketBlockEntity) helper.getBlockEntity(BUCKET);
    }

    private static InteractionResult place(GameTestHelper helper, ServerPlayer player) {
        return CampfireBuckets.tryPlace(helper.getLevel(), helper.absolutePos(CAMPFIRE), Direction.UP, player, InteractionHand.MAIN_HAND);
    }

    private static InteractionResult useBucket(GameTestHelper helper, ServerPlayer player) {
        BlockPos bucket = helper.absolutePos(BUCKET);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(bucket), Direction.UP, bucket, false);
        return helper.getLevel().getBlockState(bucket).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
    }
}
