package com.impact.realisticvitality.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.DebugConfigSection;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticvitality.VitalityMod;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Vitality's COMMON file: {@code config/impactslib/impactsvitality-common.toml}. The need switches, the switches for
 * single pieces that have recipes (sipping, linings, each gear item), and debug settings. Recipe conditions read these,
 * and they load before a world's server config. The server sends its switches to clients at login, so the HUD matches.
 */
public final class VitalityCommonConfig {
    private static volatile VitalityCommonConfig registered;

    public final Map<VitalityNeed, ConfigValue<Boolean>> needs;
    public final ConfigValue<Boolean> isSippingOn;
    public final ConfigValue<Boolean> areLiningsOn;
    public final ConfigValue<Boolean> isDryFoodRuleOn;
    public final Map<VitalityGear, ConfigValue<Boolean>> gear;
    public final DebugConfigSection debug;
    public final ConfigFile file;

    private VitalityCommonConfig(ConfigSpecBuilder builder) {
        needs = defineNeeds(builder);
        builder.pushSection("pieces", "Single pieces that can be switched off, along with their recipes (applies on /reload).");
        isSippingOn = builder.defineBoolean("sipping", true, "Sneak-use water with an empty hand to take a sip.");
        areLiningsOn = builder.defineBoolean("linings", true, "Wool, Fur and Leather Linings sewn into armor.");
        isDryFoodRuleOn = builder.defineBoolean("dryFoodRule", true, "Dry foods cost thirst and can't be eaten while Hot.");
        builder.popSection();
        builder.pushSection("gear", "Each piece of gear can be switched off, along with its recipe.");
        gear = defineGear(builder);
        builder.popSection();
        debug = DebugConfigSection.define(builder);
        file = builder.register();
    }

    public static VitalityCommonConfig register() {
        registered = new VitalityCommonConfig(ImpactsConfig.builder(VitalityMod.MOD_ID, ConfigType.COMMON));
        return registered;
    }

    public static VitalityCommonConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Vitality's common config is read before it was registered.");
        }
        return registered;
    }

    public boolean isOn(VitalityNeed need) {
        return needs.get(need).get();
    }

    /** A gear item is on when its own switch and its need's switch both are. */
    public boolean isOn(VitalityGear gearItem) {
        return gear.get(gearItem).get() && isOn(gearItem.need);
    }

    private static Map<VitalityNeed, ConfigValue<Boolean>> defineNeeds(ConfigSpecBuilder builder) {
        Map<VitalityNeed, ConfigValue<Boolean>> switches = new EnumMap<>(VitalityNeed.class);
        for (VitalityNeed need : VitalityNeed.values()) {
            builder.pushSection(need.configKey, "The " + need.configKey + " need.");
            switches.put(need, builder.defineBoolean("enabled", true, "Turn the whole " + need.configKey + " need on or off, with its gear."));
            builder.popSection();
        }
        return Collections.unmodifiableMap(switches);
    }

    private static Map<VitalityGear, ConfigValue<Boolean>> defineGear(ConfigSpecBuilder builder) {
        Map<VitalityGear, ConfigValue<Boolean>> switches = new EnumMap<>(VitalityGear.class);
        for (VitalityGear gearItem : VitalityGear.values()) {
            switches.put(gearItem, builder.defineBoolean(gearItem.configKey, true, "Whether this gear can be made."));
        }
        return Collections.unmodifiableMap(switches);
    }
}
