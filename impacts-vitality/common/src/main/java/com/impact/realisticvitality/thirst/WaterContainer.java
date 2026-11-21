package com.impact.realisticvitality.thirst;

import com.impact.realisticvitality.config.VitalityGear;

/** The five stages of drink containers. Each remembers its water's purity and how many drinks are left. */
public enum WaterContainer {
    WOODEN_CUP(VitalityGear.WOODEN_CUP, 1, false, false, false),
    WATERSKIN(VitalityGear.WATERSKIN, 3, false, false, false),
    CANTEEN(VitalityGear.CANTEEN, 5, false, false, false),
    INSULATED_FLASK(VitalityGear.INSULATED_FLASK, 8, true, false, false),
    TIDEBOUND_CANTEEN(VitalityGear.TIDEBOUND_CANTEEN, 8, false, true, false),
    HYDRATION_PACK(VitalityGear.HYDRATION_PACK, 24, false, false, true);

    public final VitalityGear gear;
    public final String itemPath;
    public final int defaultCapacity;
    /** Cold Water stays cooling and soups stay warming. */
    public final boolean keepsTemperature;
    /** Refills itself with purified water. */
    public final boolean refillsItself;
    /** Worn in the back slot (or the chest slot without one), and sipped with a key. */
    public final boolean isWorn;

    WaterContainer(VitalityGear gear, int defaultCapacity, boolean keepsTemperature, boolean refillsItself, boolean isWorn) {
        this.gear = gear;
        this.itemPath = gear.itemPath;
        this.defaultCapacity = defaultCapacity;
        this.keepsTemperature = keepsTemperature;
        this.refillsItself = refillsItself;
        this.isWorn = isWorn;
    }
}
