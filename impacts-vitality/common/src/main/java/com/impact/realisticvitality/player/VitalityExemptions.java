package com.impact.realisticvitality.player;

import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.world.entity.player.Player;

/** Players Vitality leaves alone: spectators always, and creative players while {@code players.exemptCreative} is on. */
public final class VitalityExemptions {
    private VitalityExemptions() {
    }

    public static boolean isExempt(Player player) {
        if (player == null || player.isSpectator()) {
            return true;
        }
        return player.getAbilities().instabuild && VitalityServerConfig.get().isCreativeExempt.get();
    }
}
