package com.impact.realisticvitality.config;

import java.util.List;
import java.util.Locale;

/** Every piece of water, breathing and clothing gear, each with its own switch in the common file (it turns off its recipe). */
public enum VitalityGear {
    WOODEN_CUP("woodenCup", VitalityNeed.THIRST),
    WATERSKIN("waterskin", VitalityNeed.THIRST),
    CANTEEN("canteen", VitalityNeed.THIRST),
    INSULATED_FLASK("insulatedFlask", VitalityNeed.THIRST),
    TIDEBOUND_CANTEEN("tideboundCanteen", VitalityNeed.THIRST),
    HYDRATION_PACK("hydrationPack", VitalityNeed.THIRST),
    RAIN_BARREL("rainBarrel", VitalityNeed.THIRST),
    WATER_FILTER("waterFilter", VitalityNeed.THIRST),
    COPPER_STILL("copperStill", VitalityNeed.THIRST),
    CLOTH_MASK("clothMask", VitalityNeed.AIR),
    AIR_BLADDER("airBladder", VitalityNeed.AIR),
    BOTTLED_AIR("bottledAir", VitalityNeed.AIR),
    RESPIRATOR("respirator", VitalityNeed.AIR),
    CHARCOAL_FILTER("charcoalFilter", VitalityNeed.AIR, VitalityNeed.THIRST),
    AIR_TANK("airTank", VitalityNeed.AIR),
    REBREATHER("rebreather", VitalityNeed.AIR),
    NETHERITE_REBREATHER("netheriteRebreather", VitalityNeed.AIR),
    SHULKER_AIR_TANK("shulkerAirTank", VitalityNeed.AIR),
    AIR_COMPRESSOR("airCompressor", VitalityNeed.AIR),
    STRAW_HAT("strawHat", VitalityNeed.TEMPERATURE),
    THERMOMETER("thermometer", VitalityNeed.TEMPERATURE);

    /** The config key under {@code gear}. */
    public final String configKey;
    /** The item's registry path, which is also the name the {@code impactsvitality:feature_enabled} condition uses. */
    public final String itemPath;
    /** The needs the gear answers; it stays on while any of them is on (the Charcoal Filter serves the Respirator and the Water Filter). */
    public final List<VitalityNeed> needs;

    VitalityGear(String configKey, VitalityNeed... needs) {
        this.configKey = configKey;
        this.needs = List.of(needs);
        this.itemPath = name().toLowerCase(Locale.ROOT);
    }
}
