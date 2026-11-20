package com.impact.realisticvitality.player;

import net.minecraft.nbt.CompoundTag;

/** The thirst bar: 20 points, with Hydration (like saturation) that drains first, and the exhaustion that wears them down. */
public final class ThirstState {
    public static final int FULL = 20;
    /** Hydration a new or respawned player starts with, as vanilla starts saturation at 5. */
    public static final float STARTING_HYDRATION = 5.0F;
    private static final String LEVEL_KEY = "Level";
    private static final String HYDRATION_KEY = "Hydration";
    private static final String EXHAUSTION_KEY = "Exhaustion";
    private static final String RESTING_TICKS_KEY = "RestingTicks";
    private static final String DAMAGE_TICKS_KEY = "DamageTicks";

    private int level = FULL;
    private float hydration = STARTING_HYDRATION;
    private float exhaustion;
    private int restingTicks;
    private int damageTicks;
    // Not saved: only used to log changes.
    private int loggedStep = -1;
    private boolean wasParched;

    public int level() {
        return level;
    }

    public float hydration() {
        return hydration;
    }

    public float exhaustion() {
        return exhaustion;
    }

    public void setLevel(int newLevel) {
        level = Math.max(0, Math.min(FULL, newLevel));
        hydration = Math.min(hydration, level);
    }

    public void setHydration(float newHydration) {
        hydration = Math.max(0.0F, Math.min(newHydration, level));
    }

    public void setExhaustion(float newExhaustion) {
        exhaustion = Math.max(0.0F, newExhaustion);
    }

    public int restingTicks() {
        return restingTicks;
    }

    public void setRestingTicks(int ticks) {
        restingTicks = ticks;
    }

    public int damageTicks() {
        return damageTicks;
    }

    public void setDamageTicks(int ticks) {
        damageTicks = ticks;
    }

    public int loggedStep() {
        return loggedStep;
    }

    public void setLoggedStep(int step) {
        loggedStep = step;
    }

    public boolean wasParched() {
        return wasParched;
    }

    public void setWasParched(boolean isParched) {
        wasParched = isParched;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(LEVEL_KEY, level);
        tag.putFloat(HYDRATION_KEY, hydration);
        tag.putFloat(EXHAUSTION_KEY, exhaustion);
        tag.putInt(RESTING_TICKS_KEY, restingTicks);
        tag.putInt(DAMAGE_TICKS_KEY, damageTicks);
        return tag;
    }

    void load(CompoundTag tag) {
        level = tag.contains(LEVEL_KEY) ? tag.getInt(LEVEL_KEY) : FULL;
        hydration = tag.contains(HYDRATION_KEY) ? tag.getFloat(HYDRATION_KEY) : STARTING_HYDRATION;
        exhaustion = tag.getFloat(EXHAUSTION_KEY);
        restingTicks = tag.getInt(RESTING_TICKS_KEY);
        damageTicks = tag.getInt(DAMAGE_TICKS_KEY);
    }
}
