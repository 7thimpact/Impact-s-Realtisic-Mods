package com.impact.realisticinventory.inventory;

import java.util.Set;

/** Finds the class that called into the inventory, for the log, by skipping Minecraft's and Inventory's own frames. */
public final class CallerFinder {
    private static final Set<String> SKIPPED_PREFIXES = Set.of(
            "net.minecraft.world.entity.player.Inventory",
            "com.impact.realisticinventory.inventory.",
            "com.impact.realisticinventory.mixin.",
            "com.impact.lib.item.",
            "java.", "jdk.", "sun.");
    private static final String UNKNOWN_CALLER = "unknown";

    private CallerFinder() {
    }

    public static String outsideCaller() {
        return StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(className -> SKIPPED_PREFIXES.stream().noneMatch(className::startsWith))
                .findFirst()
                .orElse(UNKNOWN_CALLER));
    }
}
