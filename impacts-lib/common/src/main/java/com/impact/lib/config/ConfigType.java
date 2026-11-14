package com.impact.lib.config;

/** The three kinds of config file an Impact mod can have. See Lib's spec, "Configuration". */
public enum ConfigType {
    /** Feature and recipe toggles, anything a recipe condition reads, and debug settings. Global. */
    COMMON("common"),
    /** Every gameplay number. Stored per world and synced to clients. */
    SERVER("server"),
    /** HUD, sound and display options. */
    CLIENT("client");

    private final String fileSuffix;

    ConfigType(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String fileSuffix() {
        return fileSuffix;
    }
}
