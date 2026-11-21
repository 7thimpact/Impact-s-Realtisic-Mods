package com.impact.realisticvitality.client;

import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.network.NeedSwitchesMessage;
import com.impact.realisticvitality.network.VitalityStateMessage;
import com.impact.realisticvitality.player.ThirstState;

/** The local player's values as the server last sent them, and the server's need switches. Client side only. */
public final class ClientVitality {
    private static final VitalityStateMessage STARTING_STATE = new VitalityStateMessage(ThirstState.FULL, ThirstState.STARTING_HYDRATION, 0.0F, Float.NaN, false,
            AirQuality.GOOD);
    private static volatile VitalityStateMessage state = STARTING_STATE;
    private static volatile NeedSwitchesMessage needSwitches;
    /** A trend older than this (no reading changed the body temperature since) counts as steady. */
    private static final long TREND_HOLD_MILLIS = 2500L;
    private static volatile float lastBodyChange;
    private static volatile long lastBodyChangeMillis;

    private ClientVitality() {
    }

    public static void onState(VitalityStateMessage message) {
        float change = message.bodyTemperature() - state.bodyTemperature();
        if (change != 0.0F) {
            lastBodyChange = change;
            lastBodyChangeMillis = System.currentTimeMillis();
        }
        state = message;
    }

    /** How the body temperature moved at the last reading that moved it: above 0 warming, below 0 cooling, 0 when steady. */
    public static float bodyTrend() {
        boolean isRecent = System.currentTimeMillis() - lastBodyChangeMillis <= TREND_HOLD_MILLIS;
        return isRecent ? lastBodyChange : 0.0F;
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
        lastBodyChange = 0.0F;
    }
}
