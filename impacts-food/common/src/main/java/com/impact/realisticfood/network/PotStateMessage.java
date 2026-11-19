package com.impact.realisticfood.network;

import com.impact.lib.network.MessageCodec;
import com.impact.realisticfood.boiling.SoupState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Server to client, about once a second for each busy pot nearby: where its water's surface is, whether it boils, and
 * whether it holds a soup, so the client can draw bubbles and guess the result of a click.
 */
public record PotStateMessage(BlockPos position, Vec3 surfaceCenter, boolean isBoiling, SoupState soupState) {
    public static final MessageCodec<PotStateMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(PotStateMessage message, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(message.position());
            buffer.writeDouble(message.surfaceCenter().x);
            buffer.writeDouble(message.surfaceCenter().y);
            buffer.writeDouble(message.surfaceCenter().z);
            buffer.writeBoolean(message.isBoiling());
            buffer.writeEnum(message.soupState());
        }

        @Override
        public PotStateMessage decode(FriendlyByteBuf buffer) {
            BlockPos position = buffer.readBlockPos();
            Vec3 surfaceCenter = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            return new PotStateMessage(position, surfaceCenter, buffer.readBoolean(), buffer.readEnum(SoupState.class));
        }
    };
}
