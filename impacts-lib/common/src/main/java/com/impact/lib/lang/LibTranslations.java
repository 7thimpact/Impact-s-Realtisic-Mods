package com.impact.lib.lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Lib's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class LibTranslations {
    public static final String WHY_AM_I_HERE_TITLE = "advancements.impactslib.why_am_i_here.title";
    public static final String WHY_AM_I_HERE_DESCRIPTION = "advancements.impactslib.why_am_i_here.description";

    public static final String UNKNOWN_MOD = "commands.impactslib.unknown_mod";
    public static final String DEBUG_FAMILY_SWITCH = "commands.impactslib.debug.family_switch";
    public static final String DEBUG_FAMILY_SWITCH_OFF = "commands.impactslib.debug.family_switch_off";
    public static final String DEBUG_STATUS = "commands.impactslib.debug.status";
    public static final String DEBUG_CHANGED = "commands.impactslib.debug.changed";
    public static final String DEBUG_CATEGORY_CHANGED = "commands.impactslib.debug.category_changed";
    public static final String DEBUG_UNKNOWN_CATEGORY = "commands.impactslib.debug.unknown_category";
    public static final String DEBUG_UNKNOWN_CATEGORY_ANY_MOD = "commands.impactslib.debug.unknown_category_any_mod";
    public static final String CONFIG_NO_VALUES = "commands.impactslib.config.no_values";
    public static final String CONFIG_VALUE = "commands.impactslib.config.value";
    public static final String CONFIG_UNKNOWN_KEY = "commands.impactslib.config.unknown_key";
    public static final String CONFIG_INVALID_VALUE = "commands.impactslib.config.invalid_value";
    public static final String CONFIG_CHANGED = "commands.impactslib.config.changed";
    public static final String DUMP_HEADER = "commands.impactslib.dump.header";
    public static final String DUMP_SECTION = "commands.impactslib.dump.section";
    public static final String DUMP_ENTRY = "commands.impactslib.dump.entry";

    public static final String BOWL_EMPTY = "tooltip.impactslib.bowl_empty";
    public static final String BOWL_ENTRY = "tooltip.impactslib.bowl_entry";
    public static final String WOODEN_GRINDER_BOWL = "item.impactslib.wooden_grinder_bowl";
    public static final String STONE_GRINDER_BOWL = "item.impactslib.stone_grinder_bowl";
    public static final String WOODEN_GRINDING_STICK = "item.impactslib.wooden_grinding_stick";
    public static final String STONE_GRINDING_STICK = "item.impactslib.stone_grinding_stick";

    public static final Map<String, String> ENGLISH = englishText();

    private LibTranslations() {
    }

    private static Map<String, String> englishText() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(WHY_AM_I_HERE_TITLE, "Why am I here?");
        english.put(WHY_AM_I_HERE_DESCRIPTION, "Join a world where Impact's Lib is the only Impact mod. It adds nothing on its own!");
        english.put(UNKNOWN_MOD, "No Impact mod called %s. Known mods: %s");
        english.put(DEBUG_FAMILY_SWITCH, "Debug lines for every Impact mod (debug.allMods): %s");
        english.put(DEBUG_FAMILY_SWITCH_OFF, "Debug lines stay hidden while debug.allMods is off in config/impactslib/impactslib-common.toml");
        english.put(DEBUG_STATUS, "%s: debug %s, categories [%s], verbose %s");
        english.put(DEBUG_CHANGED, "%s: debug is now %s (saved to its config file)");
        english.put(DEBUG_CATEGORY_CHANGED, "%s: debug category %s is now %s (saved to its config file)");
        english.put(DEBUG_UNKNOWN_CATEGORY, "%s has no debug category %s. Its categories: %s");
        english.put(DEBUG_UNKNOWN_CATEGORY_ANY_MOD, "No Impact mod has the debug category %s");
        english.put(CONFIG_NO_VALUES, "%s has no server config values");
        english.put(CONFIG_VALUE, "%s = %s (default %s, allowed %s)");
        english.put(CONFIG_UNKNOWN_KEY, "%s has no server config key %s");
        english.put(CONFIG_INVALID_VALUE, "%s is not a valid value for %s (allowed %s)");
        english.put(CONFIG_CHANGED, "%s is now %s, saved to %s");
        english.put(DUMP_HEADER, "Impact dump for %s");
        english.put(DUMP_SECTION, "[%s]");
        english.put(DUMP_ENTRY, "  %s = %s");
        english.put(BOWL_EMPTY, "Empty. Right-click a grindable item onto it, or hold it in your off hand");
        english.put(BOWL_ENTRY, "%s x%s");
        english.put(WOODEN_GRINDER_BOWL, "Wooden Grinder Bowl");
        english.put(STONE_GRINDER_BOWL, "Stone Grinder Bowl");
        english.put(WOODEN_GRINDING_STICK, "Wooden Grinding Stick");
        english.put(STONE_GRINDING_STICK, "Stone Grinding Stick");
        return Collections.unmodifiableMap(english);
    }
}
