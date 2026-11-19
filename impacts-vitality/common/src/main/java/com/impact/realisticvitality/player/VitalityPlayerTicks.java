package com.impact.realisticvitality.player;

import net.minecraft.server.level.ServerPlayer;

/** Everything Vitality does for a player each server tick. Each need adds its step here as it arrives. */
public final class VitalityPlayerTicks {
    private VitalityPlayerTicks() {
    }

    public static void tick(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }
        VitalitySync.sendIfChanged(player);
    }
}
