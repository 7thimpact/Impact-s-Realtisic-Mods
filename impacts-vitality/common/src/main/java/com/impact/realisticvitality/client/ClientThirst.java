package com.impact.realisticvitality.client;

import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.client.player.LocalPlayer;

/** Thirst rules the client applies itself, from the values the server sent. */
public final class ClientThirst {
    private ClientThirst() {
    }

    /** Like vanilla's hunger rule: riding or being able to fly lets you sprint anyway. */
    public static boolean isTooThirstyToSprint(LocalPlayer player) {
        if (!ClientVitality.isNeedOn(VitalityNeed.THIRST) || player.isPassenger() || player.getAbilities().mayfly) {
            return false;
        }
        return ClientVitality.state().thirstLevel() <= VitalityServerConfig.get().thirst.noSprintAtOrBelow.get();
    }
}
