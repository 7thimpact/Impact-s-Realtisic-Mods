package com.impact.realisticvitality.temperature;

import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.world.entity.player.Player;

/** Everything that insulates a player right now. Being Wet cools (+6); armor, linings and the Straw Hat come with insulation. */
public final class Insulations {
    private static final double WET_COOLING = 6.0;

    private Insulations() {
    }

    public static Insulation of(Player player) {
        Insulation total = Insulation.NONE;
        if (VitalityState.of(player).temperature.isWet()) {
            total = total.plus(new Insulation(0.0, WET_COOLING));
        }
        return total;
    }
}
