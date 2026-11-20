package com.impact.realisticvitality.player;

import com.impact.realisticvitality.thirst.ThirstDrain;
import net.minecraft.server.level.ServerPlayer;

/** Everything Vitality does for a player each server tick. Each need adds its step here as it arrives. */
public final class VitalityPlayerTicks {
    private VitalityPlayerTicks() {
    }

    public static void tick(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }
        ThirstDrain.tick(player);
        VitalitySync.sendIfChanged(player);
    }
}
