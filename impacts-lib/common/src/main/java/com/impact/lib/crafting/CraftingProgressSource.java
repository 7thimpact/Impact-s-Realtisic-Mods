package com.impact.lib.crafting;

import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

/** Reports the progress of a timed craft in a menu, or empty when there's none. */
@FunctionalInterface
public interface CraftingProgressSource {
    Optional<CraftingProgress.Progress> progressOf(AbstractContainerMenu menu);
}
