package com.impact.realisticvitality.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.EventTrigger;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticvitality.VitalityMod;

import java.util.List;

/**
 * Vitality's advancement triggers, as the spec names them, and two for counting achievements. Facts each one carries:
 * <ul>
 *   <li>{@code drank}: {@code purity} (purified, unclean, salt), {@code parched} (true when the drink made you Parched)</li>
 *   <li>{@code temperature_state}, once a second: {@code state}, {@code cold_or_worse}, {@code hot_or_worse}, {@code on_fire},
 *       {@code thirst_full}, {@code air_full}</li>
 *   <li>{@code air_lost}, each bubble lost: {@code cause} (thin, fumes), {@code without_gear} (no mask, tank or breathing effect)</li>
 *   <li>{@code gear_used}: {@code gear} (air_tank, filtered_mask, warm_linings, straw_hat_in_desert_noon)</li>
 * </ul>
 */
public final class VitalityTriggers {
    public static final FactTrigger DRANK = facts("drank");
    public static final FactTrigger TEMPERATURE_STATE = facts("temperature_state");
    public static final FactTrigger AIR_LOST = facts("air_lost");
    public static final FactTrigger GEAR_USED = facts("gear_used");
    public static final EventTrigger HEALTH_CAPPED = event("health_capped");
    public static final EventTrigger DRY_FOOD_REFUSED = event("dry_food_refused");
    /** {@code counters.wellWateredDrinks} drinks drunk ("Well Watered"). */
    public static final EventTrigger WELL_WATERED = event("well_watered");
    /** A whole day in balance ("In Balance"). */
    public static final EventTrigger IN_BALANCE = event("in_balance");
    /** Lib counter of drinks drunk, sips included. */
    public static final String DRINKS_COUNTER = VitalityMod.MOD_ID + ":drinks";
    /** Lib counter of seconds in a row with thirst above 6, air never empty and body temperature between Cold and Hot. */
    public static final String BALANCE_SECONDS_COUNTER = VitalityMod.MOD_ID + ":balance_seconds";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    private VitalityTriggers() {
    }

    /** Registers every trigger with vanilla. Runs once, on the main thread, before any world loads. */
    public static void registerAll() {
        List.of(DRANK, TEMPERATURE_STATE, AIR_LOST, GEAR_USED, HEALTH_CAPPED, DRY_FOOD_REFUSED, WELL_WATERED, IN_BALANCE)
                .forEach(trigger -> LoaderPlatform.current().registerCriterionTrigger(trigger));
    }

    public static String fact(boolean isTrue) {
        return isTrue ? TRUE : FALSE;
    }

    private static FactTrigger facts(String path) {
        return new FactTrigger(ResourceIds.of(VitalityMod.MOD_ID, path));
    }

    private static EventTrigger event(String path) {
        return new EventTrigger(ResourceIds.of(VitalityMod.MOD_ID, path));
    }
}
