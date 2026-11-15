package com.impact.lib.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * A mod's own network channel with a protocol version; a client and server with different versions refuse to
 * connect, with a clear message. Create one with {@code LoaderPlatform.current().createChannel(...)} during mod
 * construction, and register every message on both sides in the same order.
 */
public interface NetworkChannel {
    <M> void registerClientbound(Class<M> messageType, MessageCodec<M> codec, ClientboundHandler<M> handler);

    <M> void registerServerbound(Class<M> messageType, MessageCodec<M> codec, ServerboundHandler<M> handler);

    <M> void sendToPlayer(ServerPlayer player, M message);

    <M> void sendToAllPlayers(M message);

    /** Sends to every player who can see the entity, and to the entity itself when it is a player. */
    <M> void sendToTrackingAndSelf(Entity entity, M message);

    <M> void sendToServer(M message);
}
