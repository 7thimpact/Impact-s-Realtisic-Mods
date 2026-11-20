package com.impact.realisticcrafting.config;

import java.util.Arrays;
import java.util.Optional;

/**
 * The catalog categories whose vanilla recipes Crafting replaces, each with its own switch in the common file. The
 * {@code impactscrafting:overhaul_enabled} condition names them by {@link #configKey}.
 */
public enum OverhaulCategory {
    WOOD("wood"),
    STONE("stone"),
    IRON("iron"),
    REDSTONE("redstone"),
    COMBAT("combat"),
    TEXTILES("textiles"),
    DYES("dyes"),
    BEDS("beds");

    public final String configKey;

    OverhaulCategory(String configKey) {
        this.configKey = configKey;
    }

    public boolean isOn() {
        return CraftingCommonConfig.get().overhaul.get(this).get();
    }

    public static Optional<OverhaulCategory> byConfigKey(String key) {
        return Arrays.stream(values()).filter(category -> category.configKey.equals(key)).findFirst();
    }
}
