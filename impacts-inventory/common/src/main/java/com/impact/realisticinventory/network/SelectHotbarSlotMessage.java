package com.impact.realisticinventory.network;

import com.impact.lib.network.MessageCodec;
import com.impact.realisticinventory.hotbar.SwitchSource;
import net.minecraft.network.FriendlyByteBuf;

/** Client to server: switch to slot 1, slot 2, or {@link #OTHER_SLOT}. */
public record SelectHotbarSlotMessage(int requestedSlot, SwitchSource source) {
    /** Switch to whichever slot isn't active. */
    public static final int OTHER_SLOT = 0;

    public static final MessageCodec<SelectHotbarSlotMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(SelectHotbarSlotMessage message, FriendlyByteBuf buffer) {
            buffer.writeByte(message.requestedSlot());
            buffer.writeEnum(message.source());
        }

        @Override
        public SelectHotbarSlotMessage decode(FriendlyByteBuf buffer) {
            return new SelectHotbarSlotMessage(buffer.readByte(), buffer.readEnum(SwitchSource.class));
        }
    };
}
