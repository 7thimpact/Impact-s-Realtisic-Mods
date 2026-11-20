package com.impact.lib.dump;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The sections behind {@code /impacts dump player <name>}, {@code dump held} and {@code dump block}. Lib adds the
 * basics; each Impact mod adds its own section (thirst, temperature, back slot, an item's tier, a table's wear, ...).
 */
public final class ImpactsDump {
    private static final List<NamedSection> PLAYER_SECTIONS = new CopyOnWriteArrayList<>();
    private static final List<NamedItemSection> ITEM_SECTIONS = new CopyOnWriteArrayList<>();
    private static final List<NamedBlockSection> BLOCK_SECTIONS = new CopyOnWriteArrayList<>();

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

    /** Adds a section to {@code /impacts dump held}. */
    public static void registerItemSection(String sectionName, ItemDumpSection section) {
        ITEM_SECTIONS.add(new NamedItemSection(sectionName, section));
    }

    /** Adds a section to {@code /impacts dump block}. */
    public static void registerBlockSection(String sectionName, BlockDumpSection section) {
        BLOCK_SECTIONS.add(new NamedBlockSection(sectionName, section));
    }

    /** The held-item sections that have something to say about the stack. */
    public static List<DumpSection> describeItem(ItemStack stack, ServerPlayer player) {
        return ITEM_SECTIONS.stream()
                .map(namedSection -> new DumpSection(namedSection.name(), namedSection.section().describe(stack, player)))
                .filter(section -> !section.entries().isEmpty())
                .toList();
    }

    /** The block sections that have something to say about the block. */
    public static List<DumpSection> describeBlock(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return BLOCK_SECTIONS.stream()
                .map(namedSection -> new DumpSection(namedSection.name(), namedSection.section().describe(level, position, state)))
                .filter(section -> !section.entries().isEmpty())
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

    private record NamedItemSection(String name, ItemDumpSection section) {
    }

    private record NamedBlockSection(String name, BlockDumpSection section) {
    }
}
