package com.impact.realisticcrafting.config;

import java.util.List;
import java.util.OptionalDouble;

/** Config lists of {@code key=number} entries, such as {@code impactscrafting:chest=30} or {@code minecraft=1.5}. */
public final class KeyedNumbers {
    private static final String SEPARATOR = "=";

    private KeyedNumbers() {
    }

    /** The number written for the key, or empty when the key isn't listed or its number doesn't parse. */
    public static OptionalDouble find(List<String> entries, String key) {
        for (String entry : entries) {
            int separatorIndex = entry.lastIndexOf(SEPARATOR);
            if (separatorIndex > 0 && entry.substring(0, separatorIndex).trim().equals(key)) {
                return parse(entry.substring(separatorIndex + 1).trim());
            }
        }
        return OptionalDouble.empty();
    }

    private static OptionalDouble parse(String number) {
        try {
            return OptionalDouble.of(Double.parseDouble(number));
        } catch (NumberFormatException notANumber) {
            return OptionalDouble.empty();
        }
    }
}
