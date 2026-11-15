package com.impact.lib;

import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.SharedConstants;

/**
 * The startup lines every Impact mod writes (each spec, "Debug logging": version, Lib version, which Impact mods and
 * Salty's Realistic Forging were found). Call from the mod's ready listener, after every mod has registered.
 */
public final class StartupSummary {
    /** Salty's Realistic Forging, the family's main companion mod (see impacts-lib/docs/srf-notes.md). */
    public static final String SALTYS_REALISTIC_FORGING_MOD_ID = "realisticforgingores";
    public static final String PLAYER_ANIMATOR_MOD_ID = "playeranimator";
    private static final String ABSENT = "absent";

    private StartupSummary() {
    }

    public static void log(String modId) {
        ImpactsLog log = ImpactsLog.of(modId);
        LoaderPlatform platform = LoaderPlatform.current();
        log.debug(ImpactsLog.STARTUP_CATEGORY, "started",
                "version", versionOf(modId),
                "lib", versionOf(ImpactsLib.MOD_ID),
                "minecraft", SharedConstants.getCurrentVersion().getName(),
                "loader", platform.loaderName(),
                "loaderVersion", platform.loaderVersion());
        log.debug(ImpactsLog.STARTUP_CATEGORY, "mods found",
                "impactMods", ImpactsLib.registeredImpactMods(),
                "srf", versionOf(SALTYS_REALISTIC_FORGING_MOD_ID),
                "playerAnimator", versionOf(PLAYER_ANIMATOR_MOD_ID));
    }

    private static String versionOf(String modId) {
        return LoaderPlatform.current().modVersion(modId).orElse(ABSENT);
    }
}
