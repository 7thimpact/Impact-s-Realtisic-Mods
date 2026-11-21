package com.impact.realisticvitality.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticvitality.VitalityMod;

/** Vitality's CLIENT file: units and the HUD. */
public final class VitalityClientConfig {
    private static volatile VitalityClientConfig registered;

    public final DisplaySettings display;
    public final ConfigFile file;

    private VitalityClientConfig(ConfigSpecBuilder builder) {
        builder.pushSection("display", "How Vitality's HUD looks.");
        display = new DisplaySettings(builder);
        builder.popSection();
        file = builder.register();
    }

    public static VitalityClientConfig register() {
        registered = new VitalityClientConfig(ImpactsConfig.builder(VitalityMod.MOD_ID, ConfigType.CLIENT));
        return registered;
    }

    public static VitalityClientConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Vitality's client config is read before it was registered.");
        }
        return registered;
    }

    public static final class DisplaySettings {
        public final ConfigValue<TemperatureUnit> units;
        public final ConfigValue<Boolean> isDropletShakeOn;
        public final ConfigValue<Double> readoutScale;
        public final ConfigValue<Boolean> areNeedSoundsOn;

        private DisplaySettings(ConfigSpecBuilder builder) {
            units = builder.defineEnum("units", TemperatureUnit.FAHRENHEIT, "Units of the ambient temperature readout.");
            isDropletShakeOn = builder.defineBoolean("dropletShake", true, "Thirst droplets shake when thirst is low.");
            readoutScale = builder.defineDouble("readoutScale", 0.5, 0.25, 2.0,
                    "Text size of the Thermometer's readout under the temperature gauge (0.5 fits between the health and hunger rows).");
            areNeedSoundsOn = builder.defineBoolean("needSounds", true, "Hear yourself pant when thirsty, shiver when cold and cough in smoke.");
        }
    }
}
