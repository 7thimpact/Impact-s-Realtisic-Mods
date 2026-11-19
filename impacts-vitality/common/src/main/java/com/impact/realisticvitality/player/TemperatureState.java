package com.impact.realisticvitality.player;

import net.minecraft.nbt.CompoundTag;

/** Body temperature, from -100 (freezing) to +100 (overheating) with 0 comfortable, and how long the player stays Wet. */
public final class TemperatureState {
    public static final float COLDEST = -100.0F;
    public static final float HOTTEST = 100.0F;
    private static final String BODY_KEY = "Body";
    private static final String WET_TICKS_KEY = "WetTicks";

    private float body;
    private int wetTicks;

    public float body() {
        return body;
    }

    public void setBody(float newBody) {
        body = Math.max(COLDEST, Math.min(HOTTEST, newBody));
    }

    public int wetTicks() {
        return wetTicks;
    }

    public boolean isWet() {
        return wetTicks > 0;
    }

    public void setWetTicks(int ticks) {
        wetTicks = Math.max(0, ticks);
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat(BODY_KEY, body);
        tag.putInt(WET_TICKS_KEY, wetTicks);
        return tag;
    }

    void load(CompoundTag tag) {
        body = tag.getFloat(BODY_KEY);
        wetTicks = tag.getInt(WET_TICKS_KEY);
    }
}
