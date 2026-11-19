package com.impact.realisticfood.boiling;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Food's answer to Lib's {@code ImpactsLib.isBoiling}: every kind of pot Food has. */
public final class PotBoiling {
    private PotBoiling() {
    }

    public static boolean isBoiling(Level level, BlockPos position) {
        if (level instanceof ServerLevel serverLevel) {
            return CauldronPots.isBoiling(serverLevel, position);
        }
        return ClientBoilingPots.isBoiling(level, position);
    }
}
