package com.impact.realisticfood.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticfood.FoodMod;

/** Food's CLIENT file: boiling particles and sounds. */
public final class FoodClientConfig {
    private static volatile FoodClientConfig registered;

    public final BoilingDisplaySettings boiling;
    public final ConfigFile file;

    private FoodClientConfig(ConfigSpecBuilder builder) {
        builder.pushSection("boiling", "How boiling pots look and sound.");
        boiling = new BoilingDisplaySettings(builder);
        builder.popSection();
        file = builder.register();
    }

    public static FoodClientConfig register() {
        registered = new FoodClientConfig(ImpactsConfig.builder(FoodMod.MOD_ID, ConfigType.CLIENT));
        return registered;
    }

    public static FoodClientConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Food's client config is read before it was registered.");
        }
        return registered;
    }

    public static final class BoilingDisplaySettings {
        public final ConfigValue<Boolean> areParticlesOn;
        public final ConfigValue<Boolean> areSoundsOn;

        private BoilingDisplaySettings(ConfigSpecBuilder builder) {
            areParticlesOn = builder.defineBoolean("particles", true, "Bubble and steam particles over boiling pots.");
            areSoundsOn = builder.defineBoolean("sounds", true, "A simmering sound from boiling pots.");
        }
    }
}
