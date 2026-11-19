package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.advancement.FoodAchievements;
import com.impact.realisticfood.advancement.FoodAdvancements;
import com.impact.realisticfood.advancement.FoodTriggers;
import com.impact.realisticfood.boiling.CauldronPotHost;
import com.impact.realisticfood.boiling.CauldronPots;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Food milestone 12: the 26 achievements load, and the world events behind them grant them. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodAchievementGameTests {
    private static final int SPEC_ACHIEVEMENTS = 26;
    private static final String RECIPE_UNLOCKS_FOLDER = "recipes/";

    private FoodAchievementGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void theTabHasEveryAchievement(GameTestHelper helper) {
        long loaded = helper.getLevel().getServer().getAdvancements().getAllAdvancements().stream()
                .filter(advancement -> advancement.getId().getNamespace().equals(FoodMod.MOD_ID))
                .filter(advancement -> !advancement.getId().getPath().startsWith(RECIPE_UNLOCKS_FOLDER))
                .count();
        boolean hasCrafting = ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID);
        long expected = hasCrafting ? SPEC_ACHIEVEMENTS : SPEC_ACHIEVEMENTS - FoodAdvancements.NEEDS_CRAFTING.size();
        helper.assertTrue(FoodAdvancements.all().size() == SPEC_ACHIEVEMENTS, "Food defines 26 achievements");
        helper.assertTrue(loaded == expected, "All of them load (Stone Soup only with Crafting): expected " + expected + ", found " + loaded);
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void eatingCountsTowardTheChallenges(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ItemStack fireRoastedBeef = new ItemStack(FoodItems.styled(FoodBase.COOKED_BEEF, FoodStyle.TOASTED).orElseThrow());
            ConfigOverrides.withValue(FoodServerConfig.get().counters.dayInAndDayOutEats, 2, () -> {
                FoodAchievements.onEaten(player, fireRoastedBeef);
                helper.assertFalse(isDone(helper, player, FoodAdvancements.DAY_IN_AND_DAY_OUT), "Not after one");
                FoodAchievements.onEaten(player, fireRoastedBeef);
                helper.assertTrue(isDone(helper, player, FoodAdvancements.DAY_IN_AND_DAY_OUT), "Eating the same toasted food enough times grants it");
            });
            ConfigOverrides.withValue(FoodServerConfig.get().counters.tasteTesterFoods, 2, () -> {
                FoodAchievements.onEaten(player, new ItemStack(FoodItems.styled(FoodBase.APPLE, FoodStyle.DRIED).orElseThrow()));
                helper.assertTrue(isDone(helper, player, FoodAdvancements.TASTE_TESTER), "Two different styled foods grant Taste Tester at 2");
            });
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void soupsAndBoilingGrantTheirAchievements(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            FoodTriggers.SOUP_LADLED.trigger(player, new ItemStack(FoodItems.fixedFood(FixedFood.WATERY_SOUP)));
            helper.assertTrue(isDone(helper, player, FoodAdvancements.SOUPER), "Ladling any soup grants Souper");
            helper.assertTrue(isDone(helper, player, FoodAdvancements.WATERED_DOWN), "Watery Soup grants Watered Down");
            helper.assertFalse(isDone(helper, player, FoodAdvancements.MASTER_CHEF), "Master Chef needs every soup");
            BlockPos cauldron = helper.absolutePos(BlockPos.ZERO.above());
            helper.setBlock(BlockPos.ZERO, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
            helper.setBlock(BlockPos.ZERO.above(), Blocks.WATER_CAULDRON);
            player.teleportTo(cauldron.getX() + 0.5, cauldron.getY() + 1.0, cauldron.getZ() + 0.5);
            CauldronPots.of(helper.getLevel()).track(cauldron).boilNow(new CauldronPotHost(helper.getLevel(), cauldron));
            helper.assertTrue(isDone(helper, player, FoodAdvancements.HOT_WATER), "Seeing a cauldron come to the boil grants Hot Water");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void everythingElseMakesYouWellFed(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            for (ResourceLocation required : FoodAdvancements.wellFedRequirements()) {
                Advancement advancement = helper.getLevel().getServer().getAdvancements().getAdvancement(required);
                if (advancement != null) {
                    advancement.getCriteria().keySet().forEach(criterion -> player.getAdvancements().award(advancement, criterion));
                }
            }
            FoodAchievements.checkWellFed(player);
            helper.assertTrue(isDone(helper, player, FoodAdvancements.WELL_FED), "Every other achievement grants Well Fed");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static boolean isDone(GameTestHelper helper, ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = helper.getLevel().getServer().getAdvancements().getAdvancement(advancementId);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
