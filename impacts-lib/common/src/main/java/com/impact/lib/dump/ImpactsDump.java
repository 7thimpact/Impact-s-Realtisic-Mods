package com.impact.lib.dump;

import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The sections behind {@code /impacts dump player <name>}. Lib adds the player basics; each Impact mod adds
 * its own section (thirst, temperature, active slot, back slot, counters, ...).
 */
public final class ImpactsDump {
    private static final List<NamedSection> PLAYER_SECTIONS = new CopyOnWriteArrayList<>();

    private ImpactsDump() {
    }

    /** Adds a section to the player dump. Sections appear in the order they were registered. */
    public static void registerPlayerSection(String sectionName, PlayerDumpSection section) {
        PLAYER_SECTIONS.add(new NamedSection(sectionName, section));
    }

    public static List<DumpSection> describePlayer(ServerPlayer player) {
        return PLAYER_SECTIONS.stream()
                .map(namedSection -> new DumpSection(namedSection.name(), namedSection.section().describe(player)))
                .toList();
    }

    /** Builds a section's entries in display order from alternating keys and values. */
    public static Map<String, Object> entries(Object... keysAndValues) {
        Map<String, Object> entries = new LinkedHashMap<>();
        for (int keyIndex = 0; keyIndex + 1 < keysAndValues.length; keyIndex += 2) {
            entries.put(String.valueOf(keysAndValues[keyIndex]), keysAndValues[keyIndex + 1]);
        }
        return entries;
    }

    private record NamedSection(String name, PlayerDumpSection section) {
    }
}
