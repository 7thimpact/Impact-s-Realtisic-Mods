package com.impact.lib.log;

/**
 * Formats Impact log lines: {@code [Impact/<mod>/<category>] <event> key=value ...}. The key-value arguments
 * alternate between a key and its value.
 */
public final class LogLineFormatter {
    private static final String IMPACT_MOD_ID_PREFIX = "impacts";
    private static final String LINE_START = "[Impact/";
    private static final char PATH_SEPARATOR = '/';
    private static final String TAG_END = "] ";
    private static final char PAIR_SEPARATOR = ' ';
    private static final char KEY_VALUE_SEPARATOR = '=';
    private static final String MISSING_VALUE = "<missing>";

    private LogLineFormatter() {
    }

    public static String format(String modShortName, String category, String event, Object... keyValues) {
        StringBuilder line = new StringBuilder(LINE_START)
                .append(modShortName).append(PATH_SEPARATOR).append(category).append(TAG_END).append(event);
        for (int keyIndex = 0; keyIndex < keyValues.length; keyIndex += 2) {
            appendPair(line, keyValues, keyIndex);
        }
        return line.toString();
    }

    /** The name used inside log lines: {@code impactsinventory} becomes {@code inventory}, {@code impactslib} becomes {@code lib}. */
    public static String modShortName(String modId) {
        boolean isImpactModId = modId.startsWith(IMPACT_MOD_ID_PREFIX) && modId.length() > IMPACT_MOD_ID_PREFIX.length();
        return isImpactModId ? modId.substring(IMPACT_MOD_ID_PREFIX.length()) : modId;
    }

    private static void appendPair(StringBuilder line, Object[] keyValues, int keyIndex) {
        int valueIndex = keyIndex + 1;
        Object value = valueIndex < keyValues.length ? keyValues[valueIndex] : MISSING_VALUE;
        line.append(PAIR_SEPARATOR).append(keyValues[keyIndex]).append(KEY_VALUE_SEPARATOR).append(value);
    }
}
