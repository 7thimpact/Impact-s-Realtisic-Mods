package com.impact.realisticfood.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.EventTrigger;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticfood.FoodMod;

import java.util.List;

/**
 * Food's advancement triggers: the spec's three ({@code cauldron_boiled}, {@code soup_ladled},
 * {@code raw_food_sickness}) plus ones for achievements no vanilla trigger can express, and the Lib counters behind
 * the counting achievements.
 */
public final class FoodTriggers {
    public static final EventTrigger CAULDRON_BOILED = event("cauldron_boiled");
    public static final SoupLadledTrigger SOUP_LADLED = new SoupLadledTrigger();
    public static final EventTrigger RAW_FOOD_SICKNESS = event("raw_food_sickness");
    /** Something finished boiling in a campfire bucket nearby ("Camp Cook"). */
    public static final EventTrigger CAMPFIRE_BUCKET_BOILED = event("campfire_bucket_boiled");
    /** A Hot Pebble set a stone-boiling pot boiling ("Stone Soup", only with Crafting). */
    public static final EventTrigger STONE_BOILED = event("stone_boiled");
    public static final EventTrigger SPIT_MOUNTED = event("spit_mounted");
    /** Bread taken out of a furnace or smoker, which only Loaf Dough bakes into ("Use Your Loaf"). */
    public static final EventTrigger BREAD_BAKED = event("bread_baked");
    /** One boiled or toasted food eaten {@code counters.dayInAndDayOutEats} times. */
    public static final EventTrigger DAY_IN_AND_DAY_OUT = event("day_in_and_day_out");
    /** {@code counters.tasteTesterFoods} different styled foods eaten. */
    public static final EventTrigger TASTE_TESTER = event("taste_tester");
    /** A spit turned {@code counters.roundAndRoundTurns} times. */
    public static final EventTrigger SPIT_TURNED_OFTEN = event("spit_turned_often");
    /** Every other Realistic Food achievement earned ("Well Fed"). */
    public static final EventTrigger WELL_FED = event("well_fed");
    /** Lib counter of every styled food eaten, per item ("Taste Tester"). */
    public static final String STYLED_FOOD_EATEN_COUNTER = FoodMod.MOD_ID + ":styled_food_eaten";
    /** Lib counter of boiled and toasted food eaten, per item ("Day in and Day out"). */
    public static final String BOILED_OR_TOASTED_EATEN_COUNTER = FoodMod.MOD_ID + ":boiled_or_toasted_eaten";
    /** Lib counter of spit turns ("Round and Round"). */
    public static final String SPIT_TURNS_COUNTER = FoodMod.MOD_ID + ":spit_turns";

    private FoodTriggers() {
    }

    /** Registers every trigger with vanilla. Runs once, on the main thread, before any world loads. */
    public static void registerAll() {
        List.of(CAULDRON_BOILED, SOUP_LADLED, RAW_FOOD_SICKNESS, CAMPFIRE_BUCKET_BOILED, STONE_BOILED, SPIT_MOUNTED, BREAD_BAKED,
                        DAY_IN_AND_DAY_OUT, TASTE_TESTER, SPIT_TURNED_OFTEN, WELL_FED)
                .forEach(trigger -> LoaderPlatform.current().registerCriterionTrigger(trigger));
    }

    private static EventTrigger event(String path) {
        return new EventTrigger(ResourceIds.of(FoodMod.MOD_ID, path));
    }
}
