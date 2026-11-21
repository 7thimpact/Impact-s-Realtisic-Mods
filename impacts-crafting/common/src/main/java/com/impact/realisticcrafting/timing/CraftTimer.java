package com.impact.realisticcrafting.timing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.DataSlot;

/**
 * One crafting grid's timer: the ticks the current craft has run and the ticks it takes, synced to the client through
 * two data slots, so the arrow fills and the output stays locked on both sides. A timer with no total is idle: there's
 * no craft, or the grid isn't timed.
 */
public final class CraftTimer {
    // Data slots sync as shorts.
    private static final int MAXIMUM_TICKS = Short.MAX_VALUE;

    private int elapsedTicks;
    private int totalTicks;
    private boolean isRepeatQueued;
    private ResourceLocation recipeId;

    public final DataSlot elapsedSlot = new DataSlot() {
        @Override
        public int get() {
            return elapsedTicks;
        }

        @Override
        public void set(int value) {
            elapsedTicks = value;
        }
    };

    public final DataSlot totalSlot = new DataSlot() {
        @Override
        public int get() {
            return totalTicks;
        }

        @Override
        public void set(int value) {
            totalTicks = value;
        }
    };

    public int elapsedTicks() {
        return elapsedTicks;
    }

    public int totalTicks() {
        return totalTicks;
    }

    /** The recipe the timer last started for, or null. */
    public ResourceLocation recipeId() {
        return recipeId;
    }

    public void start(ResourceLocation craftedRecipeId, int ticks) {
        recipeId = craftedRecipeId;
        elapsedTicks = 0;
        totalTicks = Math.min(MAXIMUM_TICKS, Math.max(0, ticks));
    }

    public void clear() {
        recipeId = null;
        elapsedTicks = 0;
        totalTicks = 0;
    }

    public boolean isRunning() {
        return totalTicks > 0 && elapsedTicks < totalTicks;
    }

    public boolean hasCraft() {
        return totalTicks > 0;
    }

    /** One tick. Returns true on the tick the craft finishes. */
    public boolean advance() {
        if (!isRunning()) {
            return false;
        }
        elapsedTicks++;
        return elapsedTicks >= totalTicks;
    }

    /** Shift-clicking the output asks for repeated crafts: each one is taken when its timer finishes. */
    public boolean isRepeatQueued() {
        return isRepeatQueued;
    }

    public void setRepeatQueued(boolean isQueued) {
        isRepeatQueued = isQueued;
    }
}
