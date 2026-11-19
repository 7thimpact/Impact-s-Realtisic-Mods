package com.impact.realisticinventory.network;

import com.impact.lib.network.MessageCodec;
import net.minecraft.network.FriendlyByteBuf;

/** Client to server: open the worn pack (the B key, or the button beside the back slot). */
public record OpenPackMessage() {
    public static final MessageCodec<OpenPackMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(OpenPackMessage message, FriendlyByteBuf buffer) {
        }

        @Override
        public OpenPackMessage decode(FriendlyByteBuf buffer) {
            return new OpenPackMessage();
        }
    };
}
