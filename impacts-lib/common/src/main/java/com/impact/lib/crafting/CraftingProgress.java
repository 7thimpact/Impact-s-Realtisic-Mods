package com.impact.lib.crafting;

import com.impact.lib.ImpactsLib;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The progress of a timed craft in a menu. Impact's Realistic Crafting writes it; any screen (vanilla, Inventory's,
 * or another mod's) can draw it with {@code CraftingProgressRenderer}. Without a source, {@link #get} is empty.
 */
public final class CraftingProgress {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final List<CraftingProgressSource> SOURCES = new CopyOnWriteArrayList<>();

    private CraftingProgress() {
    }

    public static void registerSource(String modId, CraftingProgressSource source) {
        SOURCES.add(source);
        LOG.debug(LibLogCategories.PROVIDERS, "crafting progress source registered", "mod", modId);
    }

    /** The running craft's progress in this menu, or empty when no craft is running or no mod times crafts. */
    public static Optional<Progress> get(AbstractContainerMenu menu) {
        return SOURCES.stream()
                .map(source -> source.progressOf(menu))
                .flatMap(Optional::stream)
                .findFirst();
    }

    /** How far a craft has run, in ticks. */
    public record Progress(int elapsedTicks, int totalTicks) {
        public float fraction() {
            if (totalTicks <= 0) {
                return 1.0F;
            }
            return Math.min(1.0F, (float) elapsedTicks / totalTicks);
        }

        public boolean isFinished() {
            return elapsedTicks >= totalTicks;
        }
    }
}
