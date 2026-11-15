package com.impact.lib.log;

/** Debug settings that never change and cover every category. */
record FixedDebugSettings(boolean isDebugEnabled, boolean isVerboseEnabled) implements DebugSettings {
    @Override
    public boolean includesCategory(String category) {
        return true;
    }
}
