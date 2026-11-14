package com.impact.lib.config;

import com.impact.lib.log.ImpactsLog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Writes the config lines the specs ask for. When a file loads: every value that differs from its default.
 * When it reloads: every value that changed since the previous load. Reading each value also triggers its
 * out-of-range warning, so bad values are reported as soon as the file loads.
 */
public final class ConfigLoadLog {
    private final ConfigFile file;
    private final ImpactsLog log;
    private Map<String, Object> valuesByKeyAtLastLoad;

    public ConfigLoadLog(ConfigFile file) {
        this.file = file;
        this.log = ImpactsLog.of(file.modId());
    }

    /** The file was loaded or reloaded from disk. */
    public synchronized void onFileLoaded() {
        Map<String, Object> currentValuesByKey = readValuesByKey();
        boolean isFirstLoad = valuesByKeyAtLastLoad == null;
        if (isFirstLoad) {
            logValuesChangedFromDefault();
        } else {
            logValuesChangedSince(valuesByKeyAtLastLoad, currentValuesByKey);
        }
        valuesByKeyAtLastLoad = currentValuesByKey;
    }

    /** The file was unloaded, for example when a world closes. Its next load counts as a first load again. */
    public synchronized void onFileUnloaded() {
        valuesByKeyAtLastLoad = null;
    }

    private Map<String, Object> readValuesByKey() {
        Map<String, Object> valuesByKey = new LinkedHashMap<>();
        file.values().forEach(value -> valuesByKey.put(value.key(), value.get()));
        return valuesByKey;
    }

    private void logValuesChangedFromDefault() {
        if (!log.isEnabled(ImpactsLog.STARTUP_CATEGORY)) {
            return;
        }
        file.values().stream()
                .filter(value -> !value.isDefault())
                .forEach(value -> log.debug(ImpactsLog.STARTUP_CATEGORY, "config value differs from default",
                        "file", file.fileName(), "key", value.key(), "value", value.get(), "default", value.defaultValue()));
    }

    private void logValuesChangedSince(Map<String, Object> previousValuesByKey, Map<String, Object> currentValuesByKey) {
        currentValuesByKey.forEach((key, currentValue) -> {
            Object previousValue = previousValuesByKey.get(key);
            if (Objects.equals(previousValue, currentValue)) {
                return;
            }
            log.debug(ImpactsLog.CONFIG_CATEGORY, "config value changed",
                    "file", file.fileName(), "key", key, "from", previousValue, "to", currentValue);
        });
    }
}
