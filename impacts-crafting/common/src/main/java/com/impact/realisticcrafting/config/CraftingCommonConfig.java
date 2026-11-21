package com.impact.realisticcrafting.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.DebugConfigSection;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticcrafting.CraftingMod;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Crafting's COMMON file: {@code config/impactslib/impactscrafting-common.toml}. One recipe switch per catalog
 * category (read by the {@code impactscrafting:overhaul_enabled} condition, since recipes load before a world's server
 * config), recipe lists, feature switches and debug settings.
 */
public final class CraftingCommonConfig {
    private static volatile CraftingCommonConfig registered;

    public final Map<OverhaulCategory, ConfigValue<Boolean>> overhaul;
    public final ConfigValue<List<String>> disabledRecipes;
    public final ConfigValue<List<String>> keptVanillaRecipes;
    public final Map<CraftingFeature, ConfigValue<Boolean>> features;
    public final DebugConfigSection debug;
    public final ConfigFile file;

    private CraftingCommonConfig(ConfigSpecBuilder builder) {
        builder.pushSection("overhaul", "Which catalog categories replace their vanilla recipes (applies on /reload).");
        overhaul = defineOverhaul(builder);
        builder.popSection();
        builder.pushSection("recipes", "Single recipes.");
        disabledRecipes = builder.defineStringList("disabled", List.of(), "Crafting recipe IDs to drop.");
        keptVanillaRecipes = builder.defineStringList("keepVanilla", List.of(), "Vanilla recipe IDs Crafting leaves alone.");
        builder.popSection();
        builder.pushSection("features", "Crafting's systems.");
        features = defineFeatures(builder);
        builder.popSection();
        debug = DebugConfigSection.define(builder);
        file = builder.register();
    }

    public static CraftingCommonConfig register() {
        registered = new CraftingCommonConfig(ImpactsConfig.builder(CraftingMod.MOD_ID, ConfigType.COMMON));
        return registered;
    }

    public static CraftingCommonConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Crafting's common config is read before it was registered.");
        }
        return registered;
    }

    private static Map<OverhaulCategory, ConfigValue<Boolean>> defineOverhaul(ConfigSpecBuilder builder) {
        Map<OverhaulCategory, ConfigValue<Boolean>> switches = new EnumMap<>(OverhaulCategory.class);
        for (OverhaulCategory category : OverhaulCategory.values()) {
            switches.put(category, builder.defineBoolean(category.configKey, true, "Replace the " + category.configKey + " catalog's vanilla recipes."));
        }
        return Collections.unmodifiableMap(switches);
    }

    private static Map<CraftingFeature, ConfigValue<Boolean>> defineFeatures(ConfigSpecBuilder builder) {
        Map<CraftingFeature, ConfigValue<Boolean>> switches = new EnumMap<>(CraftingFeature.class);
        for (CraftingFeature feature : CraftingFeature.values()) {
            switches.put(feature, builder.defineBoolean(feature.configKey, true, feature.description));
        }
        return Collections.unmodifiableMap(switches);
    }
}
