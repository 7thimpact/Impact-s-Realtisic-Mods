package com.impact.lib.dump;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** One mod's part of {@code /impacts dump block}: what that mod knows about the block looked at. Empty to leave the section out. */
@FunctionalInterface
public interface BlockDumpSection {
    Map<String, Object> describe(ServerLevel level, BlockPos position, BlockState state);
}
