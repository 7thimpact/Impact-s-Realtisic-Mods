package com.impact.realisticinventory.network;

import com.impact.lib.network.MessageCodec;
import net.minecraft.network.FriendlyByteBuf;

/** Server to client: which hotbar slot is active. */
public record HotbarStateMessage(int activeSlot) {
    public static final MessageCodec<HotbarStateMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(HotbarStateMessage message, FriendlyByteBuf buffer) {
            buffer.writeByte(message.activeSlot());
        }

        @Override
        public HotbarStateMessage decode(FriendlyByteBuf buffer) {
            return new HotbarStateMessage(buffer.readByte());
        }
    };
}
