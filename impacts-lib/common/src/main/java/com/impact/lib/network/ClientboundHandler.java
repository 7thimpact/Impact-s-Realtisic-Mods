package com.impact.lib.network;

/**
 * Handles a message from the server, on the client's main thread. Keep client-only classes out of the class
 * that registers the handler: call into a separate client class from inside the handler.
 */
@FunctionalInterface
public interface ClientboundHandler<M> {
    void handle(M message);
}
