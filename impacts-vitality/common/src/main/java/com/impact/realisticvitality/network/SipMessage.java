package com.impact.realisticvitality.network;

import com.impact.lib.network.MessageCodec;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Client to server: the player sneak-used water with an empty hand while looking at nothing the game counts as a block,
 * so only the client saw the click. The server checks everything again before sipping.
 */
public record SipMessage() {
    public static final MessageCodec<SipMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(SipMessage message, FriendlyByteBuf buffer) {
        }

        @Override
        public SipMessage decode(FriendlyByteBuf buffer) {
            return new SipMessage();
        }
    };
}
