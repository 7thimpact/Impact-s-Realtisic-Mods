package com.impact.lib.forge.config;

import com.impact.lib.config.ConfigValue;
import com.impact.lib.log.ImpactsLog;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One Impact config value backed by a Forge config value. Forge already resets values it can't read to their
 * default; this class adds the range check, so an out-of-range value also falls back to its default (Forge
 * would clamp it) and a warning names the key, the rejected value and the allowed range.
 */
final class ForgeConfigValue<T> implements ConfigValue<T> {
    private final ForgeConfigFile file;
    private final String key;
    private final ForgeConfigSpec.ConfigValue<?> forgeValue;
    private final T defaultValue;
    private final Function<Object, T> storedValueReader;
    private final Predicate<T> isInAllowedRange;
    private final String allowedRange;
    private volatile Object lastRejectedValue;

    ForgeConfigValue(ForgeConfigFile file, String key, ForgeConfigSpec.ConfigValue<?> forgeValue, T defaultValue,
                     Function<Object, T> storedValueReader, Predicate<T> isInAllowedRange, String allowedRange) {
        this.file = file;
        this.key = key;
        this.forgeValue = forgeValue;
        this.defaultValue = defaultValue;
        this.storedValueReader = storedValueReader;
        this.isInAllowedRange = isInAllowedRange;
        this.allowedRange = allowedRange;
    }

    @Override
    public T get() {
        // Forge throws when a value is read before its file loads; Impact code gets the default instead.
        if (!file.isLoaded()) {
            return defaultValue;
        }
        T storedValue = storedValueReader.apply(forgeValue.get());
        if (isInAllowedRange.test(storedValue)) {
            return storedValue;
        }
        warnOnceAboutRejectedValue(storedValue);
        return defaultValue;
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(T value) {
        if (!file.isLoaded()) {
            throw new IllegalStateException("Config value " + key + " in " + file.fileName() + " was set before the file loaded.");
        }
        // Each value is stored as the type it was defined with, so this cast always holds.
        ForgeConfigSpec.ConfigValue<Object> storedValue = (ForgeConfigSpec.ConfigValue<Object>) forgeValue;
        storedValue.set(value);
        storedValue.save();
    }

    @Override
    public String key() {
        return key;
    }

    private void warnOnceAboutRejectedValue(T storedValue) {
        if (storedValue.equals(lastRejectedValue)) {
            return;
        }
        lastRejectedValue = storedValue;
        ImpactsLog.of(file.modId()).warn(ImpactsLog.CONFIG_CATEGORY, "value out of range, using default",
                "file", file.fileName(), "key", key, "value", storedValue, "allowed", allowedRange, "default", defaultValue);
    }
}
