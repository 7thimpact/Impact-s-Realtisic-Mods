package com.impact.realisticvitality.network;

import com.impact.lib.network.MessageCodec;
import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.network.FriendlyByteBuf;

/** Server to client, whenever one of the player's own values changes: what the HUD shows. */
public record VitalityStateMessage(int thirstLevel, float hydration, float bodyTemperature, float ambientTemperature, boolean isWet,
                                  AirQuality airQuality) {
    public static final MessageCodec<VitalityStateMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(VitalityStateMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.thirstLevel());
            buffer.writeFloat(message.hydration());
            buffer.writeFloat(message.bodyTemperature());
            buffer.writeFloat(message.ambientTemperature());
            buffer.writeBoolean(message.isWet());
            buffer.writeEnum(message.airQuality());
        }

        @Override
        public VitalityStateMessage decode(FriendlyByteBuf buffer) {
            return new VitalityStateMessage(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean(),
                    buffer.readEnum(AirQuality.class));
        }
    };

    public static VitalityStateMessage of(VitalityState state) {
        return new VitalityStateMessage(state.thirst.level(), state.thirst.hydration(), state.temperature.body(), state.temperature.ambient(),
                state.temperature.isWet(), state.air.quality());
    }
}
