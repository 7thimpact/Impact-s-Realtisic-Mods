package com.impact.lib.config;

import com.impact.lib.log.DebugSettings;
import com.impact.lib.log.ImpactsLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The debug keys every Impact mod's COMMON file carries: {@code debug.enabled}, {@code debug.categories}
 * and {@code debug.verbose}. Defining the section connects it to the mod's {@link ImpactsLog} and lets
 * {@code /impacts debug} find it.
 */
public final class DebugConfigSection implements DebugSettings {
    public static final String SECTION_NAME = "debug";
    public static final String ALL_CATEGORIES = "all";

    private static final Map<String, DebugConfigSection> SECTIONS_BY_MOD_ID = new ConcurrentHashMap<>();

    public final String modId;
    public final ConfigValue<Boolean> isEnabled;
    public final ConfigValue<List<String>> categories;
    public final ConfigValue<Boolean> isVerbose;

    private DebugConfigSection(ConfigSpecBuilder builder) {
        modId = builder.modId();
        isEnabled = builder.defineBoolean("enabled", true,
                "Write this mod's debug lines to logs/latest.log and logs/impacts-debug.log.",
                "Warnings and errors are always written.");
        categories = builder.defineStringList("categories", List.of(ALL_CATEGORIES),
                "The debug categories to write. \"" + ALL_CATEGORIES + "\" writes every category.");
        isVerbose = builder.defineBoolean("verbose", false,
                "Also write the detailed lines, such as per-recipe or per-second breakdowns.");
        SECTIONS_BY_MOD_ID.put(modId, this);
        ImpactsLog.of(modId).useDebugSettings(this);
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

    public static Optional<DebugConfigSection> forMod(String modId) {
        return Optional.ofNullable(SECTIONS_BY_MOD_ID.get(modId));
    }

    /** Every registered section, sorted by mod ID. */
    public static Map<String, DebugConfigSection> allByModId() {
        return new TreeMap<>(SECTIONS_BY_MOD_ID);
    }

    /** The categories after turning one on. Unchanged when every category is already on. */
    public static List<String> categoriesWith(List<String> currentCategories, String category) {
        boolean isAlreadyOn = currentCategories.contains(ALL_CATEGORIES) || currentCategories.contains(category);
        if (isAlreadyOn) {
            return currentCategories;
        }
        List<String> changedCategories = new ArrayList<>(currentCategories);
        changedCategories.add(category);
        return List.copyOf(changedCategories);
    }

    /** The categories after turning one off. "all" is first expanded into the mod's registered categories. */
    public static List<String> categoriesWithout(List<String> currentCategories, String category,
                                                 Collection<String> registeredCategories) {
        Collection<String> startingCategories = currentCategories.contains(ALL_CATEGORIES) ? registeredCategories : currentCategories;
        return startingCategories.stream()
                .filter(startingCategory -> !startingCategory.equals(category))
                .sorted()
                .toList();
    }

    /** Turns debug lines on or off for this mod and saves its COMMON file. */
    public void setDebugEnabled(boolean isOn) {
        isEnabled.set(isOn);
    }

    /** Turns one category on, which also turns the mod's debug lines on, and saves the file. */
    public void turnCategoryOn(String category) {
        categories.set(categoriesWith(categories.get(), category));
        isEnabled.set(true);
    }

    /** Turns one category off and saves the file. */
    public void turnCategoryOff(String category) {
        categories.set(categoriesWithout(categories.get(), category, ImpactsLog.of(modId).registeredCategories()));
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
