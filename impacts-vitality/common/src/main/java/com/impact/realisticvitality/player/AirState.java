package com.impact.realisticvitality.player;

import net.minecraft.nbt.CompoundTag;

/** Breathing outside water: the ticks until the next bubble is lost to bad air, and the damage timer once bubbles run out. */
public final class AirState {
    private static final String DRAIN_TICKS_KEY = "DrainTicks";
    private static final String DAMAGE_TICKS_KEY = "DamageTicks";

    private int drainTicks;
    private int damageTicks;

    public int drainTicks() {
        return drainTicks;
    }

    public void setDrainTicks(int ticks) {
        drainTicks = ticks;
    }

    public int damageTicks() {
        return damageTicks;
    }

    public void setDamageTicks(int ticks) {
        damageTicks = ticks;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(DRAIN_TICKS_KEY, drainTicks);
        tag.putInt(DAMAGE_TICKS_KEY, damageTicks);
        return tag;
    }

    void load(CompoundTag tag) {
        drainTicks = tag.getInt(DRAIN_TICKS_KEY);
        damageTicks = tag.getInt(DAMAGE_TICKS_KEY);
    }
}
