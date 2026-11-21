package com.impact.realisticvitality.thirst;

import java.util.Arrays;
import java.util.Optional;

/** How clean water is, from best to worst. Mixing two waters gives the worse one: one sip of salt water spoils a canteen. */
public enum WaterPurity {
    PURIFIED("purified"),
    UNCLEAN("unclean"),
    SALT("salt");

    /** The name saved in NBT and written in logs. */
    public final String serializedName;

    WaterPurity(String serializedName) {
        this.serializedName = serializedName;
    }

    public WaterPurity mixedWith(WaterPurity other) {
        return ordinal() >= other.ordinal() ? this : other;
    }

    /** Unclean water can make you Parched; purified water never does, and salt water hurts in its own way. */
    public boolean canMakeParched() {
        return this == UNCLEAN;
    }

    public static Optional<WaterPurity> bySerializedName(String name) {
        return Arrays.stream(values()).filter(purity -> purity.serializedName.equals(name)).findFirst();
    }
}
