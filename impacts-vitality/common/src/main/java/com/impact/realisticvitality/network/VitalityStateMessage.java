package com.impact.realisticvitality.network;

import com.impact.lib.network.MessageCodec;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.network.FriendlyByteBuf;

/** Server to client, whenever one of the player's own values changes: what the HUD shows. */
public record VitalityStateMessage(int thirstLevel, float hydration, float bodyTemperature, boolean isWet) {
    public static final MessageCodec<VitalityStateMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(VitalityStateMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.thirstLevel());
            buffer.writeFloat(message.hydration());
            buffer.writeFloat(message.bodyTemperature());
            buffer.writeBoolean(message.isWet());
        }

        @Override
        public VitalityStateMessage decode(FriendlyByteBuf buffer) {
            return new VitalityStateMessage(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
        }
    };

    public static VitalityStateMessage of(VitalityState state) {
        return new VitalityStateMessage(state.thirst.level(), state.thirst.hydration(), state.temperature.body(), state.temperature.isWet());
    }
}
