package com.impact.lib.platform;

import com.impact.lib.hud.StatusRowHeights;

/**
 * What the running mod loader provides to loader-independent client code. Only ever used on the client. Each loader
 * module lists its implementation in {@code META-INF/services/com.impact.lib.platform.ClientPlatform}.
 */
public interface ClientPlatform {
    static ClientPlatform current() {
        return ClientPlatformLookup.LOADED_PLATFORM;
    }

    StatusRowHeights statusRowHeights();
}
