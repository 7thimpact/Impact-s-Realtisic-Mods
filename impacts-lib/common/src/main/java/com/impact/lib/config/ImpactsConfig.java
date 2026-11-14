package com.impact.lib.config;

import com.impact.lib.platform.LoaderPlatform;

/**
 * Entry point of Lib's config framework. Every Impact mod's files share one folder named after Lib:
 * {@code config/impactslib/} holds COMMON and CLIENT files, {@code <world>/serverconfig/impactslib/} holds
 * SERVER files, and pack makers' defaults go in {@code defaultconfigs/impactslib/}.
 */
public final class ImpactsConfig {
    public static final String FOLDER_NAME = "impactslib";
    private static final String FILE_EXTENSION = ".toml";

    private ImpactsConfig() {
    }

    public static ConfigSpecBuilder builder(String modId, ConfigType type) {
        return LoaderPlatform.current().configBuilder(modId, type);
    }

    /** The file's path inside the config folder, for example {@code impactslib/impactsinventory-server.toml}. */
    public static String fileName(String modId, ConfigType type) {
        return FOLDER_NAME + "/" + modId + "-" + type.fileSuffix() + FILE_EXTENSION;
    }
}
