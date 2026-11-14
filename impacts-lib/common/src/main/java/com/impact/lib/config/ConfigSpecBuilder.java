package com.impact.lib.config;

import java.util.List;

/**
 * Builds one Impact config file. Sections nest the way the specs' dotted keys do: pushing {@code debug}, then
 * {@code logFile}, then defining {@code sizeMb} creates {@code debug.logFile.sizeMb}. Every value takes a
 * comment, and the builder adds the value's default (and allowed range, where there is one) to it.
 * A value outside its range, or one that can't be read, falls back to its default with a warning.
 */
public interface ConfigSpecBuilder {
    ConfigSpecBuilder pushSection(String name, String... comment);

    ConfigSpecBuilder popSection();

    ConfigValue<Boolean> defineBoolean(String key, boolean defaultValue, String... comment);

    ConfigValue<Integer> defineInteger(String key, int defaultValue, int minimum, int maximum, String... comment);

    ConfigValue<Double> defineDouble(String key, double defaultValue, double minimum, double maximum, String... comment);

    /** A list of strings, such as the specs' {@code id=value} override lists. An empty list is allowed. */
    ConfigValue<List<String>> defineStringList(String key, List<String> defaultValue, String... comment);

    <E extends Enum<E>> ConfigValue<E> defineEnum(String key, E defaultValue, String... comment);

    /** Builds the file and registers it with the loader as {@code impactslib/<modid>-<type>.toml}. */
    ConfigFile register();
}
