package com.impact.realisticvitality.player;

import com.impact.realisticvitality.network.VitalityNetwork;
import com.impact.realisticvitality.network.VitalityStateMessage;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.WeakHashMap;

/** Keeps each player's client copy up to date: everything at login, then only when a value the HUD shows has changed. */
public final class VitalitySync {
    /** Server thread only. A respawned player is a new object, so it gets a full send. */
    private static final Map<ServerPlayer, VitalityStateMessage> LAST_SENT_BY_PLAYER = new WeakHashMap<>();

    private VitalitySync() {
    }

    public static void onLogin(ServerPlayer player) {
        VitalityNetwork.sendNeedSwitches(player);
        LAST_SENT_BY_PLAYER.remove(player);
        sendIfChanged(player);
    }

    /** Called at the end of each player tick. */
    public static void sendIfChanged(ServerPlayer player) {
        VitalityStateMessage current = VitalityStateMessage.of(VitalityState.of(player));
        if (current.equals(LAST_SENT_BY_PLAYER.get(player))) {
            return;
        }
        LAST_SENT_BY_PLAYER.put(player, current);
        VitalityNetwork.sendState(player, current);
    }
}
