package com.impact.realisticvitality.temperature;

import com.impact.realisticvitality.config.VitalityServerConfig;

/** The named bands of body temperature, from Freezing to Overheating, and what each does. */
public enum BodyState {
    FREEZING("freezing"),
    VERY_COLD("very_cold"),
    COLD("cold"),
    CHILLY("chilly"),
    COMFORTABLE("comfortable"),
    WARM("warm"),
    HOT("hot"),
    VERY_HOT("very_hot"),
    OVERHEATING("overheating");

    private static final float EXTREME = 100.0F;

    /** The name in logs, triggers and translation keys. */
    public final String serializedName;

    BodyState(String serializedName) {
        this.serializedName = serializedName;
    }

    /** The band for a body temperature, with the edges from the server config (25, 50 and 75 by default, mirrored below zero). */
    public static BodyState of(float body) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        double warm = settings.warmAt.get();
        double hot = settings.hotAt.get();
        double veryHot = settings.veryHotAt.get();
        if (body <= -EXTREME) {
            return FREEZING;
        }
        if (body <= -veryHot) {
            return VERY_COLD;
        }
        if (body <= -hot) {
            return COLD;
        }
        if (body <= -warm) {
            return CHILLY;
        }
        if (body < warm) {
            return COMFORTABLE;
        }
        if (body < hot) {
            return WARM;
        }
        if (body < veryHot) {
            return HOT;
        }
        return body < EXTREME ? VERY_HOT : OVERHEATING;
    }

    public boolean isColdOrWorse() {
        return compareTo(COLD) <= 0;
    }

    public boolean isVeryColdOrWorse() {
        return compareTo(VERY_COLD) <= 0;
    }

    public boolean isHotOrWorse() {
        return compareTo(HOT) >= 0;
    }

    public boolean isVeryHotOrWorse() {
        return compareTo(VERY_HOT) >= 0;
    }

    /** Hot drains thirst 1.5 times as fast; Very Hot and Overheating twice as fast. */
    public double thirstMultiplier() {
        VitalityServerConfig.ThirstSettings settings = VitalityServerConfig.get().thirst;
        if (isVeryHotOrWorse()) {
            return settings.veryHotDrainMultiplier.get();
        }
        return this == HOT ? settings.hotDrainMultiplier.get() : 1.0;
    }

    /** States that pulse on the gauge: Cold and Hot and beyond. */
    public boolean isWarning() {
        return isColdOrWorse() || isHotOrWorse();
    }
}
