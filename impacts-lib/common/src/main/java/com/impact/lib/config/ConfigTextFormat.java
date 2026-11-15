package com.impact.lib.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** How config values are typed as text, for example in {@code /impacts config <mod> <key> <value>}. */
public final class ConfigTextFormat {
    public static final String BOOLEAN_VALUES = "true or false";
    public static final String STRING_LIST_VALUES = "a comma-separated list";
    private static final String TRUE_TEXT = "true";
    private static final String FALSE_TEXT = "false";
    private static final String LIST_START = "[";
    private static final String LIST_END = "]";
    private static final String LIST_SEPARATOR = ",";
    private static final String QUOTE = "\"";
    private static final String ENUM_NAME_SEPARATOR = ", ";

    private ConfigTextFormat() {
    }

    public static Optional<Boolean> parseBoolean(String text) {
        String trimmedText = text.trim();
        if (trimmedText.equalsIgnoreCase(TRUE_TEXT)) {
            return Optional.of(true);
        }
        if (trimmedText.equalsIgnoreCase(FALSE_TEXT)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    public static Optional<Integer> parseInteger(String text) {
        try {
            return Optional.of(Integer.parseInt(text.trim()));
        } catch (NumberFormatException notAnInteger) {
            return Optional.empty();
        }
    }

    public static Optional<Double> parseDouble(String text) {
        try {
            double number = Double.parseDouble(text.trim());
            return Double.isFinite(number) ? Optional.of(number) : Optional.empty();
        } catch (NumberFormatException notANumber) {
            return Optional.empty();
        }
    }

    /** Reads {@code a=1, b=2} or {@code [a=1, b=2]}. Blank entries are dropped, so {@code []} is an empty list. */
    public static Optional<List<String>> parseStringList(String text) {
        String listBody = stripSurrounding(text.trim(), LIST_START, LIST_END);
        List<String> entries = Arrays.stream(listBody.split(LIST_SEPARATOR))
                .map(entry -> stripSurrounding(entry.trim(), QUOTE, QUOTE).trim())
                .filter(entry -> !entry.isEmpty())
                .toList();
        return Optional.of(entries);
    }

    /** Matches an enum constant by name, ignoring case. */
    public static <E extends Enum<E>> Optional<E> parseEnum(Class<E> enumType, String text) {
        String trimmedText = text.trim();
        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.name().equalsIgnoreCase(trimmedText))
                .findFirst();
    }

    public static <E extends Enum<E>> String enumValues(Class<E> enumType) {
        return String.join(ENUM_NAME_SEPARATOR, Arrays.stream(enumType.getEnumConstants()).map(Enum::name).toList());
    }

    private static String stripSurrounding(String text, String start, String end) {
        boolean isSurrounded = text.length() >= start.length() + end.length() && text.startsWith(start) && text.endsWith(end);
        return isSurrounded ? text.substring(start.length(), text.length() - end.length()) : text;
    }
}
