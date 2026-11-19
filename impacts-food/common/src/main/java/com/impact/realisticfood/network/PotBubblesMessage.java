package com.impact.realisticfood.network;

import com.impact.lib.network.MessageCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Server to client, about once a second for each boiling pot nearby: where it is and where its water's surface is. */
public record PotBubblesMessage(BlockPos position, Vec3 surfaceCenter) {
    public static final MessageCodec<PotBubblesMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(PotBubblesMessage message, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(message.position());
            buffer.writeDouble(message.surfaceCenter().x);
            buffer.writeDouble(message.surfaceCenter().y);
            buffer.writeDouble(message.surfaceCenter().z);
        }

        @Override
        public PotBubblesMessage decode(FriendlyByteBuf buffer) {
            return new PotBubblesMessage(buffer.readBlockPos(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
    };
}
