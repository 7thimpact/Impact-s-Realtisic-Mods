package com.impact.realisticvitality.client;

import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.network.NeedSwitchesMessage;
import com.impact.realisticvitality.network.VitalityStateMessage;
import com.impact.realisticvitality.player.ThirstState;

/** The local player's values as the server last sent them, and the server's need switches. Client side only. */
public final class ClientVitality {
    private static final VitalityStateMessage STARTING_STATE = new VitalityStateMessage(ThirstState.FULL, ThirstState.STARTING_HYDRATION, 0.0F, Float.NaN, false);
    private static volatile VitalityStateMessage state = STARTING_STATE;
    private static volatile NeedSwitchesMessage needSwitches;

    private ClientVitality() {
    }

    public static void onState(VitalityStateMessage message) {
        state = message;
    }

    public static void onNeedSwitches(NeedSwitchesMessage message) {
        needSwitches = message;
    }

    public static VitalityStateMessage state() {
        return state;
    }

    /** The server's switch once it has sent one, otherwise this client's own common file. */
    public static boolean isNeedOn(VitalityNeed need) {
        NeedSwitchesMessage switches = needSwitches;
        return switches == null ? VitalityCommonConfig.get().isOn(need) : switches.isOn(need);
    }

    /** Called when the client leaves a world, so the next world starts from its own values. */
    public static void reset() {
        state = STARTING_STATE;
        needSwitches = null;
    }
}
