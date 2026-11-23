package com.impact.realisticfood.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.spit.Spit;
import com.impact.realisticfood.spit.SpitHolder;
import com.impact.realisticfood.spit.Spits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Food milestone 7: mounting a spit, its 6 slots, cooking speed, turning, taking it down, and the campfire going out or breaking. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodSpitGameTests {
    private static final BlockPos CAMPFIRE = BlockPos.ZERO;
    private static final int SPEC_SLOTS = 6;
    private static final int VANILLA_CAMPFIRE_BEEF_TICKS = 600;
    private static final int SPEC_SPIT_BEEF_TICKS = 480;
    private static final int TURNED_BEEF_TICKS = 400;

    private FoodSpitGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aSpitHoldsSixOnTopOfTheCampfire(GameTestHelper helper) {
        CampfireBlockEntity campfire = litCampfire(helper);
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Spits.spitItem()));
            helper.assertTrue(useCampfire(helper, player) == InteractionResult.CONSUME, "A spit mounts on a lit campfire");
            helper.assertTrue(SpitHolder.spitOf(campfire).isPresent() && player.getMainHandItem().isEmpty(), "The spit moves onto the campfire");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF, SPEC_SLOTS + 1));
            for (int item = 0; item <= SPEC_SLOTS; item++) {
                useCampfire(helper, player);
            }
            long onSpit = SpitHolder.spitOf(campfire).orElseThrow().items().stream().filter(stack -> !stack.isEmpty()).count();
            long onCampfire = campfire.getItems().stream().filter(stack -> !stack.isEmpty()).count();
            helper.assertTrue(onSpit == SPEC_SLOTS, "The spit holds 6 (found " + onSpit + ")");
            helper.assertTrue(onCampfire == 1, "The 7th goes onto the campfire itself (found " + onCampfire + ")");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void spitFoodCooksFasterAndFasterWhenTurned(GameTestHelper helper) {
        StrayItems.clearAround(helper, CAMPFIRE);
        CampfireBlockEntity campfire = litCampfire(helper);
        Spit spit = mountSpit(campfire);
        BlockPos position = helper.absolutePos(CAMPFIRE);
        helper.assertTrue(spit.addFood(helper.getLevel(), position, new ItemStack(Items.BEEF)), "Raw beef goes on the spit");
        cook(helper, campfire, SPEC_SPIT_BEEF_TICKS - 1);
        helper.assertItemEntityNotPresent(Items.COOKED_BEEF, CAMPFIRE, 2.0);
        cook(helper, campfire, 1);
        helper.assertItemEntityPresent(Items.COOKED_BEEF, CAMPFIRE, 2.0);
        helper.assertTrue(SPEC_SPIT_BEEF_TICKS < VANILLA_CAMPFIRE_BEEF_TICKS, "A 600-tick item takes 480 on the spit");
        helper.assertTrue(spit.addFood(helper.getLevel(), position, new ItemStack(Items.MUTTON)), "Raw mutton goes on");
        helper.assertTrue(spit.turn(helper.getLevel(), position), "The spit turns");
        helper.assertFalse(spit.turn(helper.getLevel(), position), "Turning again does nothing while it's still turning");
        // Game time stands still within a test's tick, so the spit stays turned (1.5 times campfire speed) throughout.
        cook(helper, campfire, TURNED_BEEF_TICKS - 1);
        helper.assertItemEntityNotPresent(Items.COOKED_MUTTON, CAMPFIRE, 2.0);
        cook(helper, campfire, 1);
        helper.assertItemEntityPresent(Items.COOKED_MUTTON, CAMPFIRE, 2.0);
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void sneakingTakesTheSpitDown(GameTestHelper helper) {
        CampfireBlockEntity campfire = litCampfire(helper);
        Spit spit = mountSpit(campfire);
        spit.addFood(helper.getLevel(), helper.absolutePos(CAMPFIRE), new ItemStack(Items.CHICKEN));
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setShiftKeyDown(true);
            helper.assertTrue(useCampfire(helper, player) == InteractionResult.CONSUME, "Sneaking with an empty hand takes the spit down");
            helper.assertTrue(SpitHolder.spitOf(campfire).isEmpty(), "The spit is gone from the campfire");
            helper.assertItemEntityPresent(Spits.spitItem(), CAMPFIRE, 2.0);
            helper.assertItemEntityPresent(Items.CHICKEN, CAMPFIRE, 2.0);
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void theSpitStaysOutAndDropsWhenBroken(GameTestHelper helper) {
        CampfireBlockEntity campfire = litCampfire(helper);
        mountSpit(campfire);
        helper.setBlock(CAMPFIRE, helper.getBlockState(CAMPFIRE).setValue(CampfireBlock.LIT, false));
        CampfireBlockEntity outCampfire = (CampfireBlockEntity) helper.getBlockEntity(CAMPFIRE);
        helper.assertTrue(SpitHolder.spitOf(outCampfire).isPresent(), "The spit stays when the campfire goes out");
        helper.setBlock(CAMPFIRE, Blocks.AIR);
        helper.assertItemEntityPresent(Spits.spitItem(), CAMPFIRE, 2.0);
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void theSpitIsSavedAndSynced(GameTestHelper helper) {
        CampfireBlockEntity campfire = litCampfire(helper);
        Spit spit = mountSpit(campfire);
        spit.addFood(helper.getLevel(), helper.absolutePos(CAMPFIRE), new ItemStack(Items.BEEF));
        CampfireBlockEntity reloaded = new CampfireBlockEntity(helper.absolutePos(CAMPFIRE), campfire.getBlockState());
        reloaded.load(campfire.saveWithoutMetadata());
        helper.assertTrue(SpitHolder.spitOf(reloaded).map(saved -> saved.items().get(0).is(Items.BEEF)).orElse(false),
                "A saved campfire keeps its spit and the food on it");
        CampfireBlockEntity synced = new CampfireBlockEntity(helper.absolutePos(CAMPFIRE), campfire.getBlockState());
        synced.load(campfire.getUpdateTag());
        helper.assertTrue(SpitHolder.spitOf(synced).isPresent(), "Clients get the spit with the campfire's update");
        helper.succeed();
    }

    private static CampfireBlockEntity litCampfire(GameTestHelper helper) {
        helper.setBlock(CAMPFIRE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        return (CampfireBlockEntity) helper.getBlockEntity(CAMPFIRE);
    }

    private static Spit mountSpit(CampfireBlockEntity campfire) {
        Spit spit = new Spit(new ItemStack(Spits.spitItem()), SPEC_SLOTS);
        ((SpitHolder) campfire).impactsfood$setSpit(spit);
        return spit;
    }

    private static void cook(GameTestHelper helper, CampfireBlockEntity campfire, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            Spits.cookTick(helper.getLevel(), helper.absolutePos(CAMPFIRE), campfire.getBlockState(), campfire);
        }
    }

    private static InteractionResult useCampfire(GameTestHelper helper, ServerPlayer player) {
        BlockPos campfire = helper.absolutePos(CAMPFIRE);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(campfire), Direction.UP, campfire, false);
        return helper.getLevel().getBlockState(campfire).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
    }
}
