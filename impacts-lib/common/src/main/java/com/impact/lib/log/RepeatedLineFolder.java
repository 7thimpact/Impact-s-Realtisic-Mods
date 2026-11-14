package com.impact.lib.log;

import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Folds identical log lines written within one second of each other, so busy systems can't flood the log.
 * The first line is written at once. Repeats inside its one-second window are only counted, and when the
 * window closes they are written once as {@code <line> (xN)}, where N counts every occurrence in the window.
 */
public final class RepeatedLineFolder {
    public static final long FOLD_WINDOW_MILLISECONDS = 1000;
    private static final String REPEAT_SUMMARY_FORMAT = "%s (x%d)";

    private final LongSupplier clockMilliseconds;
    private final Map<String, FoldWindow> windowsByLine = new LinkedHashMap<>();

    public RepeatedLineFolder(LongSupplier clockMilliseconds) {
        this.clockMilliseconds = clockMilliseconds;
    }

    /** Whether the line should be written now. A repeat inside an open window returns false and is counted. */
    public synchronized boolean shouldWrite(String line, Level level) {
        long nowMilliseconds = clockMilliseconds.getAsLong();
        FoldWindow window = windowsByLine.get(line);
        boolean isRepeatInsideWindow = window != null && window.isOpenAt(nowMilliseconds);
        if (isRepeatInsideWindow) {
            window.occurrenceCount++;
            return false;
        }
        windowsByLine.put(line, new FoldWindow(level, nowMilliseconds));
        return true;
    }

    /** Closes every window older than one second and returns a summary line for each one that held repeats. */
    public synchronized List<FoldedLine> closeExpiredWindows() {
        long nowMilliseconds = clockMilliseconds.getAsLong();
        List<FoldedLine> summaries = new ArrayList<>();
        Iterator<Map.Entry<String, FoldWindow>> windowEntries = windowsByLine.entrySet().iterator();
        while (windowEntries.hasNext()) {
            Map.Entry<String, FoldWindow> windowEntry = windowEntries.next();
            if (windowEntry.getValue().isOpenAt(nowMilliseconds)) {
                continue;
            }
            windowEntries.remove();
            addSummaryIfRepeated(summaries, windowEntry.getKey(), windowEntry.getValue());
        }
        return summaries;
    }

    /** Closes every window, open or not, for example when the server stops. */
    public synchronized List<FoldedLine> closeAllWindows() {
        List<FoldedLine> summaries = new ArrayList<>();
        windowsByLine.forEach((line, window) -> addSummaryIfRepeated(summaries, line, window));
        windowsByLine.clear();
        return summaries;
    }

    private static void addSummaryIfRepeated(List<FoldedLine> summaries, String line, FoldWindow window) {
        boolean hasRepeats = window.occurrenceCount > 1;
        if (!hasRepeats) {
            return;
        }
        String summary = String.format(REPEAT_SUMMARY_FORMAT, line, window.occurrenceCount);
        summaries.add(new FoldedLine(summary, window.level));
    }

    /** A summary line to write, at the level of the line it folds. */
    public record FoldedLine(String text, Level level) {
    }

    private static final class FoldWindow {
        private final Level level;
        private final long startMilliseconds;
        private int occurrenceCount = 1;

        private FoldWindow(Level level, long startMilliseconds) {
            this.level = level;
            this.startMilliseconds = startMilliseconds;
        }

        private boolean isOpenAt(long nowMilliseconds) {
            return nowMilliseconds - startMilliseconds < FOLD_WINDOW_MILLISECONDS;
        }
    }
}
