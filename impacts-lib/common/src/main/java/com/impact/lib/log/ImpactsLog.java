package com.impact.lib.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Logging for every Impact mod. Lines read {@code [Impact/<mod>/<category>] <event> key=value ...} and go to
 * {@code logs/latest.log} at INFO level and to {@code logs/impacts-debug.log}. Debug lines follow each mod's
 * {@code debug.*} config and Lib's {@code debug.allMods} master switch; warnings and errors are always written.
 * Identical lines within one second are folded into one (see {@link RepeatedLineFolder}).
 */
public final class ImpactsLog {
    public static final String LOGGER_NAME = "Impact";
    /** Every Impact mod logs its version, the mods it found and its non-default config values here. */
    public static final String STARTUP_CATEGORY = "startup";
    /** Config reloads and rejected config values, for every Impact mod. */
    public static final String CONFIG_CATEGORY = "config";

    private static final Logger LOGGER = LogManager.getLogger(LOGGER_NAME);
    private static final Map<String, ImpactsLog> LOGS_BY_MOD_ID = new ConcurrentHashMap<>();
    private static final RepeatedLineFolder REPEATED_LINE_FOLDER = new RepeatedLineFolder(ImpactsLog::monotonicMilliseconds);
    private static volatile BooleanSupplier isFamilyDebugOn = () -> true;

    private final String modId;
    private final String modShortName;
    private final Set<String> registeredCategories = ConcurrentHashMap.newKeySet();
    private volatile DebugSettings debugSettings = DebugSettings.DEFAULTS;

    private ImpactsLog(String modId) {
        this.modId = modId;
        this.modShortName = LogLineFormatter.modShortName(modId);
    }

    /** The log for one mod, for example {@code ImpactsLog.of("impactscrafting")}. */
    public static ImpactsLog of(String modId) {
        return LOGS_BY_MOD_ID.computeIfAbsent(modId, ImpactsLog::new);
    }

    /** Whether the mod's debug lines in this category are written. Check it before building an expensive value. */
    public static boolean enabled(String modId, String category) {
        return of(modId).isEnabled(category);
    }

    /** Connects Lib's {@code debug.allMods} master switch. */
    public static void useFamilyDebugSwitch(BooleanSupplier isOn) {
        isFamilyDebugOn = isOn;
    }

    /** Writes the summaries of repeated lines whose one-second window has closed. Lib calls this every tick. */
    public static void flushExpiredRepeats() {
        REPEATED_LINE_FOLDER.closeExpiredWindows().forEach(ImpactsLog::writeFoldedLine);
    }

    /** Writes the summaries of every repeated line, for example when the server stops. */
    public static void flushAllRepeats() {
        REPEATED_LINE_FOLDER.closeAllWindows().forEach(ImpactsLog::writeFoldedLine);
    }

    public String modId() {
        return modId;
    }

    /** Registers the mod's category names, so its config and Lib's commands can list them. */
    public ImpactsLog registerCategories(Collection<String> categories) {
        registeredCategories.addAll(categories);
        return this;
    }

    public Set<String> registeredCategories() {
        return Set.copyOf(registeredCategories);
    }

    /** Connects the mod's {@code debug.*} config values. Until then the specs' defaults apply. */
    public void useDebugSettings(DebugSettings settings) {
        debugSettings = settings;
    }

    public boolean isEnabled(String category) {
        return isLineAllowed(isFamilyDebugOn.getAsBoolean(), debugSettings, category);
    }

    /** The rule behind {@link #isEnabled}: the master switch, then the mod's debug.enabled, then its categories. */
    public static boolean isLineAllowed(boolean isFamilyDebugOn, DebugSettings settings, String category) {
        if (!isFamilyDebugOn) {
            return false;
        }
        return settings.isDebugEnabled() && settings.includesCategory(category);
    }

    public boolean isVerbose(String category) {
        return isEnabled(category) && debugSettings.isVerboseEnabled();
    }

    public void debug(String category, String event, Object... keyValues) {
        if (!isEnabled(category)) {
            return;
        }
        write(Level.INFO, LogLineFormatter.format(modShortName, category, event, keyValues));
    }

    /** A detailed line, written only when the mod's {@code debug.verbose} is on. */
    public void verbose(String category, String event, Object... keyValues) {
        if (!isVerbose(category)) {
            return;
        }
        write(Level.INFO, LogLineFormatter.format(modShortName, category, event, keyValues));
    }

    public void warn(String category, String event, Object... keyValues) {
        write(Level.WARN, LogLineFormatter.format(modShortName, category, event, keyValues));
    }

    public void error(String category, String event, Object... keyValues) {
        write(Level.ERROR, LogLineFormatter.format(modShortName, category, event, keyValues));
    }

    private static void write(Level level, String line) {
        flushExpiredRepeats();
        if (!REPEATED_LINE_FOLDER.shouldWrite(line, level)) {
            return;
        }
        LOGGER.log(level, line);
    }

    private static void writeFoldedLine(RepeatedLineFolder.FoldedLine foldedLine) {
        LOGGER.log(foldedLine.level(), foldedLine.text());
    }

    private static long monotonicMilliseconds() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }
}
