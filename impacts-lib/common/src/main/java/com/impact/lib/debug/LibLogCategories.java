package com.impact.lib.debug;

import com.impact.lib.log.ImpactsLog;

import java.util.List;

/** Lib's debug log categories, as listed in its spec ("Debug logging"). */
public final class LibLogCategories {
    public static final String STARTUP = ImpactsLog.STARTUP_CATEGORY;
    public static final String REGISTRY = "registry";
    public static final String TOOL_CRAFTING = "toolcrafting";
    public static final String GRINDER = "grinder";
    public static final String HOLD_TO_USE = "holdtouse";
    public static final String USES = "uses";
    public static final String GIVE_OR_DROP = "giveordrop";
    public static final String COUNTERS = "counters";
    public static final String LAYOUT = "layout";
    public static final String PROVIDERS = "providers";
    public static final String CONFLICTS = "conflicts";
    public static final String AUDIT = "audit";

    public static final List<String> ALL = List.of(STARTUP, REGISTRY, TOOL_CRAFTING, GRINDER, HOLD_TO_USE, USES,
            GIVE_OR_DROP, COUNTERS, LAYOUT, PROVIDERS, CONFLICTS, AUDIT);

    private LibLogCategories() {
    }
}
