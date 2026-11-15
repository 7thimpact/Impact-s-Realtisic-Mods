package com.impact.lib.network;

import net.minecraft.server.level.ServerPlayer;

/** Handles a message from a player, on the server's main thread. */
@FunctionalInterface
public interface ServerboundHandler<M> {
    void handle(M message, ServerPlayer sender);
}
