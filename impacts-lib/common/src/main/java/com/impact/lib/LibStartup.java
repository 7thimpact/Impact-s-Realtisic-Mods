package com.impact.lib;

import com.impact.lib.advancement.LibTriggers;
import com.impact.lib.config.LibCommonConfig;
import com.impact.lib.config.LibServerConfig;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.dump.ImpactsDump;
import com.impact.lib.dump.LibPlayerDumpSection;
import com.impact.lib.log.DebugLogFile;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.SharedConstants;

import java.nio.file.Path;
import java.util.Set;

/** Lib's startup steps that don't depend on the mod loader. The loader module calls them in this order. */
public final class LibStartup {
    private static final String LOGS_FOLDER_NAME = "logs";
    private static final String UNKNOWN_VERSION = "unknown";
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private LibStartup() {
    }

    /** Mod construction: Lib's log categories, its config files, its dump section, and the debug log file. */
    public static void onModConstruction() {
        LOG.registerCategories(LibLogCategories.ALL);
        ImpactsDump.registerPlayerSection(LibPlayerDumpSection.SECTION_NAME, new LibPlayerDumpSection());
        LibCommonConfig commonConfig = LibCommonConfig.register();
        LibServerConfig.register();
        ImpactsLog.useFamilyDebugSwitch(commonConfig.isFamilyDebugOn::get);
        commonConfig.file.onLoad(LibStartup::applyDebugLogFile);
        applyDebugLogFile();
    }

    /**
     * Every mod finished common setup (runs on the main thread): registers Lib's advancement triggers, logs the
     * startup summary and tells listeners which Impact mods are present.
     */
    public static void onModsReady() {
        LibTriggers.registerAll();
        logStartupSummary();
        ImpactsLib.announceModsReady();
    }

    private static void applyDebugLogFile() {
        LibCommonConfig.LogFileSettings logFile = LibCommonConfig.get().logFile;
        Path logsDirectory = LoaderPlatform.current().gameDirectory().resolve(LOGS_FOLDER_NAME);
        DebugLogFile.apply(new DebugLogFile.Settings(logFile.isEnabled.get(), logsDirectory,
                logFile.sizeMegabytes.get(), logFile.fileCount.get()));
    }

    private static void logStartupSummary() {
        LoaderPlatform platform = LoaderPlatform.current();
        LOG.debug(LibLogCategories.STARTUP, "started",
                "version", platform.modVersion(ImpactsLib.MOD_ID).orElse(UNKNOWN_VERSION),
                "minecraft", SharedConstants.getCurrentVersion().getName(),
                "loader", platform.loaderName(),
                "loaderVersion", platform.loaderVersion());
        Set<String> impactMods = ImpactsLib.registeredImpactMods();
        LOG.debug(LibLogCategories.STARTUP, "impact mods found", "count", impactMods.size(), "mods", impactMods);
    }
}
