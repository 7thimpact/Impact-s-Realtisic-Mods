package com.impact.lib.water;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A mod's blocks that hold water a bottle can be filled from, like Food's campfire bucket. */
public interface WaterVesselProvider {
    /** Whether the block here is this mod's vessel and a bottle could be filled from it now. Asked on both sides; the client guesses. */
    boolean canFillBottle(Level level, BlockPos position);

    /** Takes one bottle's worth of water out. Server side only. False, changing nothing, when it can't. */
    boolean takeBottle(Level level, BlockPos position);
}
