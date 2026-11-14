package com.impact.lib.config;

import com.impact.lib.ImpactsLib;

/** Lib's own COMMON file: {@code config/impactslib/impactslib-common.toml}. */
public final class LibCommonConfig {
    private static volatile LibCommonConfig registered;

    /** Lib's own debug switches (debug.enabled, debug.categories, debug.verbose). */
    public final DebugConfigSection debug;
    /** debug.allMods: the master switch over every Impact mod's debug lines. */
    public final ConfigValue<Boolean> isFamilyDebugOn;
    /** debug.logFile.*: the rolling logs/impacts-debug.log file. */
    public final LogFileSettings logFile;
    /** commands.*: the /impacts commands. */
    public final CommandSettings commands;
    public final ConfigFile file;

    private LibCommonConfig(ConfigSpecBuilder builder) {
        builder.pushSection(DebugConfigSection.SECTION_NAME, "Debug logging. See Impact's Lib, \"Debug logging\".");
        debug = DebugConfigSection.defineInCurrentSection(builder);
        isFamilyDebugOn = builder.defineBoolean("allMods", true,
                "Master switch over every Impact mod's debug lines. When false, no Impact mod writes debug lines.",
                "Warnings and errors are always written.");
        builder.pushSection("logFile", "The rolling file logs/impacts-debug.log, which holds only Impact lines.");
        logFile = new LogFileSettings(builder);
        builder.popSection();
        builder.popSection();
        builder.pushSection("commands", "The /impacts commands.");
        commands = new CommandSettings(builder);
        builder.popSection();
        file = builder.register();
    }

    /** Builds and registers the file. Called once, during Lib's mod construction. */
    public static LibCommonConfig register() {
        registered = new LibCommonConfig(ImpactsConfig.builder(ImpactsLib.MOD_ID, ConfigType.COMMON));
        return registered;
    }

    public static LibCommonConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Impact's Lib config is read before Lib registered it.");
        }
        return registered;
    }

    public static final class LogFileSettings {
        private static final int DEFAULT_SIZE_MEGABYTES = 10;
        private static final int MINIMUM_SIZE_MEGABYTES = 1;
        private static final int MAXIMUM_SIZE_MEGABYTES = 1024;
        private static final int DEFAULT_FILE_COUNT = 3;
        // The current file plus at least one rolled-over file.
        private static final int MINIMUM_FILE_COUNT = 2;
        private static final int MAXIMUM_FILE_COUNT = 20;

        public final ConfigValue<Boolean> isEnabled;
        public final ConfigValue<Integer> sizeMegabytes;
        public final ConfigValue<Integer> fileCount;

        private LogFileSettings(ConfigSpecBuilder builder) {
            isEnabled = builder.defineBoolean("enabled", true,
                    "Also write Impact lines to logs/impacts-debug.log.");
            sizeMegabytes = builder.defineInteger("sizeMb", DEFAULT_SIZE_MEGABYTES,
                    MINIMUM_SIZE_MEGABYTES, MAXIMUM_SIZE_MEGABYTES,
                    "Size in megabytes at which the file rolls over to a new one.");
            fileCount = builder.defineInteger("count", DEFAULT_FILE_COUNT, MINIMUM_FILE_COUNT, MAXIMUM_FILE_COUNT,
                    "How many files to keep, counting the current one.");
        }
    }

    public static final class CommandSettings {
        private static final int DEFAULT_PERMISSION_LEVEL = 2;
        private static final int MINIMUM_PERMISSION_LEVEL = 0;
        private static final int MAXIMUM_PERMISSION_LEVEL = 4;

        public final ConfigValue<Integer> permissionLevel;

        private CommandSettings(ConfigSpecBuilder builder) {
            permissionLevel = builder.defineInteger("permissionLevel", DEFAULT_PERMISSION_LEVEL,
                    MINIMUM_PERMISSION_LEVEL, MAXIMUM_PERMISSION_LEVEL,
                    "Operator level needed to run the /impacts commands.");
        }
    }
}
