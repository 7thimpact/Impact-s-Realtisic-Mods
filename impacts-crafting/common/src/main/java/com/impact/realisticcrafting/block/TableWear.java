package com.impact.realisticcrafting.block;

import com.impact.realisticcrafting.config.CraftingServerConfig;

/** A makeshift table's wear stage, like the anvil's: new, worn (below 2/3 of its uses) and falling apart (below 1/3). */
public final class TableWear {
    public static final int NEW = 0;
    public static final int WORN = 1;
    public static final int FALLING_APART = 2;

    private TableWear() {
    }

    public static int stageFor(int usesLeft, int maxUses) {
        CraftingServerConfig.TableSettings settings = CraftingServerConfig.get().tables;
        double shareLeft = maxUses <= 0 ? 0.0 : (double) usesLeft / maxUses;
        if (shareLeft < settings.fallingApartBelow.get()) {
            return FALLING_APART;
        }
        return shareLeft < settings.wornBelow.get() ? WORN : NEW;
    }
}
