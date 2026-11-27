package com.impact.realisticinventory.network;

import com.impact.lib.network.MessageCodec;
import com.impact.realisticinventory.pack.WornOn;
import net.minecraft.network.FriendlyByteBuf;

/** Client to server: open a worn pack (B or N, or the buttons by the main hand), the one on the back or on the chest. */
public record OpenPackMessage(WornOn place) {
    private static final WornOn[] PLACES = WornOn.values();

    public static final MessageCodec<OpenPackMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(OpenPackMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.place().ordinal());
        }

        @Override
        public OpenPackMessage decode(FriendlyByteBuf buffer) {
            int placeIndex = buffer.readVarInt();
            boolean isKnownPlace = placeIndex >= 0 && placeIndex < PLACES.length;
            return new OpenPackMessage(isKnownPlace ? PLACES[placeIndex] : WornOn.BACK);
        }
    };
}
