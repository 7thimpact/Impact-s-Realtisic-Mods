package com.impact.lib.forge.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigLoadLog;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** An Impact config file backed by a {@link ForgeConfigSpec}. */
final class ForgeConfigFile implements ConfigFile {
    private final String modId;
    private final ConfigType type;
    private final String fileName;
    private final List<ConfigValue<?>> values = new ArrayList<>();
    private final List<Runnable> loadListeners = new CopyOnWriteArrayList<>();
    private final ConfigLoadLog loadLog;
    private volatile ForgeConfigSpec spec;

    ForgeConfigFile(String modId, ConfigType type) {
        this.modId = modId;
        this.type = type;
        this.fileName = ImpactsConfig.fileName(modId, type);
        this.loadLog = new ConfigLoadLog(this);
    }

    void addValue(ConfigValue<?> value) {
        values.add(value);
    }

    void attachSpec(ForgeConfigSpec builtSpec) {
        spec = builtSpec;
    }

    /** Forge loaded or reloaded a config file. Acts only when it is this one. */
    void handleLoad(ModConfig loadedConfig) {
        if (!isThisFile(loadedConfig)) {
            return;
        }
        loadLog.onFileLoaded();
        loadListeners.forEach(Runnable::run);
    }

    /** Forge unloaded a config file, for example a SERVER file when its world closes. Acts only when it is this one. */
    void handleUnload(ModConfig unloadedConfig) {
        if (!isThisFile(unloadedConfig)) {
            return;
        }
        loadLog.onFileUnloaded();
    }

    private boolean isThisFile(ModConfig config) {
        return config.getSpec() == spec;
    }

    @Override
    public String modId() {
        return modId;
    }

    @Override
    public ConfigType type() {
        return type;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public boolean isLoaded() {
        ForgeConfigSpec attachedSpec = spec;
        return attachedSpec != null && attachedSpec.isLoaded();
    }

    @Override
    public List<ConfigValue<?>> values() {
        return List.copyOf(values);
    }

    @Override
    public void onLoad(Runnable listener) {
        loadListeners.add(listener);
    }
}
