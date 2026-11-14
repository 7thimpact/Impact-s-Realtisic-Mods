package com.impact.lib.config;

/**
 * One value in an Impact config file. Mods read values at the moment they use them, so changes saved to
 * the file apply without a restart.
 */
public interface ConfigValue<T> {
    /** The current value, or the default while the file is not loaded yet. */
    T get();

    T defaultValue();

    /** Changes the value and saves the file. The file must be loaded. */
    void set(T value);

    /** The value's full dotted key, for example {@code debug.logFile.sizeMb}. */
    String key();

    default boolean isDefault() {
        return get().equals(defaultValue());
    }
}
