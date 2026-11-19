package com.impact.realisticvitality.config;

/** The three needs. Each can be turned off on its own in the common file, along with its gear and recipes. */
public enum VitalityNeed {
    THIRST("thirst"),
    TEMPERATURE("temperature"),
    AIR("air");

    public final String configKey;

    VitalityNeed(String configKey) {
        this.configKey = configKey;
    }
}
