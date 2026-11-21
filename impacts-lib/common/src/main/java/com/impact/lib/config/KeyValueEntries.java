package com.impact.lib.config;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Reads config lists written as {@code key=value} entries, such as {@code ["minecraft:beef=3"]}. Entries that don't
 * parse are skipped; the first entry for a key wins.
 */
public final class KeyValueEntries {
    private static final String SEPARATOR = "=";
    private static final int KEY_AND_VALUE = 2;

    private KeyValueEntries() {
    }

    public static Optional<String> find(List<String> entries, String key) {
        return entries.stream()
                .map(entry -> entry.split(SEPARATOR, KEY_AND_VALUE))
                .filter(parts -> parts.length == KEY_AND_VALUE && parts[0].trim().equals(key))
                .map(parts -> parts[1].trim())
                .findFirst();
    }

    public static OptionalInt findInt(List<String> entries, String key) {
        Optional<String> value = find(entries, key);
        if (value.isEmpty()) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(value.get()));
        } catch (NumberFormatException notANumber) {
            return OptionalInt.empty();
        }
    }

    public static OptionalDouble findDouble(List<String> entries, String key) {
        Optional<String> value = find(entries, key);
        if (value.isEmpty()) {
            return OptionalDouble.empty();
        }
        try {
            return OptionalDouble.of(Double.parseDouble(value.get()));
        } catch (NumberFormatException notANumber) {
            return OptionalDouble.empty();
        }
    }
}
