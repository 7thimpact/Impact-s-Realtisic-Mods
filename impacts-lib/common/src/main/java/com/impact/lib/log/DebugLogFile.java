package com.impact.lib.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.file.Path;

/**
 * The rolling file {@code logs/impacts-debug.log}: the lines Impact mods write to {@code latest.log}, and
 * nothing else. It is attached to the "Impact" logger through Log4j, which Minecraft already ships.
 */
public final class DebugLogFile {
    private static final String APPENDER_NAME_PREFIX = "ImpactsDebugFile-";
    private static final String FILE_NAME = "impacts-debug.log";
    private static final String ROLLED_FILE_NAME_PATTERN = "impacts-debug-%i.log";
    private static final String LINE_LAYOUT = "[%d{ddMMMyyyy HH:mm:ss.SSS}] [%t/%level]: %msg%n";
    private static final String MEGABYTES_UNIT = "MB";
    private static final int FIRST_ROLLED_FILE_INDEX = 1;
    private static final int CURRENT_FILE_COUNT = 1;

    private static Settings appliedSettings;
    private static Appender attachedAppender;
    private static int attachCount;

    private DebugLogFile() {
    }

    /** Where the file lives, when it rolls over, and how many files to keep (counting the current one). */
    public record Settings(boolean isEnabled, Path logsDirectory, int sizeMegabytes, int fileCount) {
    }

    /** Attaches, replaces or removes the file so it matches the settings. Does nothing if they haven't changed. */
    public static synchronized void apply(Settings settings) {
        if (settings.equals(appliedSettings)) {
            return;
        }
        LoggerContext context = impactLoggerContext();
        Configuration configuration = context.getConfiguration();
        LoggerConfig impactLoggerConfig = findOrCreateImpactLoggerConfig(configuration);
        detachCurrentFile(impactLoggerConfig);
        if (settings.isEnabled()) {
            attachFile(configuration, impactLoggerConfig, settings);
        }
        context.updateLoggers();
        appliedSettings = settings;
    }

    private static LoggerContext impactLoggerContext() {
        Logger impactLogger = (Logger) LogManager.getLogger(ImpactsLog.LOGGER_NAME);
        return impactLogger.getContext();
    }

    private static LoggerConfig findOrCreateImpactLoggerConfig(Configuration configuration) {
        LoggerConfig closestConfig = configuration.getLoggerConfig(ImpactsLog.LOGGER_NAME);
        if (closestConfig.getName().equals(ImpactsLog.LOGGER_NAME)) {
            return closestConfig;
        }
        // Additive, so Impact lines still reach latest.log and the console through the root logger.
        LoggerConfig impactLoggerConfig = new LoggerConfig(ImpactsLog.LOGGER_NAME, Level.INFO, true);
        configuration.addLogger(ImpactsLog.LOGGER_NAME, impactLoggerConfig);
        return impactLoggerConfig;
    }

    private static void detachCurrentFile(LoggerConfig impactLoggerConfig) {
        if (attachedAppender == null) {
            return;
        }
        impactLoggerConfig.removeAppender(attachedAppender.getName());
        attachedAppender.stop();
        attachedAppender = null;
    }

    private static void attachFile(Configuration configuration, LoggerConfig impactLoggerConfig, Settings settings) {
        attachCount++;
        int rolledFileCount = settings.fileCount() - CURRENT_FILE_COUNT;
        RollingFileAppender appender = RollingFileAppender.newBuilder()
                .setName(APPENDER_NAME_PREFIX + attachCount)
                .withFileName(settings.logsDirectory().resolve(FILE_NAME).toString())
                .withFilePattern(settings.logsDirectory().resolve(ROLLED_FILE_NAME_PATTERN).toString())
                .setLayout(PatternLayout.newBuilder().withPattern(LINE_LAYOUT).withConfiguration(configuration).build())
                .withPolicy(SizeBasedTriggeringPolicy.createPolicy(settings.sizeMegabytes() + MEGABYTES_UNIT))
                .withStrategy(DefaultRolloverStrategy.newBuilder()
                        .withMin(String.valueOf(FIRST_ROLLED_FILE_INDEX))
                        .withMax(String.valueOf(rolledFileCount))
                        .withConfig(configuration)
                        .build())
                .setConfiguration(configuration)
                .build();
        appender.start();
        configuration.addAppender(appender);
        impactLoggerConfig.addAppender(appender, Level.INFO, null);
        attachedAppender = appender;
    }
}
