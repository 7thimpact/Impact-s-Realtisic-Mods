package com.impact.lib.config;

import com.impact.lib.log.DebugSettings;

import java.util.List;

/**
 * The debug keys every Impact mod's COMMON file carries: {@code debug.enabled}, {@code debug.categories}
 * and {@code debug.verbose}.
 */
public final class DebugConfigSection implements DebugSettings {
    public static final String SECTION_NAME = "debug";
    public static final String ALL_CATEGORIES = "all";

    public final ConfigValue<Boolean> isEnabled;
    public final ConfigValue<List<String>> categories;
    public final ConfigValue<Boolean> isVerbose;

    private DebugConfigSection(ConfigSpecBuilder builder) {
        isEnabled = builder.defineBoolean("enabled", true,
                "Write this mod's debug lines to logs/latest.log and logs/impacts-debug.log.",
                "Warnings and errors are always written.");
        categories = builder.defineStringList("categories", List.of(ALL_CATEGORIES),
                "The debug categories to write. \"" + ALL_CATEGORIES + "\" writes every category.");
        isVerbose = builder.defineBoolean("verbose", false,
                "Also write the detailed lines, such as per-recipe or per-second breakdowns.");
    }

    /** Defines the three keys inside their own {@code debug} section. */
    public static DebugConfigSection define(ConfigSpecBuilder builder) {
        builder.pushSection(SECTION_NAME, "Debug logging. See Impact's Lib, \"Debug logging\".");
        DebugConfigSection section = defineInCurrentSection(builder);
        builder.popSection();
        return section;
    }

    /** Defines the three keys in the section the builder is already in, for files that add debug keys of their own. */
    public static DebugConfigSection defineInCurrentSection(ConfigSpecBuilder builder) {
        return new DebugConfigSection(builder);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled.get();
    }

    @Override
    public boolean includesCategory(String category) {
        List<String> chosenCategories = categories.get();
        return chosenCategories.contains(ALL_CATEGORIES) || chosenCategories.contains(category);
    }

    @Override
    public boolean isVerboseEnabled() {
        return isVerbose.get();
    }
}
