package com.impact.realisticvitality.debug;

import com.impact.lib.log.ImpactsLog;

import java.util.List;

/** Vitality's debug log categories, as listed in its spec ("Debug logging"). */
public final class VitalityLogCategories {
    public static final String STARTUP = ImpactsLog.STARTUP_CATEGORY;
    public static final String COMPAT = "compat";
    public static final String THIRST = "thirst";
    public static final String DRINK = "drink";
    public static final String PURITY = "purity";
    public static final String HEALTH = "health";
    public static final String TEMPERATURE = "temperature";
    public static final String FOOD = "food";
    public static final String AIR = "air";
    public static final String GEAR = "gear";

    public static final List<String> ALL = List.of(STARTUP, COMPAT, THIRST, DRINK, PURITY, HEALTH, TEMPERATURE, FOOD, AIR, GEAR);

    private VitalityLogCategories() {
    }
}
