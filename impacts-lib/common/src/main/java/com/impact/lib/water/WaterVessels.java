package com.impact.lib.water;

import com.impact.lib.ImpactsLib;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Blocks that hold water without being a water block or a vanilla cauldron, asked by mods that fill bottles. Food
 * answers for its campfire bucket; Vitality fills bottles and containers from it, with the purity Lib's
 * {@code isBoiling} decides. Neither mod sees the other's classes.
 */
public final class WaterVessels {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final List<WaterVesselProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private WaterVessels() {
    }

    public static void registerProvider(String modId, WaterVesselProvider provider) {
        PROVIDERS.add(provider);
        LOG.debug(LibLogCategories.PROVIDERS, "water vessel provider registered", "mod", modId);
    }

    public static boolean canFillBottle(Level level, BlockPos position) {
        return PROVIDERS.stream().anyMatch(provider -> provider.canFillBottle(level, position));
    }

    /** Takes one bottle's worth of water from whichever mod's vessel is here. False when none could give it. */
    public static boolean takeBottle(Level level, BlockPos position) {
        return PROVIDERS.stream().anyMatch(provider -> provider.takeBottle(level, position));
    }
}
