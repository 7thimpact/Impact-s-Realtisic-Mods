package com.impact.realisticinventory.network;

import com.impact.lib.network.MessageCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/** Server to client: what a player wears in the back slot, for drawing it and for that player's own screens. */
public record BackSlotMessage(int wearerEntityId, ItemStack worn) {
    public static final MessageCodec<BackSlotMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(BackSlotMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.wearerEntityId());
            buffer.writeItem(message.worn());
        }

        @Override
        public BackSlotMessage decode(FriendlyByteBuf buffer) {
            return new BackSlotMessage(buffer.readVarInt(), buffer.readItem());
        }
    };
}
