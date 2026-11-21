package com.impact.lib.gametest;

import com.impact.lib.config.ConfigValue;

/**
 * Runs a GameTest check with a config value changed in memory only. Never {@code set} config values in tests: saving
 * the file makes the loader reload it on another thread, which can put a test's value back after the test ends.
 */
public final class ConfigOverrides {
    private ConfigOverrides() {
    }

    /** Runs the check with the value overridden, then lifts the override. The check must finish before this returns. */
    public static <T> void withValue(ConfigValue<T> setting, T value, Runnable check) {
        setting.overrideInMemory(value);
        try {
            check.run();
        } finally {
            setting.clearOverride();
        }
    }
}
