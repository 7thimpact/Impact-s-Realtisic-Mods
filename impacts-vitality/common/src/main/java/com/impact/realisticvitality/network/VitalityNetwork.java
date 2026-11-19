package com.impact.realisticvitality.network;

import com.impact.lib.ResourceIds;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.client.ClientVitality;
import net.minecraft.server.level.ServerPlayer;

/** Vitality's {@code impactsvitality:main} channel. */
public final class VitalityNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static volatile NetworkChannel channel;

    private VitalityNetwork() {
    }

    /** Creates the channel and registers every message. Called once, during mod construction. */
    public static void create() {
        channel = LoaderPlatform.current().createChannel(ResourceIds.of(VitalityMod.MOD_ID, "main"), PROTOCOL_VERSION);
        channel.registerClientbound(VitalityStateMessage.class, VitalityStateMessage.CODEC, message -> ClientVitality.onState(message));
        channel.registerClientbound(NeedSwitchesMessage.class, NeedSwitchesMessage.CODEC, message -> ClientVitality.onNeedSwitches(message));
    }

    public static void sendState(ServerPlayer player, VitalityStateMessage message) {
        channel.sendToPlayer(player, message);
    }

    public static void sendNeedSwitches(ServerPlayer player) {
        channel.sendToPlayer(player, NeedSwitchesMessage.fromConfig());
    }
}
