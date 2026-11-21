package com.impact.realisticcrafting.debug;

import com.impact.lib.log.ImpactsLog;

import java.util.List;

/** Crafting's debug log categories, as listed in its spec ("Debug logging"). */
public final class CraftingLogCategories {
    public static final String STARTUP = ImpactsLog.STARTUP_CATEGORY;
    public static final String COMPAT = "compat";
    public static final String DEPTH = "depth";
    public static final String TIMER = "timer";
    public static final String TIER = "tier";
    public static final String WEAR = "wear";
    public static final String HAMMER = "hammer";
    public static final String PEBBLES = "pebbles";
    public static final String BUCKET = "bucket";
    public static final String VAT = "vat";
    public static final String BEDS = "beds";
    public static final String BYPRODUCTS = "byproducts";
    public static final String AUDIT = "audit";
    public static final String ACHIEVEMENTS = "achievements";

    public static final List<String> ALL = List.of(STARTUP, COMPAT, DEPTH, TIMER, TIER, WEAR, HAMMER, PEBBLES, BUCKET, VAT, BEDS, BYPRODUCTS, AUDIT, ACHIEVEMENTS);

    private CraftingLogCategories() {
    }
}
