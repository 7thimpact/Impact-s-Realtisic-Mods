package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

/** Vitality milestone 4: hunger and thirst together decide how far natural regeneration heals. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityHealthCapGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final float STARTING_HEALTH = 5.0F;
    private static final int REGENERATION_TICKS = 3000;
    private static final float TOLERANCE = 0.001F;

    private VitalityHealthCapGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void lowHungerAndFullWaterHealsToHalf(GameTestHelper helper) {
        withPlayer(helper, player -> {
            float health = regenerate(player, 6, 0.0F, 20, 0.0F);
            helper.assertTrue(isClose(health, 10.0F), "3 bars of hunger and full water should heal to 10, got " + health);
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void fullHungerAndEmptyWaterHealsToHalf(GameTestHelper helper) {
        withPlayer(helper, player -> {
            float health = regenerate(player, 20, 5.0F, 0, 0.0F);
            helper.assertTrue(isClose(health, 10.0F), "Full hunger and empty water should heal to 10, got " + health);
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void middlingThirstHealsToThreeQuarters(GameTestHelper helper) {
        withPlayer(helper, player -> {
            float health = regenerate(player, 20, 0.0F, 12, 0.0F);
            helper.assertTrue(isClose(health, 15.0F), "Full hunger and 12 thirst should heal to 15, got " + health);
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bothFullHealsToFull(GameTestHelper helper) {
        withPlayer(helper, player -> {
            float health = regenerate(player, 20, 5.0F, 20, 5.0F);
            helper.assertTrue(isClose(health, 20.0F), "Full on both should heal to 20, got " + health);
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bothBarsBelowEighteenStopRegeneration(GameTestHelper helper) {
        withPlayer(helper, player -> {
            float health = regenerate(player, 17, 0.0F, 17, 0.0F);
            helper.assertTrue(isClose(health, STARTING_HEALTH), "With neither bar at 18, nothing regenerates, got " + health);
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theCapNeverTakesHealthAway(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.setHealth(18.0F);
            player.getFoodData().setFoodLevel(6);
            for (int tick = 0; tick < REGENERATION_TICKS; tick++) {
                player.getFoodData().tick(player);
            }
            helper.assertTrue(isClose(player.getHealth(), 18.0F), "Health above the cap stays where it is");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void regenerationExhaustionIsSplit(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.setHealth(STARTING_HEALTH);
            FoodData food = player.getFoodData();
            food.setFoodLevel(20);
            food.setSaturation(0.0F);
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(20);
            thirst.setHydration(0.0F);
            for (int tick = 0; tick < 80; tick++) {
                food.tick(player);
            }
            helper.assertTrue(isClose(food.getExhaustionLevel(), 3.0F), "Half of a heal's 6 exhaustion goes to hunger, got " + food.getExhaustionLevel());
            helper.assertTrue(isClose(thirst.exhaustion(), 3.0F), "and half to thirst, got " + thirst.exhaustion());
            helper.assertTrue(isClose(player.getHealth(), STARTING_HEALTH + 1.0F), "One slow heal");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void healingPotionsIgnoreTheCap(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.setHealth(STARTING_HEALTH);
            player.getFoodData().setFoodLevel(6);
            player.heal(10.0F);
            helper.assertTrue(isClose(player.getHealth(), 15.0F), "A heal from a potion goes past the cap");
            player.setHealth(STARTING_HEALTH);
            ConfigOverrides.withValue(VitalityServerConfig.get().healthCap.capsAllHealing, true, () -> player.heal(10.0F));
            helper.assertTrue(isClose(player.getHealth(), 10.0F), "With capAllHealing on, it stops at the cap");
        });
    }

    // Keeps both bars where the test puts them, as if the player kept eating and drinking, and lets regeneration run.
    private static float regenerate(ServerPlayer player, int hunger, float saturation, int thirstLevel, float hydration) {
        player.setHealth(STARTING_HEALTH);
        ThirstState thirst = VitalityState.of(player).thirst;
        FoodData food = player.getFoodData();
        for (int tick = 0; tick < REGENERATION_TICKS; tick++) {
            food.setFoodLevel(hunger);
            food.setSaturation(saturation);
            thirst.setLevel(thirstLevel);
            thirst.setHydration(hydration);
            food.tick(player);
        }
        return player.getHealth();
    }

    private static boolean isClose(float actual, float expected) {
        return Math.abs(actual - expected) < TOLERANCE;
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
