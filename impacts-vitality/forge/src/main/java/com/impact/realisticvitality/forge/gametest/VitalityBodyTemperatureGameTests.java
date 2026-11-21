package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.player.TemperatureState;
import com.impact.realisticvitality.player.VitalityExemptions;
import com.impact.realisticvitality.player.VitalityPlayerTicks;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.BodyState;
import com.impact.realisticvitality.temperature.BodyTemperature;
import com.impact.realisticvitality.temperature.ComfortRange;
import com.impact.realisticvitality.temperature.DryFoods;
import com.impact.realisticvitality.temperature.Insulation;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.ContainerContents;
import com.impact.realisticvitality.thirst.Drinking;
import com.impact.realisticvitality.thirst.ThirstDrain;
import com.impact.realisticvitality.thirst.WaterContainer;
import com.impact.realisticvitality.thirst.WaterPurity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/** Vitality milestone 6: body temperature drifts, its bands have their effects, and it's too hot for dry food when Hot. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityBodyTemperatureGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final float COMFORTABLE_AMBIENT = 65.0F;
    private static final float TOLERANCE = 0.001F;

    private VitalityBodyTemperatureGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bodyDriftsOutsideTheComfortableRange(GameTestHelper helper) {
        assertClose(helper, BodyTemperature.driftPerSecond(0.0F, 100.0F, Insulation.NONE), 2.0F, "20°F above the range heats 2 points a second");
        assertClose(helper, BodyTemperature.driftPerSecond(0.0F, 30.0F, Insulation.NONE), -2.0F, "20°F below the range cools 2 points a second");
        assertClose(helper, BodyTemperature.driftPerSecond(10.0F, COMFORTABLE_AMBIENT, Insulation.NONE), -2.0F, "Inside the range it returns to 0");
        assertClose(helper, BodyTemperature.driftPerSecond(1.0F, COMFORTABLE_AMBIENT, Insulation.NONE), -1.0F, "and stops at 0");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void insulationMovesTheEdges(GameTestHelper helper) {
        ComfortRange ironSet = ComfortRange.of(new Insulation(-4.0, -4.0));
        assertClose(helper, (float) ironSet.coldEdge(), 62.0F, "A full set of iron armor moves the cold edge up to 62°F");
        assertClose(helper, (float) ironSet.hotEdge(), 68.0F, "and the hot edge down to 68°F");
        ComfortRange wet = ComfortRange.of(new Insulation(0.0, 6.0));
        assertClose(helper, (float) wet.hotEdge(), 98.0F, "Being Wet (+6 cooling) raises the hot edge 18°F");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bodyTemperatureBands(GameTestHelper helper) {
        Map<Float, BodyState> expected = Map.of(-100.0F, BodyState.FREEZING, -80.0F, BodyState.VERY_COLD, -60.0F, BodyState.COLD,
                -30.0F, BodyState.CHILLY, 0.0F, BodyState.COMFORTABLE, 30.0F, BodyState.WARM, 60.0F, BodyState.HOT,
                80.0F, BodyState.VERY_HOT, 100.0F, BodyState.OVERHEATING);
        expected.forEach((body, state) -> helper.assertTrue(BodyState.of(body) == state, body + " should be " + state + ", got " + BodyState.of(body)));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void heatDrainsThirstFaster(GameTestHelper helper) {
        withPlayer(helper, player -> {
            TemperatureState temperature = VitalityState.of(player).temperature;
            temperature.setBody(60.0F);
            ThirstDrain.addExhaustion(player, 1.0F);
            assertClose(helper, VitalityState.of(player).thirst.exhaustion(), 1.5F, "Hot drains thirst 1.5 times as fast");
            VitalityState.of(player).thirst.setExhaustion(0.0F);
            temperature.setBody(80.0F);
            ThirstDrain.addExhaustion(player, 1.0F);
            assertClose(helper, VitalityState.of(player).thirst.exhaustion(), 2.0F, "Very Hot drains it twice as fast");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bandsGiveTheirEffects(GameTestHelper helper) {
        withPlayer(helper, player -> {
            TemperatureState temperature = VitalityState.of(player).temperature;
            temperature.setBody(-60.0F);
            BodyTemperature.tickSecond(player, COMFORTABLE_AMBIENT);
            helper.assertTrue(player.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 0, "Cold gives Slowness I");
            player.removeAllEffects();
            temperature.setBody(-90.0F);
            BodyTemperature.tickSecond(player, COMFORTABLE_AMBIENT);
            helper.assertTrue(player.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 1, "Very Cold gives Slowness II");
            player.removeAllEffects();
            temperature.setBody(85.0F);
            BodyTemperature.tickSecond(player, COMFORTABLE_AMBIENT);
            helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "Very Hot gives Weakness");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void extremesHurtEveryThreeSeconds(GameTestHelper helper) {
        withPlayer(helper, player -> {
            Difficulty original = helper.getLevel().getServer().getWorldData().getDifficulty();
            helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
            try {
                MockPlayers.endSpawnProtection(player);
                TemperatureState temperature = VitalityState.of(player).temperature;
                temperature.setBody(TemperatureState.HOTTEST);
                float hotAmbient = 200.0F;
                BodyTemperature.tickSecond(player, hotAmbient);
                BodyTemperature.tickSecond(player, hotAmbient);
                helper.assertTrue(player.getHealth() == player.getMaxHealth(), "No damage for the first two seconds");
                BodyTemperature.tickSecond(player, hotAmbient);
                helper.assertTrue(player.getHealth() < player.getMaxHealth(), "Overheating hurts on the third second");
                helper.assertTrue(player.hasEffect(MobEffects.CONFUSION), "and makes you nauseous");
            } finally {
                helper.getLevel().getServer().setDifficulty(original, true);
            }
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void tooHotForDryFood(GameTestHelper helper) {
        helper.assertTrue(DryFoods.isTooHotToEat(60.0F, new ItemStack(Items.BREAD)), "Bread can't be eaten while Hot");
        helper.assertFalse(DryFoods.isTooHotToEat(40.0F, new ItemStack(Items.BREAD)), "but can while only Warm");
        helper.assertFalse(DryFoods.isTooHotToEat(60.0F, new ItemStack(Items.APPLE)), "An apple isn't dry");
        helper.assertFalse(DryFoods.isTooHotToEat(60.0F, new ItemStack(Items.MELON_SLICE)), "Melon is moist");
        helper.assertFalse(DryFoods.isTooHotToEat(60.0F, new ItemStack(Items.MUSHROOM_STEW)), "Soup is moist");
        ResourceIds.item(ResourceIds.of("impactsfood", "boiled_bread")).ifPresent(boiledBread ->
                helper.assertFalse(DryFoods.isTooHotToEat(60.0F, new ItemStack(boiledBread)), "Boiled bread is moist, not dry"));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void creativePlayersAreLeftAlone(GameTestHelper helper) {
        withPlayer(helper, player -> {
            helper.assertFalse(VitalityExemptions.isExempt(player), "A survival player feels Vitality's needs");
            player.setGameMode(GameType.CREATIVE);
            helper.assertTrue(VitalityExemptions.isExempt(player), "A creative player doesn't");
            float bodyBefore = VitalityState.of(player).temperature.body();
            VitalityState.of(player).temperature.setBody(bodyBefore + 30.0F);
            float hotBody = VitalityState.of(player).temperature.body();
            VitalityPlayerTicks.tick(player);
            helper.assertTrue(VitalityState.of(player).temperature.body() == hotBody, "and their temperature doesn't tick");
            player.setGameMode(GameType.SPECTATOR);
            helper.assertTrue(VitalityExemptions.isExempt(player), "nor does a spectator's");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void coldDrinksCoolAndSoupsWarm(GameTestHelper helper) {
        withPlayer(helper, player -> {
            TemperatureState temperature = VitalityState.of(player).temperature;
            Drinking.onUseFinished(player, VitalityItems.bottle(BottledWater.COLD));
            assertClose(helper, temperature.body(), -15.0F, "Cold Water drops body temperature by 15");
            Drinking.onUseFinished(player, new ItemStack(Items.MUSHROOM_STEW));
            assertClose(helper, temperature.body(), 0.0F, "A stew raises it by 15");
            Drinking.onUseFinished(player, new ItemStack(Items.MELON_SLICE));
            assertClose(helper, temperature.body(), -15.0F, "A melon slice cools by 15");
            ItemStack coldFlask = new ItemStack(VitalityItems.container(WaterContainer.INSULATED_FLASK));
            new ContainerContents(2, ContainerContents.Kind.COLD_WATER, WaterPurity.PURIFIED, Optional.empty()).writeTo(coldFlask);
            Drinking.onUseFinished(player, coldFlask);
            assertClose(helper, temperature.body(), -30.0F, "Cold Water from the Insulated Flask still cools");
            ItemStack soupFlask = new ItemStack(VitalityItems.container(WaterContainer.INSULATED_FLASK));
            ContainerContents.EMPTY.withSoup(ResourceIds.itemId(Items.RABBIT_STEW)).orElseThrow().writeTo(soupFlask);
            Drinking.onUseFinished(player, soupFlask);
            assertClose(helper, temperature.body(), -15.0F, "Soup from the flask still warms");
        });
    }

    private static void assertClose(GameTestHelper helper, float actual, float expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < TOLERANCE, what + ": expected " + expected + ", got " + actual);
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
