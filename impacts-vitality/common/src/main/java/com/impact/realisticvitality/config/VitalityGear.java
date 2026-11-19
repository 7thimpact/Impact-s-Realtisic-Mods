package com.impact.realisticvitality.config;

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
    CHARCOAL_FILTER("charcoalFilter", VitalityNeed.AIR),
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
    /** The need the gear answers; turning the need off turns its gear off too. */
    public final VitalityNeed need;

    VitalityGear(String configKey, VitalityNeed need) {
        this.configKey = configKey;
        this.need = need;
        this.itemPath = name().toLowerCase(Locale.ROOT);
    }
}
