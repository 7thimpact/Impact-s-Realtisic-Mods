package com.impact.lib.network;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.platform.LoaderPlatform;

/**
 * Lib's {@code impactslib:main} channel, for shared sync (config values, counters). Its protocol version is Lib's
 * major.minor version, so a client and server must run the same Lib minor version to connect.
 */
public final class LibNetwork {
    private static final String VERSION_SEPARATOR = "\\.";
    private static final String UNKNOWN_VERSION = "0.0";
    private static volatile NetworkChannel channel;

    private LibNetwork() {
    }

    /** Creates the channel. Called once, during Lib's mod construction. */
    public static NetworkChannel create() {
        channel = LoaderPlatform.current().createChannel(ResourceIds.of(ImpactsLib.MOD_ID, "main"), protocolVersion());
        return channel;
    }

    public static NetworkChannel channel() {
        if (channel == null) {
            throw new IllegalStateException("Impact's Lib network channel is used before Lib created it.");
        }
        return channel;
    }

    /** Lib's major.minor version, for example {@code 1.0} for 1.0.3. */
    public static String protocolVersion() {
        String fullVersion = LoaderPlatform.current().modVersion(ImpactsLib.MOD_ID).orElse(UNKNOWN_VERSION);
        String[] versionParts = fullVersion.split(VERSION_SEPARATOR);
        boolean hasMinorVersion = versionParts.length >= 2;
        return hasMinorVersion ? versionParts[0] + "." + versionParts[1] : fullVersion;
    }
}
