package com.impact.lib.log;

/** A mod's debug switches: whether it writes debug lines, in which categories, and whether verbose lines too. */
public interface DebugSettings {
    /** Used until a mod's config is loaded. Matches the specs' defaults: every category on, verbose off. */
    DebugSettings DEFAULTS = fixed(true, false);

    /** Settings that never change, covering every category. */
    static DebugSettings fixed(boolean isDebugEnabled, boolean isVerboseEnabled) {
        return new FixedDebugSettings(isDebugEnabled, isVerboseEnabled);
    }

    boolean isDebugEnabled();

    boolean includesCategory(String category);

    boolean isVerboseEnabled();
}
