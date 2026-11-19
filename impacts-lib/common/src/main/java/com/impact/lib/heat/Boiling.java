package com.impact.lib.heat;

import com.impact.lib.ImpactsLib;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Whether water is boiling at a position, for mods that care without knowing who boils it (Vitality fills Purified
 * Water from a boiling pot). Without a provider (no Food installed) the answer is always false.
 */
public final class Boiling {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final List<BoilingProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private Boiling() {
    }

    public static void registerBoilingProvider(String modId, BoilingProvider provider) {
        PROVIDERS.add(provider);
        LOG.debug(LibLogCategories.PROVIDERS, "boiling provider registered", "mod", modId);
    }

    public static boolean isBoiling(Level level, BlockPos position) {
        return PROVIDERS.stream().anyMatch(provider -> provider.isBoiling(level, position));
    }
}
