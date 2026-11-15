package com.impact.lib.platform;

import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.network.NetworkChannel;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Optional;

/**
 * What the running mod loader provides to loader-independent code. Each loader module ships one
 * implementation, listed in {@code META-INF/services/com.impact.lib.platform.LoaderPlatform}.
 */
public interface LoaderPlatform {
    static LoaderPlatform current() {
        return LoaderPlatformLookup.LOADED_PLATFORM;
    }

    /** The loader's name as written in logs, for example {@code forge}. */
    String loaderName();

    String loaderVersion();

    boolean isModLoaded(String modId);

    Optional<String> modVersion(String modId);

    /** The game directory: the folder that holds {@code config/}, {@code logs/} and {@code mods/}. */
    Path gameDirectory();

    /** Starts a config file for a mod. The builder's {@code register()} registers the file with the loader. */
    ConfigSpecBuilder configBuilder(String modId, ConfigType type);

    /** Registers an advancement trigger. Vanilla keeps this private; each loader opens it its own way. */
    void registerCriterionTrigger(CriterionTrigger<?> trigger);

    /** Creates a network channel. Call during mod construction. */
    NetworkChannel createChannel(ResourceLocation channelId, String protocolVersion);

    /** Whether this game instance is a client (single player or connected to a server). */
    boolean isClient();
}
