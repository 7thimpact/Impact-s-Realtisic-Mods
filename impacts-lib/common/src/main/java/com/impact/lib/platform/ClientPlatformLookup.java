package com.impact.lib.platform;

import java.util.ServiceLoader;

/** Finds the running loader's {@link ClientPlatform} implementation once, on first use (client side only). */
final class ClientPlatformLookup {
    static final ClientPlatform LOADED_PLATFORM = findClientPlatform();

    private ClientPlatformLookup() {
    }

    private static ClientPlatform findClientPlatform() {
        ClassLoader modClassLoader = ClientPlatform.class.getClassLoader();
        return ServiceLoader.load(ClientPlatform.class, modClassLoader)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No ClientPlatform implementation found in Impact's Lib."));
    }
}
