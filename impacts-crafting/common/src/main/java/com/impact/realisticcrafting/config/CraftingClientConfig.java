package com.impact.realisticcrafting.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticcrafting.CraftingMod;

/** Crafting's CLIENT file: hints, hammering effects and the progress arrow. */
public final class CraftingClientConfig {
    private static volatile CraftingClientConfig registered;

    public final ConfigValue<Boolean> areNextStepHintsOn;
    public final ConfigValue<Boolean> areHammeringEffectsOn;
    public final ConfigFile file;

    private CraftingClientConfig(ConfigSpecBuilder builder) {
        builder.pushSection("display", "How Crafting shows itself.");
        areNextStepHintsOn = builder.defineBoolean("nextStepHints", true, "Tooltips name each part's next step.");
        areHammeringEffectsOn = builder.defineBoolean("hammeringEffects", true, "Particles and sounds while hammering.");
        builder.popSection();
        file = builder.register();
    }

    public static CraftingClientConfig register() {
        registered = new CraftingClientConfig(ImpactsConfig.builder(CraftingMod.MOD_ID, ConfigType.CLIENT));
        return registered;
    }

    public static CraftingClientConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Crafting's client config is read before it was registered.");
        }
        return registered;
    }
}
