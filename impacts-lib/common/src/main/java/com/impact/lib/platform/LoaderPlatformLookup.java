package com.impact.lib.platform;

import java.util.ServiceLoader;

/** Finds the running loader's {@link LoaderPlatform} implementation once, on first use. */
final class LoaderPlatformLookup {
    static final LoaderPlatform LOADED_PLATFORM = findLoaderPlatform();

    private LoaderPlatformLookup() {
    }

    private static LoaderPlatform findLoaderPlatform() {
        ClassLoader modClassLoader = LoaderPlatform.class.getClassLoader();
        return ServiceLoader.load(LoaderPlatform.class, modClassLoader)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No LoaderPlatform implementation found. Impact's Lib must be loaded from a loader jar, such as its Forge jar."));
    }
}
