package com.impact.realisticvitality.player;

import com.impact.realisticvitality.air.AirTicks;
import com.impact.realisticvitality.temperature.TemperatureTicks;
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
        TemperatureTicks.tick(player);
        AirTicks.tick(player);
        VitalitySync.sendIfChanged(player);
    }
}
