package com.impact.lib.log;

import com.impact.lib.ImpactsLib;
import net.minecraft.core.Vec3i;

/**
 * Formats Impact log lines: {@code [Impact/<mod>/<category>] <event> key=value ...}. The key-value arguments
 * alternate between a key and its value. Block positions print as {@code x,y,z}, and a value with spaces in it is
 * quoted, so every pair stays one word.
 */
public final class LogLineFormatter {
    private static final String LINE_START = "[Impact/";
    private static final char PATH_SEPARATOR = '/';
    private static final String TAG_END = "] ";
    private static final char PAIR_SEPARATOR = ' ';
    private static final char KEY_VALUE_SEPARATOR = '=';
    private static final String MISSING_VALUE = "<missing>";
    private static final char QUOTE = '"';
    private static final String POSITION_SEPARATOR = ",";

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
        String prefix = ImpactsLib.MOD_ID_PREFIX;
        boolean isImpactModId = modId.startsWith(prefix) && modId.length() > prefix.length();
        return isImpactModId ? modId.substring(prefix.length()) : modId;
    }

    private static void appendPair(StringBuilder line, Object[] keyValues, int keyIndex) {
        int valueIndex = keyIndex + 1;
        Object value = valueIndex < keyValues.length ? keyValues[valueIndex] : MISSING_VALUE;
        line.append(PAIR_SEPARATOR).append(keyValues[keyIndex]).append(KEY_VALUE_SEPARATOR).append(valueText(value));
    }

    private static String valueText(Object value) {
        if (value instanceof Vec3i position) {
            return position.getX() + POSITION_SEPARATOR + position.getY() + POSITION_SEPARATOR + position.getZ();
        }
        String text = String.valueOf(value);
        boolean hasSpace = text.chars().anyMatch(Character::isWhitespace);
        return hasSpace ? QUOTE + text + QUOTE : text;
    }
}
