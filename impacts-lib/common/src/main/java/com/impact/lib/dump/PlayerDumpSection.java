package com.impact.lib.dump;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/** One mod's part of {@code /impacts dump player}: named values describing the player, in display order. */
@FunctionalInterface
public interface PlayerDumpSection {
    Map<String, Object> describe(ServerPlayer player);
}
