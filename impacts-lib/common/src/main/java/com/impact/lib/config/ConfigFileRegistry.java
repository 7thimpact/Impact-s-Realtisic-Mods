package com.impact.lib.config;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Every Impact config file registered so far, so Lib's commands can find them. */
public final class ConfigFileRegistry {
    private static final List<ConfigFile> REGISTERED_FILES = new CopyOnWriteArrayList<>();

    private ConfigFileRegistry() {
    }

    /** Called by each loader's config builder when it registers a file. */
    public static void add(ConfigFile file) {
        REGISTERED_FILES.add(file);
    }

    public static Optional<ConfigFile> find(String modId, ConfigType type) {
        return REGISTERED_FILES.stream()
                .filter(file -> file.modId().equals(modId) && file.type() == type)
                .findFirst();
    }

    public static List<ConfigFile> ofType(ConfigType type) {
        return REGISTERED_FILES.stream().filter(file -> file.type() == type).toList();
    }
}
