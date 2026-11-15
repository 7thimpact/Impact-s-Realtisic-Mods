package com.impact.lib.advancement;

import com.impact.lib.ImpactsLib;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.server.level.ServerPlayer;

/** Grants "Why am I here?" when a player joins a world where Impact's Lib is the only Impact mod. */
public final class LoneLibAchievement {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private LoneLibAchievement() {
    }

    /** True when no other Impact mod registered with Lib. Also decides whether the advancement exists. */
    public static boolean isLibAlone() {
        return ImpactsLib.registeredImpactMods().isEmpty();
    }

    public static void grantOnLogin(ServerPlayer player) {
        if (!isLibAlone()) {
            return;
        }
        LibTriggers.JOINED_ALONE.trigger(player);
        LOG.debug(ImpactsLog.STARTUP_CATEGORY, "joined alone", "player", player.getGameProfile().getName());
    }
}
