package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.TemperatureUnit;
import com.impact.realisticvitality.config.VitalityClientConfig;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityFeatures;
import com.impact.realisticvitality.config.VitalityGear;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Vitality milestone 1: the mod registers with Lib, its config files load with the spec's defaults, and player data saves. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalitySkeletonGameTests {
    static final String LIB_NAMESPACE = ImpactsLib.MOD_ID;
    static final String EMPTY_TEMPLATE = "empty";
    private static final float SAVED_HYDRATION = 3.5F;
    private static final float SAVED_BODY_TEMPERATURE = -42.0F;
    private static final int SAVED_THIRST = 13;
    private static final int SAVED_WET_TICKS = 120;

    private VitalitySkeletonGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void registersWithLib(GameTestHelper helper) {
        helper.assertTrue(ImpactsLib.registeredImpactMods().contains(VitalityMod.MOD_ID), "Vitality should register itself with Lib");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void registersEveryLogCategory(GameTestHelper helper) {
        ImpactsLog log = ImpactsLog.of(VitalityMod.MOD_ID);
        VitalityLogCategories.ALL.forEach(category ->
                helper.assertTrue(log.registeredCategories().contains(category), "Vitality should register the log category " + category));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void commonConfigSwitchesDefaultOn(GameTestHelper helper) {
        VitalityCommonConfig config = VitalityCommonConfig.get();
        for (VitalityNeed need : VitalityNeed.values()) {
            helper.assertTrue(config.needs.get(need).defaultValue(), need.configKey + ".enabled should default to true");
        }
        for (VitalityGear gearItem : VitalityGear.values()) {
            helper.assertTrue(config.gear.get(gearItem).defaultValue(), "gear." + gearItem.configKey + " should default to true");
        }
        helper.assertTrue(config.isSippingOn.defaultValue(), "pieces.sipping should default to true");
        helper.assertTrue(config.areLiningsOn.defaultValue(), "pieces.linings should default to true");
        helper.assertTrue(config.isDryFoodRuleOn.defaultValue(), "pieces.dryFoodRule should default to true");
        helper.assertTrue(config.debug.isDebugEnabled(), "debug.enabled should default to true");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void serverConfigUsesSpecDefaults(GameTestHelper helper) {
        VitalityServerConfig config = VitalityServerConfig.get();
        helper.assertTrue(config.thirst.restingDrainTicks.defaultValue() == 6000, "Thirst drains 1 point every 5 minutes at rest");
        helper.assertTrue(config.thirst.hotDrainMultiplier.defaultValue() == 1.5, "Hot drains thirst 1.5 times as fast");
        helper.assertTrue(config.thirst.veryHotDrainMultiplier.defaultValue() == 2.0, "Very Hot drains thirst twice as fast");
        helper.assertTrue(config.thirst.noSprintAtOrBelow.defaultValue() == 6, "No sprinting at 6 thirst or less");
        helper.assertTrue(config.thirst.parchedChance.defaultValue() == 0.3, "Unclean water makes you Parched 30% of the time");
        helper.assertTrue(config.thirst.parchedTicks.defaultValue() == 600, "Parched lasts 30 seconds");
        helper.assertTrue(config.thirst.sipThirst.defaultValue() == 1, "A sip restores 1 thirst");
        helper.assertTrue(config.healthCap.isOn.defaultValue(), "healthCap.enabled should default to true");
        helper.assertFalse(config.healthCap.capsAllHealing.defaultValue(), "healthCap.capAllHealing should default to false");
        helper.assertTrue(config.healthCap.fullBarAtOrAbove.defaultValue() == 18, "Regeneration needs a bar at 18 or more");
        helper.assertTrue(config.healthCap.lowBarAtOrBelow.defaultValue() == 6, "A bar at 6 or less caps health at half");
        helper.assertTrue(config.healthCap.middleShare.defaultValue() == 0.75, "A middling bar caps health at three-quarters");
        helper.assertTrue(config.healthCap.lowShare.defaultValue() == 0.5, "A low bar caps health at half");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void clientConfigShowsFahrenheit(GameTestHelper helper) {
        helper.assertTrue(VitalityClientConfig.get().display.units.defaultValue() == TemperatureUnit.FAHRENHEIT, "display.units should default to F");
        helper.assertTrue(TemperatureUnit.CELSIUS.fromFahrenheit(212.0) == 100.0, "212°F is 100°C");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void everyFeatureNameResolves(GameTestHelper helper) {
        for (String featureName : VitalityFeatures.conditionNames()) {
            helper.assertTrue(VitalityFeatures.byConditionName(featureName).orElseThrow().getAsBoolean(), featureName + " should be on by default");
        }
        helper.assertTrue(VitalityFeatures.byConditionName("canteen").isPresent(), "Gear is named by its item ID");
        helper.assertTrue(VitalityFeatures.byConditionName("thirst").isPresent(), "Needs are named by their config key");
        helper.assertTrue(VitalityFeatures.byConditionName("not_a_feature").isEmpty(), "Unknown names don't resolve");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void newPlayersStartFull(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            VitalityState state = VitalityState.of(player);
            helper.assertTrue(state.thirst.level() == ThirstState.FULL, "A new player starts with full thirst");
            helper.assertTrue(state.thirst.hydration() == ThirstState.STARTING_HYDRATION, "A new player starts with 5 Hydration, like saturation");
            helper.assertTrue(state.temperature.body() == 0.0F, "A new player starts Comfortable");
            helper.assertFalse(state.temperature.isWet(), "A new player starts dry");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void playerStateSavesAndLoads(GameTestHelper helper) {
        ServerPlayer savedPlayer = MockPlayers.joinWorld(helper.getLevel());
        ServerPlayer loadedPlayer = MockPlayers.joinWorld(helper.getLevel());
        try {
            VitalityState saved = VitalityState.of(savedPlayer);
            saved.thirst.setLevel(SAVED_THIRST);
            saved.thirst.setHydration(SAVED_HYDRATION);
            saved.temperature.setBody(SAVED_BODY_TEMPERATURE);
            saved.temperature.setWetTicks(SAVED_WET_TICKS);
            CompoundTag playerTag = new CompoundTag();
            PlayerAttachments.save(savedPlayer, playerTag);
            PlayerAttachments.load(loadedPlayer, playerTag);

            VitalityState loaded = VitalityState.of(loadedPlayer);
            helper.assertTrue(loaded.thirst.level() == SAVED_THIRST, "Thirst should survive saving");
            helper.assertTrue(loaded.thirst.hydration() == SAVED_HYDRATION, "Hydration should survive saving");
            helper.assertTrue(loaded.temperature.body() == SAVED_BODY_TEMPERATURE, "Body temperature should survive saving");
            helper.assertTrue(loaded.temperature.wetTicks() == SAVED_WET_TICKS, "The Wet timer should survive saving");
        } finally {
            MockPlayers.leaveWorld(savedPlayer);
            MockPlayers.leaveWorld(loadedPlayer);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void hydrationNeverExceedsThirst(GameTestHelper helper) {
        ThirstState thirst = new ThirstState();
        thirst.setLevel(3);
        helper.assertTrue(thirst.hydration() == 3.0F, "Hydration drops with thirst, as saturation never exceeds hunger");
        thirst.setHydration(10.0F);
        helper.assertTrue(thirst.hydration() == 3.0F, "Hydration can't be raised past thirst");
        thirst.setLevel(-5);
        helper.assertTrue(thirst.level() == 0, "Thirst stops at 0");
        helper.succeed();
    }
}
