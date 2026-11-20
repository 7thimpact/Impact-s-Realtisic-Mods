package com.impact.realisticvitality.client;

import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.temperature.BodyState;
import net.minecraft.client.player.LocalPlayer;

/** When the client won't let the player sprint, from the values the server sent: low thirst, or Very Cold and colder. */
public final class ClientSprinting {
    private ClientSprinting() {
    }

    /** Like vanilla's hunger rule: riding or being able to fly lets you sprint anyway. */
    public static boolean isBlocked(LocalPlayer player) {
        if (player.isPassenger() || player.getAbilities().mayfly) {
            return false;
        }
        return isTooThirsty() || isTooCold();
    }

    private static boolean isTooThirsty() {
        return ClientVitality.isNeedOn(VitalityNeed.THIRST)
                && ClientVitality.state().thirstLevel() <= VitalityServerConfig.get().thirst.noSprintAtOrBelow.get();
    }

    private static boolean isTooCold() {
        return ClientVitality.isNeedOn(VitalityNeed.TEMPERATURE) && BodyState.of(ClientVitality.state().bodyTemperature()).isVeryColdOrWorse();
    }
}
