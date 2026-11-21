package com.impact.realisticvitality.network;

import com.impact.lib.network.MessageCodec;
import net.minecraft.network.FriendlyByteBuf;

/** Client to server: the player pressed the Hydration Pack key. The server finds the worn pack and checks its cooldown. */
public record PackSipMessage() {
    public static final MessageCodec<PackSipMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(PackSipMessage message, FriendlyByteBuf buffer) {
        }

        @Override
        public PackSipMessage decode(FriendlyByteBuf buffer) {
            return new PackSipMessage();
        }
    };
}
