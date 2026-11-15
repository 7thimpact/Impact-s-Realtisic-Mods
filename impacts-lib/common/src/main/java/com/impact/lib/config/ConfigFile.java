package com.impact.lib.config;

import java.util.List;

/** A registered Impact config file. */
public interface ConfigFile {
    String modId();

    ConfigType type();

    /** The path inside the config folder, for example {@code impactslib/impactsinventory-server.toml}. */
    String fileName();

    boolean isLoaded();

    /** Every value defined in the file, in definition order. */
    List<ConfigValue<?>> values();

    /** Runs the listener each time the file is loaded or reloaded from disk. */
    void onLoad(Runnable listener);
}
