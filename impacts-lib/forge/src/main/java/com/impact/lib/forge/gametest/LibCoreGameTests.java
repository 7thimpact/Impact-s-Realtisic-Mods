package com.impact.lib.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ImpactsConfig;
import com.impact.lib.config.LibCommonConfig;
import com.impact.lib.log.DebugSettings;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.log.LogLineFormatter;
import com.impact.lib.log.RepeatedLineFolder;
import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/** Lib milestone 1: mod detection, config file names and defaults, and debug logging. */
@GameTestHolder(ImpactsLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibCoreGameTests {
    private static final String EMPTY_TEMPLATE = "empty";
    private static final String NEVER_INSTALLED_MOD_ID = "impactslib_never_installed";
    private static final String TEST_MOD_ID = "impactslibtest";
    private static final String DEBUG_FILE_PATH = "logs/impacts-debug.log";
    // Defaults written in Lib's spec ("Configuration").
    private static final int SPEC_LOG_FILE_SIZE_MEGABYTES = 10;
    private static final int SPEC_LOG_FILE_COUNT = 3;
    private static final int SPEC_COMMAND_PERMISSION_LEVEL = 2;
    private static final long HALF_FOLD_WINDOW_MILLISECONDS = RepeatedLineFolder.FOLD_WINDOW_MILLISECONDS / 2;
    private static final int HAMMER_USE_TICKS = 40;

    private LibCoreGameTests() {
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void reportsLibAsLoaded(GameTestHelper helper) {
        helper.assertTrue(ImpactsLib.isLoaded(ImpactsLib.MOD_ID), "Impact's Lib should report itself as loaded");
        helper.assertFalse(ImpactsLib.isLoaded(NEVER_INSTALLED_MOD_ID), "A mod that isn't installed should report as not loaded");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void namesConfigFilesInsideLibFolder(GameTestHelper helper) {
        String fileName = ImpactsConfig.fileName("impactsinventory", ConfigType.SERVER);
        helper.assertTrue(fileName.equals("impactslib/impactsinventory-server.toml"), "Unexpected config file name: " + fileName);
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void libConfigUsesSpecDefaults(GameTestHelper helper) {
        LibCommonConfig config = LibCommonConfig.get();
        helper.assertTrue(config.file.isLoaded(), "Lib's common config should be loaded on a running server");
        helper.assertTrue(config.isFamilyDebugOn.defaultValue(), "debug.allMods should default to true");
        helper.assertTrue(config.logFile.isEnabled.defaultValue(), "debug.logFile.enabled should default to true");
        helper.assertTrue(config.logFile.sizeMegabytes.defaultValue() == SPEC_LOG_FILE_SIZE_MEGABYTES, "debug.logFile.sizeMb should default to 10");
        helper.assertTrue(config.logFile.fileCount.defaultValue() == SPEC_LOG_FILE_COUNT, "debug.logFile.count should default to 3");
        helper.assertTrue(config.commands.permissionLevel.defaultValue() == SPEC_COMMAND_PERMISSION_LEVEL, "commands.permissionLevel should default to 2");
        helper.assertTrue(config.debug.isEnabled.defaultValue(), "debug.enabled should default to true");
        helper.assertFalse(config.debug.isVerbose.defaultValue(), "debug.verbose should default to false");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void formatsDebugLines(GameTestHelper helper) {
        String line = LogLineFormatter.format("crafting", "hammer", "finished", "item", "minecraft:wooden_pickaxe", "ticks", HAMMER_USE_TICKS);
        helper.assertTrue(line.equals("[Impact/crafting/hammer] finished item=minecraft:wooden_pickaxe ticks=40"), "Unexpected line: " + line);
        helper.assertTrue(LogLineFormatter.modShortName("impactsinventory").equals("inventory"), "impactsinventory should log as inventory");
        helper.assertTrue(LogLineFormatter.modShortName("impactslib").equals("lib"), "impactslib should log as lib");
        helper.assertTrue(LogLineFormatter.modShortName("minecraft").equals("minecraft"), "Other mod IDs should stay as they are");
        String positionLine = LogLineFormatter.format("inventory", "migration", "chest placed", "chest", new BlockPos(4, 72, -8), "note", "two words");
        helper.assertTrue(positionLine.equals("[Impact/inventory/migration] chest placed chest=4,72,-8 note=\"two words\""), "Unexpected line: " + positionLine);
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void foldsRepeatedLinesWithinOneSecond(GameTestHelper helper) {
        AtomicLong clockMilliseconds = new AtomicLong();
        RepeatedLineFolder folder = new RepeatedLineFolder(clockMilliseconds::get);
        helper.assertTrue(folder.shouldWrite("line", Level.INFO), "The first line should be written");
        clockMilliseconds.set(HALF_FOLD_WINDOW_MILLISECONDS);
        helper.assertFalse(folder.shouldWrite("line", Level.INFO), "A repeat within one second should be folded");
        helper.assertTrue(folder.shouldWrite("other line", Level.INFO), "A different line should be written");
        clockMilliseconds.set(RepeatedLineFolder.FOLD_WINDOW_MILLISECONDS);
        List<RepeatedLineFolder.FoldedLine> summaries = folder.closeExpiredWindows();
        helper.assertTrue(summaries.size() == 1, "Only the repeated line should get a summary, got " + summaries.size());
        helper.assertTrue(summaries.get(0).text().equals("line (x2)"), "Unexpected summary: " + summaries.get(0).text());
        helper.assertTrue(folder.shouldWrite("line", Level.INFO), "The line should be written again once its window closed");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void debugSwitchesControlDebugLines(GameTestHelper helper) {
        DebugSettings debugOn = DebugSettings.fixed(true, false);
        DebugSettings debugOff = DebugSettings.fixed(false, false);
        helper.assertTrue(ImpactsLog.isLineAllowed(true, debugOn, ImpactsLog.STARTUP_CATEGORY), "Debug lines should be written when debug.enabled is true");
        helper.assertFalse(ImpactsLog.isLineAllowed(true, debugOff, ImpactsLog.STARTUP_CATEGORY), "Debug lines should stop when debug.enabled is false");
        helper.assertFalse(ImpactsLog.isLineAllowed(false, debugOn, ImpactsLog.STARTUP_CATEGORY), "Debug lines should stop when debug.allMods is false");
        helper.assertFalse(debugOn.isVerboseEnabled(), "Verbose lines should stay off when debug.verbose is false");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void writesImpactLinesToDebugFile(GameTestHelper helper) {
        ImpactsLog testLog = ImpactsLog.of(TEST_MOD_ID);
        testLog.useDebugSettings(DebugSettings.fixed(true, false));
        String marker = UUID.randomUUID().toString();
        testLog.debug(ImpactsLog.STARTUP_CATEGORY, "gametest marker", "id", marker);
        // The line can only reach the file when debug.allMods and debug.logFile.enabled are on.
        boolean isLineExpected = testLog.isEnabled(ImpactsLog.STARTUP_CATEGORY)
                && LibCommonConfig.get().logFile.isEnabled.get();
        Path debugFile = LoaderPlatform.current().gameDirectory().resolve(DEBUG_FILE_PATH);
        boolean isLineInFile = readOrEmpty(debugFile).contains("[Impact/libtest/startup] gametest marker id=" + marker);
        helper.assertTrue(isLineInFile == isLineExpected,
                "Expected the marker line " + (isLineExpected ? "in " : "not in ") + debugFile);
        helper.succeed();
    }

    private static String readOrEmpty(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException readFailure) {
            return "";
        }
    }
}
