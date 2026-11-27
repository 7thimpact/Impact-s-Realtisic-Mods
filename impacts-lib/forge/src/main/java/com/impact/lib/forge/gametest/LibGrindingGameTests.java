package com.impact.lib.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.grinding.BowlContents;
import com.impact.lib.grinding.GrinderTier;
import com.impact.lib.grinding.Grinding;
import com.impact.lib.grinding.GrindingRecipe;
import com.impact.lib.hold.HoldAction;
import com.impact.lib.hold.HoldMotion;
import com.impact.lib.hold.HoldToUse;
import com.impact.lib.hold.WorkMotions;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** Lib's food slice: grinder bowls, grinding recipes, hold-to-use cycles, and the work motions mods show. */
@GameTestHolder(ImpactsLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibGrindingGameTests {
    private static final String EMPTY_TEMPLATE = "empty";
    private static final int BOWL_CAPACITY = 16;
    private static final int TEST_CYCLE_TICKS = 2;

    private LibGrindingGameTests() {
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void bowlsHoldSixteenItemsOfAtMostTwoKinds(GameTestHelper helper) {
        ItemStack bowl = new ItemStack(Grinding.woodenBowl());
        ItemStack poppies = new ItemStack(Items.POPPY, 20);
        helper.assertTrue(BowlContents.load(bowl, poppies) == BOWL_CAPACITY, "A bowl takes 16 items");
        helper.assertTrue(poppies.getCount() == 20 - BOWL_CAPACITY, "The rest stays on the cursor");
        ItemStack mixingBowl = new ItemStack(Grinding.woodenBowl());
        BowlContents.load(mixingBowl, new ItemStack(Items.RED_DYE, 2));
        BowlContents.load(mixingBowl, new ItemStack(Items.WHITE_DYE, 2));
        helper.assertTrue(BowlContents.load(mixingBowl, new ItemStack(Items.BLUE_DYE)) == 0, "A third kind doesn't fit");
        BowlContents.consumeOneOfEach(mixingBowl);
        List<ItemStack> left = BowlContents.read(mixingBowl);
        helper.assertTrue(left.size() == 2 && left.stream().allMatch(stack -> stack.getCount() == 1), "Grinding takes one of each kind");
        helper.assertTrue(new ItemStack(Grinding.stoneStick()).getMaxDamage() == GrinderTier.STONE.durability(), "Stone grinders use the configured durability");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void grindingRecipesMatchKindsInAnyOrder(GameTestHelper helper) {
        GrindingRecipe mixing = new GrindingRecipe(ResourceIds.of(ImpactsLib.MOD_ID, "test_mix"),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.RED_DYE), Ingredient.of(Items.WHITE_DYE)), new ItemStack(Items.PINK_DYE, 2),
                GrinderTier.WOOD, GrindingRecipe.STICK_DECIDES_TICKS, List.of());
        helper.assertTrue(mixing.matches(new SimpleContainer(new ItemStack(Items.WHITE_DYE), new ItemStack(Items.RED_DYE)), helper.getLevel()),
                "Two dyes mix in either order");
        helper.assertFalse(mixing.matches(new SimpleContainer(new ItemStack(Items.RED_DYE)), helper.getLevel()), "One dye alone isn't the mix");
        helper.assertFalse(GrinderTier.WOOD.reaches(GrinderTier.STONE), "A wooden bowl can't grind stone recipes");
        helper.assertTrue(GrinderTier.STONE.reaches(GrinderTier.WOOD), "A stone bowl grinds wooden recipes");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void holdingFinishesOneCycleAtATime(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        AtomicInteger finishedCycles = new AtomicInteger();
        try {
            HoldToUse.register(Grinding.woodenStick(), new CountingAction(finishedCycles));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Grinding.woodenStick()));
            HoldToUse.start(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.isUsingItem(), "Right-click starts holding");
            ItemStack stick = player.getMainHandItem();
            HoldToUse.tick(helper.getLevel(), player, stick);
            helper.assertTrue(finishedCycles.get() == 0, "Nothing happens before the cycle is done");
            HoldToUse.tick(helper.getLevel(), player, stick);
            helper.assertTrue(finishedCycles.get() == 1, "A full cycle does the action once");
            HoldToUse.tick(helper.getLevel(), player, stick);
            HoldToUse.release(helper.getLevel(), player);
            HoldToUse.tick(helper.getLevel(), player, stick);
            helper.assertTrue(finishedCycles.get() == 1, "Letting go loses the cycle in progress");
        } finally {
            Grinding.registerHoldActions();
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void workMotionsFollowWhatTheServerShows(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            helper.assertTrue(WorkMotions.shownMotion(player).isEmpty(), "A player shows no work motion at first");
            WorkMotions.show(player, Optional.of(HoldMotion.CRAFT));
            WorkMotions.show(player, Optional.of(HoldMotion.CRAFT));
            helper.assertTrue(WorkMotions.shownMotion(player).equals(Optional.of(HoldMotion.CRAFT)), "A mod shows the crafting motion");
            WorkMotions.show(player, Optional.empty());
            helper.assertTrue(WorkMotions.shownMotion(player).isEmpty(), "and stops it");
            WorkMotions.show(player, Optional.of(HoldMotion.TURN));
            WorkMotions.forget(player);
            helper.assertTrue(WorkMotions.shownMotion(player).isEmpty(), "A player who leaves is forgotten");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private record CountingAction(AtomicInteger finishedCycles) implements HoldAction {
        @Override
        public ResourceLocation id() {
            return ResourceIds.of(ImpactsLib.MOD_ID, "test_counting");
        }

        @Override
        public int cycleTicks(Player player, ItemStack held) {
            return TEST_CYCLE_TICKS;
        }

        @Override
        public boolean canContinue(Player player, ItemStack held) {
            return true;
        }

        @Override
        public boolean completeCycle(ServerPlayer player, ItemStack held) {
            finishedCycles.incrementAndGet();
            return true;
        }
    }
}
