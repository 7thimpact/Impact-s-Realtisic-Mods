package com.impact.lib.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Answers whether the water at a position is boiling. Food registers one for cauldrons, campfire buckets and stone-boiling pots. */
@FunctionalInterface
public interface BoilingProvider {
    boolean isBoiling(Level level, BlockPos position);
}
