package com.impact.lib;

import com.impact.lib.advancement.LibTriggers;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.config.LibClientConfig;
import com.impact.lib.config.LibCommonConfig;
import com.impact.lib.config.LibServerConfig;
import com.impact.lib.counter.ImpactsCounters;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.dump.ImpactsDump;
import com.impact.lib.dump.LibPlayerDumpSection;
import com.impact.lib.log.DebugLogFile;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.network.LibNetwork;
import com.impact.lib.platform.LoaderPlatform;

import java.nio.file.Path;

/** Lib's startup steps that don't depend on the mod loader. The loader module calls them in this order. */
public final class LibStartup {
    private static final String LOGS_FOLDER_NAME = "logs";
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private LibStartup() {
    }

    /** Mod construction: Lib's log categories, config files, network channel, dump section and debug log file. */
    public static void onModConstruction() {
        LOG.registerCategories(LibLogCategories.ALL);
        ImpactsDump.registerPlayerSection(LibPlayerDumpSection.SECTION_NAME, new LibPlayerDumpSection());
        LibCommonConfig commonConfig = LibCommonConfig.register();
        LibServerConfig.register();
        LibClientConfig.register();
        LibNetwork.create();
        PlayerAttachments.register(ImpactsCounters.COUNTERS);
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
        StartupSummary.log(ImpactsLib.MOD_ID);
        ImpactsLib.announceModsReady();
    }

    private static void applyDebugLogFile() {
        LibCommonConfig.LogFileSettings logFile = LibCommonConfig.get().logFile;
        Path logsDirectory = LoaderPlatform.current().gameDirectory().resolve(LOGS_FOLDER_NAME);
        DebugLogFile.apply(new DebugLogFile.Settings(logFile.isEnabled.get(), logsDirectory,
                logFile.sizeMegabytes.get(), logFile.fileCount.get()));
    }

}
