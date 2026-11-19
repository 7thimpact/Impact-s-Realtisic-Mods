package com.impact.realisticfood.debug;

import com.impact.lib.log.ImpactsLog;

import java.util.List;

/** Food's debug log categories, as listed in its spec ("Debug logging"). */
public final class FoodLogCategories {
    public static final String STARTUP = ImpactsLog.STARTUP_CATEGORY;
    public static final String COMPAT = "compat";
    public static final String CUTTING = "cutting";
    public static final String HEAT = "heat";
    public static final String BOILING = "boiling";
    public static final String SOUP = "soup";
    public static final String EVAPORATION = "evaporation";
    public static final String CAMPFIRE_BUCKET = "campfire_bucket";
    public static final String STONE_BOILING = "stone_boiling";
    public static final String SPIT = "spit";
    public static final String RAW = "raw";
    public static final String STYLES = "styles";
    public static final String COUNTERS = "counters";

    public static final List<String> ALL = List.of(STARTUP, COMPAT, CUTTING, HEAT, BOILING, SOUP, EVAPORATION, CAMPFIRE_BUCKET, STONE_BOILING,
            SPIT, RAW, STYLES, COUNTERS);

    private FoodLogCategories() {
    }
}
