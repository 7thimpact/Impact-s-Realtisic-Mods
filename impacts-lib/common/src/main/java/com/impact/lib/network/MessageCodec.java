package com.impact.lib.network;

import net.minecraft.network.FriendlyByteBuf;

/** Writes a message to a packet buffer and reads it back. */
public interface MessageCodec<M> {
    void encode(M message, FriendlyByteBuf buffer);

    M decode(FriendlyByteBuf buffer);
}
