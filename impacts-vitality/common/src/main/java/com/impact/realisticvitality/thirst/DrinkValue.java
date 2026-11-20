package com.impact.realisticvitality.thirst;

import java.util.Optional;

/** What one drink or bite does to thirst: points of thirst (negative for salty or dry food) and Hydration. */
public record DrinkValue(int thirst, float hydration) {
    private static final String SEPARATOR = ",";

    /** Reads {@code "thirst,hydration"}, as written in the config, for example {@code "6,4"} or {@code "-1,0"}. */
    public static Optional<DrinkValue> parse(String text) {
        String[] parts = text.split(SEPARATOR);
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            return Optional.of(new DrinkValue(Integer.parseInt(parts[0].trim()), Float.parseFloat(parts[1].trim())));
        } catch (NumberFormatException notANumber) {
            return Optional.empty();
        }
    }
}
