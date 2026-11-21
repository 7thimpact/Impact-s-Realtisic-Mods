package com.impact.realisticfood.config;

import com.impact.lib.config.ConfigValue;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * Food's switchable features, each backed by a switch in the common file. The {@code impactsfood:feature_enabled}
 * recipe condition names them by {@link #conditionName}.
 */
public enum FoodFeature {
    CUTTING("cutting", config -> config.overhaul.isCuttingOn),
    BOILING("boiling", config -> config.overhaul.isBoilingOn),
    SOUPS("soups", config -> config.overhaul.areSoupsOn),
    EATING_FROM_POT("eating_from_pot", config -> config.overhaul.isEatingFromPotOn),
    CAMPFIRE_BUCKET("campfire_bucket", config -> config.overhaul.isCampfireBucketOn),
    STONE_BOILING("stone_boiling", config -> config.overhaul.isStoneBoilingOn),
    SPIT("spit", config -> config.overhaul.isSpitOn),
    BAKING("baking", config -> config.overhaul.isBakingOn),
    STEW_OVERHAUL("stews", config -> config.overhaul.isStewOverhaulOn),
    RAW_FOOD_RISK("raw_food", config -> config.rawFood.isOn),
    STYLES("styles", config -> config.cooking.areStylesOn),
    OVERCOOKING("overcooking", config -> config.cooking.isOvercookingOn),
    DRIED("dried", config -> config.cooking.isDriedOn),
    TOASTED("toasted", config -> config.cooking.isToastedOn),
    BOILED("boiled", config -> config.cooking.isBoiledOn);

    public final String conditionName;
    private final Function<FoodCommonConfig, ConfigValue<Boolean>> configSwitch;

    FoodFeature(String conditionName, Function<FoodCommonConfig, ConfigValue<Boolean>> configSwitch) {
        this.conditionName = conditionName;
        this.configSwitch = configSwitch;
    }

    public boolean isOn() {
        return configSwitch.apply(FoodCommonConfig.get()).get();
    }

    public static Optional<FoodFeature> byConditionName(String conditionName) {
        return Arrays.stream(values()).filter(feature -> feature.conditionName.equals(conditionName)).findFirst();
    }
}
