package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.advancement.VitalityAchievements;
import com.impact.realisticvitality.advancement.VitalityAdvancements;
import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.air.AirTicks;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.temperature.DryFoods;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.Drinking;
import net.minecraft.advancements.Advancement;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

/** Vitality milestone 11: the 22 achievements load, and the triggers behind them fire from Vitality's systems. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityAchievementGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final int SPEC_ACHIEVEMENTS = 22;
    private static final String RECIPE_UNLOCKS_FOLDER = "recipes/";

    private VitalityAchievementGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theTabHasEveryAchievement(GameTestHelper helper) {
        long loaded = helper.getLevel().getServer().getAdvancements().getAllAdvancements().stream()
                .filter(advancement -> advancement.getId().getNamespace().equals(VitalityMod.MOD_ID))
                .filter(advancement -> !advancement.getId().getPath().startsWith(RECIPE_UNLOCKS_FOLDER))
                .count();
        helper.assertTrue(VitalityAdvancements.all().size() == SPEC_ACHIEVEMENTS, "Vitality defines 22 achievements");
        helper.assertTrue(loaded == SPEC_ACHIEVEMENTS, "All of them load, found " + loaded);
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void drinkingGrantsTheThirstBranch(GameTestHelper helper) {
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().thirst.parchedChance, 1.0, () -> {
            Drinking.onUseFinished(player, VitalityItems.bottle(BottledWater.PURIFIED));
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.STAY_HYDRATED), "Any water grants Stay Hydrated");
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.CLEAN_LIVING), "Purified Water grants Clean Living");
            helper.assertFalse(isDone(helper, player, VitalityAdvancements.WORTH_ITS_SALT), "but not Worth Its Salt");
            Drinking.onUseFinished(player, VitalityItems.bottle(BottledWater.SALT));
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.WORTH_ITS_SALT), "Salt water grants Worth Its Salt");
            Drinking.onUseFinished(player, VitalityItems.bottle(BottledWater.UNCLEAN));
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.SOMETHING_IN_THE_WATER), "Getting Parched grants Something in the Water");
        }));
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void wellWateredCountsDrinks(GameTestHelper helper) {
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().counters.wellWateredDrinks, 2, () -> {
            Drinking.onUseFinished(player, new ItemStack(Items.MILK_BUCKET));
            helper.assertFalse(isDone(helper, player, VitalityAdvancements.WELL_WATERED), "Not after one drink");
            Drinking.onUseFinished(player, new ItemStack(Items.HONEY_BOTTLE));
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.WELL_WATERED), "Every drink counts toward Well Watered");
        }));
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void temperatureGrantsItsBranch(GameTestHelper helper) {
        withPlayer(helper, player -> {
            VitalityState.of(player).temperature.setBody(-60.0F);
            VitalityAchievements.onSecond(player);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.COLD_FEET), "Being Cold grants Cold Feet");
            helper.assertFalse(isDone(helper, player, VitalityAdvancements.ALBERT_AND_EINSTEIN), "Not on fire yet");
            player.setRemainingFireTicks(100);
            VitalityAchievements.onSecond(player);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.ALBERT_AND_EINSTEIN), "On fire while Cold grants Albert and Einstein");
            VitalityState.of(player).temperature.setBody(100.0F);
            VitalityAchievements.onSecond(player);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.SO_HOT), "Overheating grants It's getting hot in here!");
            DryFoods.onRefused(player, new ItemStack(Items.BREAD));
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.DRY_SPELL), "A refused dry food grants Dry Spell");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bundleUpNeedsFourWarmLinings(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.setItemSlot(EquipmentSlot.HEAD, ArmorLining.WOOL.sewnInto(new ItemStack(Items.LEATHER_HELMET)));
            player.setItemSlot(EquipmentSlot.CHEST, ArmorLining.FUR.sewnInto(new ItemStack(Items.IRON_CHESTPLATE)));
            player.setItemSlot(EquipmentSlot.LEGS, ArmorLining.WOOL.sewnInto(new ItemStack(Items.IRON_LEGGINGS)));
            player.setItemSlot(EquipmentSlot.FEET, ArmorLining.LEATHER.sewnInto(new ItemStack(Items.IRON_BOOTS)));
            VitalityAchievements.onSecond(player);
            helper.assertFalse(isDone(helper, player, VitalityAdvancements.BUNDLE_UP), "A Leather Lining doesn't keep you warm");
            player.setItemSlot(EquipmentSlot.FEET, ArmorLining.WOOL.sewnInto(new ItemStack(Items.IRON_BOOTS)));
            VitalityAchievements.onSecond(player);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.BUNDLE_UP), "Four Wool or Fur Linings grant Bundle Up");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void inBalanceNeedsAnUnbrokenDay(GameTestHelper helper) {
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().counters.inBalanceSeconds, 2, () -> {
            VitalityAchievements.onSecond(player);
            VitalityState.of(player).thirst.setLevel(4);
            VitalityAchievements.onSecond(player);
            VitalityState.of(player).thirst.setLevel(20);
            VitalityAchievements.onSecond(player);
            helper.assertFalse(isDone(helper, player, VitalityAdvancements.IN_BALANCE), "Low thirst breaks the streak");
            VitalityAchievements.onSecond(player);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.IN_BALANCE), "Two seconds in a row in balance grant it at 2");
        }));
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void airLossGrantsByPlaceAndCause(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.moveTo(player.getX(), 170.0, player.getZ());
            loseABubble(player, AirQuality.THIN);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.BREATHTAKING), "Thin air at Y 170 grants Breathtaking");
            loseABubble(player, AirQuality.SMOKY);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.UP_IN_SMOKE), "Fumes grant Up in Smoke");
            helper.assertFalse(isDone(helper, player, VitalityAdvancements.IS_IT_WORTH_IT), "Not near bedrock");
            player.moveTo(player.getX(), -60.0, player.getZ());
            loseABubble(player, AirQuality.THIN);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.IS_IT_WORTH_IT), "Thin air at Y −60 without gear grants Is it worth it?");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void peakConditionUpHigh(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.moveTo(player.getX(), 310.0, player.getZ());
            VitalityAchievements.onSecond(player);
            helper.assertTrue(isDone(helper, player, VitalityAdvancements.PEAK_CONDITION), "Full and Comfortable above Y 300 grants Peak Condition");
        });
    }

    // Drains one bubble's worth of ticks in the given air, without the once-a-second reading of the test spot's real air.
    private static void loseABubble(ServerPlayer player, AirQuality quality) {
        VitalityState.of(player).air.setQuality(quality);
        player.setAirSupply(player.getMaxAirSupply());
        for (int tick = 0; tick < quality.bubbleTicks().orElseThrow(); tick++) {
            player.tickCount = 1 - player.getId();
            AirTicks.tick(player);
        }
    }

    private static boolean isDone(GameTestHelper helper, ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = helper.getLevel().getServer().getAdvancements().getAdvancement(advancementId);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static void withPlayer(GameTestHelper helper, Consumer<ServerPlayer> test) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            test.accept(player);
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
