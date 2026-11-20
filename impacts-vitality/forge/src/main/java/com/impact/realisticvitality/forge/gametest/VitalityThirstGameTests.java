package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.network.VitalityStateMessage;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import com.impact.realisticvitality.registry.VitalityEffects;
import com.impact.realisticvitality.thirst.ThirstDrain;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

/** Vitality milestone 2: thirst drains with exertion and at rest, Hydration goes first, and an empty bar dehydrates. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityThirstGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final float SPRINT_JUMP_EXHAUSTION = 0.2F;
    private static final float FULL_HEALTH = 20.0F;
    private static final float TOLERANCE = 0.0001F;

    private VitalityThirstGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void actionsCostThirstAsTheyCostFood(GameTestHelper helper) {
        withPlayer(helper, Difficulty.NORMAL, player -> {
            player.causeFoodExhaustion(SPRINT_JUMP_EXHAUSTION);
            float thirstExhaustion = VitalityState.of(player).thirst.exhaustion();
            helper.assertTrue(isClose(thirstExhaustion, SPRINT_JUMP_EXHAUSTION), "A sprint-jump should cost the same thirst exhaustion as food exhaustion");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void hungerEffectCostsNoThirst(GameTestHelper helper) {
        withPlayer(helper, Difficulty.NORMAL, player -> {
            MobEffects.HUNGER.applyEffectTick(player, 0);
            helper.assertTrue(player.getFoodData().getExhaustionLevel() > 0.0F, "The Hunger effect should still drain food");
            helper.assertTrue(VitalityState.of(player).thirst.exhaustion() == 0.0F, "The Hunger effect isn't an action, so it costs no thirst");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void parchedDrainsThirstLikeHunger(GameTestHelper helper) {
        withPlayer(helper, Difficulty.NORMAL, player -> {
            float foodBefore = player.getFoodData().getExhaustionLevel();
            VitalityEffects.parched().applyEffectTick(player, 1);
            double expected = VitalityServerConfig.get().thirst.parchedExhaustionPerTick.get() * 2;
            helper.assertTrue(isClose(VitalityState.of(player).thirst.exhaustion(), (float) expected), "Parched II should add twice the per-level thirst exhaustion");
            helper.assertTrue(player.getFoodData().getExhaustionLevel() == foodBefore, "Parched doesn't touch hunger");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void hydrationDrainsBeforeThirst(GameTestHelper helper) {
        withPlayer(helper, Difficulty.NORMAL, player -> {
            ThirstState thirst = VitalityState.of(player).thirst;
            float exhaustionPerPoint = VitalityServerConfig.get().thirst.exhaustionPerPoint.get().floatValue();
            thirst.setHydration(1.0F);
            thirst.setExhaustion(exhaustionPerPoint);
            ThirstDrain.tick(player);
            helper.assertTrue(thirst.hydration() == 0.0F && thirst.level() == ThirstState.FULL, "The first point of exhaustion should cost Hydration");
            thirst.setExhaustion(exhaustionPerPoint);
            ThirstDrain.tick(player);
            helper.assertTrue(thirst.level() == ThirstState.FULL - 1, "With Hydration empty, exhaustion should cost thirst");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void thirstDrainsEvenAtRest(GameTestHelper helper) {
        withPlayer(helper, Difficulty.NORMAL, player -> {
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setHydration(0.0F);
            thirst.setRestingTicks(VitalityServerConfig.get().thirst.restingDrainTicks.get() - 1);
            ThirstDrain.tick(player);
            helper.assertTrue(thirst.level() == ThirstState.FULL - 1, "Every 5 minutes at rest should cost 1 point of thirst");
            helper.assertTrue(thirst.restingTicks() == 0, "The resting timer should start over");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void peacefulNeverDrainsAndRefills(GameTestHelper helper) {
        withPlayer(helper, Difficulty.PEACEFUL, player -> {
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(10);
            thirst.setHydration(0.0F);
            thirst.setExhaustion(VitalityServerConfig.get().thirst.exhaustionPerPoint.get().floatValue());
            ThirstDrain.tick(player);
            helper.assertTrue(thirst.level() == 11, "On Peaceful, thirst shouldn't drop and should refill like hunger");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void emptyThirstDehydrates(GameTestHelper helper) {
        withPlayer(helper, Difficulty.NORMAL, player -> {
            MockPlayers.endSpawnProtection(player);
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(0);
            thirst.setDamageTicks(VitalityServerConfig.get().thirst.dehydrationIntervalTicks.get() - 1);
            ThirstDrain.tick(player);
            float expectedHealth = FULL_HEALTH - VitalityServerConfig.get().thirst.dehydrationDamage.get().floatValue();
            helper.assertTrue(isClose(player.getHealth(), expectedHealth), "Empty thirst should deal dehydration damage, got health " + player.getHealth());
            helper.assertTrue(player.getLastDamageSource() != null && player.getLastDamageSource().is(VitalityDamageTypes.DEHYDRATION),
                    "The damage should be impactsvitality:dehydration");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void dehydrationFollowsStarvationRules(GameTestHelper helper) {
        helper.assertTrue(dehydrationLeavesHealth(helper, Difficulty.EASY, 10.5F) < 10.5F, "On Easy, dehydration hurts above 10 health");
        helper.assertTrue(dehydrationLeavesHealth(helper, Difficulty.EASY, 10.0F) == 10.0F, "On Easy, dehydration stops at 10 health");
        helper.assertTrue(dehydrationLeavesHealth(helper, Difficulty.NORMAL, 1.5F) < 1.5F, "On Normal, dehydration hurts above 1 health");
        helper.assertTrue(dehydrationLeavesHealth(helper, Difficulty.NORMAL, 1.0F) == 1.0F, "On Normal, dehydration stops at 1 health");
        helper.assertTrue(dehydrationLeavesHealth(helper, Difficulty.HARD, 1.0F) <= 0.0F, "On Hard, dehydration kills");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void syncMessageCarriesTheBar(GameTestHelper helper) {
        withPlayer(helper, Difficulty.NORMAL, player -> {
            VitalityState state = VitalityState.of(player);
            state.thirst.setLevel(7);
            state.temperature.setWetTicks(10);
            VitalityStateMessage message = VitalityStateMessage.of(state);
            helper.assertTrue(message.thirstLevel() == 7 && message.isWet(), "The HUD message should carry thirst and the Wet flag");
        });
    }

    private static float dehydrationLeavesHealth(GameTestHelper helper, Difficulty difficulty, float startingHealth) {
        float[] healthAfter = new float[1];
        runWithDifficulty(helper, difficulty, () -> {
            ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
            try {
                MockPlayers.endSpawnProtection(player);
                player.setHealth(startingHealth);
                ThirstState thirst = VitalityState.of(player).thirst;
                thirst.setLevel(0);
                thirst.setDamageTicks(VitalityServerConfig.get().thirst.dehydrationIntervalTicks.get() - 1);
                ThirstDrain.tick(player);
                healthAfter[0] = player.getHealth();
            } finally {
                MockPlayers.leaveWorld(player);
            }
        });
        return healthAfter[0];
    }

    // Tests run one at a time on the server thread, so a difficulty set and restored inside one test touches no other.
    private static void withPlayer(GameTestHelper helper, Difficulty difficulty, Consumer<ServerPlayer> test) {
        runWithDifficulty(helper, difficulty, () -> {
            ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
            try {
                test.accept(player);
            } finally {
                MockPlayers.leaveWorld(player);
            }
        });
        helper.succeed();
    }

    private static void runWithDifficulty(GameTestHelper helper, Difficulty difficulty, Runnable test) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty original = server.getWorldData().getDifficulty();
        server.setDifficulty(difficulty, true);
        try {
            test.run();
        } finally {
            server.setDifficulty(original, true);
        }
    }

    private static boolean isClose(float actual, float expected) {
        return Math.abs(actual - expected) < TOLERANCE;
    }
}
