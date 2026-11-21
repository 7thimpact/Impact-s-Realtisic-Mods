package com.impact.lib.config;

import java.util.Optional;

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

    /**
     * Holds this value in memory, leaving the file alone, until {@link #clearOverride()}. For GameTests: saving the
     * file makes the loader reload it on another thread, which can put an older value back.
     */
    void overrideInMemory(T value);

    void clearOverride();

    /** The value's full dotted key, for example {@code debug.logFile.sizeMb}. */
    String key();

    /** Reads a value typed by a person, for example in {@code /impacts config}. Empty when the text isn't valid for this type. */
    Optional<T> parse(String text);

    /** Whether the value is inside this value's allowed range. */
    boolean isAllowed(T value);

    /** The allowed values in words, for example {@code 1 ~ 1024} or {@code true or false}. */
    String allowedValues();

    default boolean isDefault() {
        return get().equals(defaultValue());
    }
}
