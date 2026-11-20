package com.impact.realisticvitality.player;

import com.impact.realisticvitality.air.AirQuality;
import net.minecraft.nbt.CompoundTag;

/**
 * Breathing outside water: the ticks until the next bubble is lost to bad air, the damage timer once bubbles run out,
 * and (not saved, read again within a second) the air quality around the player and whether bubbles are draining.
 */
public final class AirState {
    private static final String DRAIN_TICKS_KEY = "DrainTicks";
    private static final String DAMAGE_TICKS_KEY = "DamageTicks";

    private int drainTicks;
    private int damageTicks;
    private AirQuality quality = AirQuality.GOOD;
    private boolean isDraining;

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

    public AirQuality quality() {
        return quality;
    }

    public void setQuality(AirQuality newQuality) {
        quality = newQuality;
    }

    public boolean isDraining() {
        return isDraining;
    }

    public void setDraining(boolean draining) {
        isDraining = draining;
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
