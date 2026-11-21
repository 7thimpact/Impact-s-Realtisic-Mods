package com.impact.realisticvitality.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * The names the {@code impactsvitality:feature_enabled} recipe condition accepts, each backed by switches in the common
 * file: a need ({@code thirst}, {@code temperature}, {@code air}), a piece ({@code sipping}, {@code linings},
 * {@code dry_food_rule}) or a gear item by its registry path ({@code canteen}, {@code respirator}, ...). A piece or a
 * gear item is off whenever its need is.
 */
public final class VitalityFeatures {
    public static final String SIPPING = "sipping";
    public static final String LININGS = "linings";
    public static final String DRY_FOOD_RULE = "dry_food_rule";
    private static final Map<String, BooleanSupplier> SWITCHES_BY_NAME = buildSwitches();

    private VitalityFeatures() {
    }

    public static Optional<BooleanSupplier> byConditionName(String conditionName) {
        return Optional.ofNullable(SWITCHES_BY_NAME.get(conditionName));
    }

    public static Iterable<String> conditionNames() {
        return SWITCHES_BY_NAME.keySet();
    }

    private static Map<String, BooleanSupplier> buildSwitches() {
        Map<String, BooleanSupplier> switches = new LinkedHashMap<>();
        for (VitalityNeed need : VitalityNeed.values()) {
            switches.put(need.configKey, () -> VitalityCommonConfig.get().isOn(need));
        }
        switches.put(SIPPING, () -> isPieceOn(VitalityNeed.THIRST, VitalityCommonConfig.get().isSippingOn.get()));
        switches.put(LININGS, () -> isPieceOn(VitalityNeed.TEMPERATURE, VitalityCommonConfig.get().areLiningsOn.get()));
        switches.put(DRY_FOOD_RULE, () -> isPieceOn(VitalityNeed.TEMPERATURE, VitalityCommonConfig.get().isDryFoodRuleOn.get()));
        for (VitalityGear gearItem : VitalityGear.values()) {
            switches.put(gearItem.itemPath, () -> VitalityCommonConfig.get().isOn(gearItem));
        }
        return Collections.unmodifiableMap(switches);
    }

    private static boolean isPieceOn(VitalityNeed need, boolean isPieceSwitchOn) {
        return isPieceSwitchOn && VitalityCommonConfig.get().isOn(need);
    }
}
