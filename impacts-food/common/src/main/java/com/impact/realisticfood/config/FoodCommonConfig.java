package com.impact.realisticfood.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.DebugConfigSection;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticfood.FoodMod;

/**
 * Food's COMMON file: {@code config/impactslib/impactsfood-common.toml}. A switch for each feature (recipe conditions
 * read them, and they load before a world's server config), plus debug settings.
 */
public final class FoodCommonConfig {
    private static volatile FoodCommonConfig registered;

    public final OverhaulSettings overhaul;
    public final RawFoodSwitch rawFood;
    public final CookingSwitches cooking;
    public final DebugConfigSection debug;
    public final ConfigFile file;

    private FoodCommonConfig(ConfigSpecBuilder builder) {
        builder.pushSection("overhaul", "Each feature can be switched off on its own, along with its recipes (applies on /reload).");
        overhaul = new OverhaulSettings(builder);
        builder.popSection();
        builder.pushSection("rawFood", "Eating raw meat or fish can make you sick. Chance and length are in the server file.");
        rawFood = new RawFoodSwitch(builder);
        builder.popSection();
        builder.pushSection("cooking", "Cooking styles and overcooking. Their food values are in the server file.");
        cooking = new CookingSwitches(builder);
        builder.popSection();
        debug = DebugConfigSection.define(builder);
        file = builder.register();
    }

    public static FoodCommonConfig register() {
        registered = new FoodCommonConfig(ImpactsConfig.builder(FoodMod.MOD_ID, ConfigType.COMMON));
        return registered;
    }

    public static FoodCommonConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Food's common config is read before it was registered.");
        }
        return registered;
    }

    public static final class OverhaulSettings {
        public final ConfigValue<Boolean> isCuttingOn;
        public final ConfigValue<Boolean> isBoilingOn;
        public final ConfigValue<Boolean> areSoupsOn;
        public final ConfigValue<Boolean> isEatingFromPotOn;
        public final ConfigValue<Boolean> isCampfireBucketOn;
        public final ConfigValue<Boolean> isStoneBoilingOn;
        public final ConfigValue<Boolean> isSpitOn;
        public final ConfigValue<Boolean> isBakingOn;
        /** overhaul.stews: when on, vanilla's grid stew and soup recipes are off (they're cooked in pots instead). */
        public final ConfigValue<Boolean> isStewOverhaulOn;

        private OverhaulSettings(ConfigSpecBuilder builder) {
            isCuttingOn = builder.defineBoolean("cutting", true, "Knives, and cutting raw food into pieces.");
            isBoilingOn = builder.defineBoolean("boiling", true, "Boiling water in cauldrons, and boiling food in it.");
            areSoupsOn = builder.defineBoolean("soups", true, "Soups and stews cooked in boiling water.");
            isEatingFromPotOn = builder.defineBoolean("eatingFromPot", true, "Eating a finished soup straight from the pot with an empty hand.");
            isCampfireBucketOn = builder.defineBoolean("campfireBucket", true, "A water bucket set on a lit campfire as a small pot.");
            isStoneBoilingOn = builder.defineBoolean("stoneBoiling", true, "Stone boiling with Hot Pebbles (only with Impact's Realistic Crafting).");
            isSpitOn = builder.defineBoolean("spit", true, "Spit roasting on campfires.");
            isBakingOn = builder.defineBoolean("baking", true, "Flour, dough and baking, in place of vanilla's grid bread, cake, cookies and pie.");
            isStewOverhaulOn = builder.defineBoolean("stews", true, "Vanilla's grid stew and soup recipes are off; stews are cooked in pots instead.");
        }
    }

    public static final class RawFoodSwitch {
        public final ConfigValue<Boolean> isOn;

        private RawFoodSwitch(ConfigSpecBuilder builder) {
            isOn = builder.defineBoolean("enabled", true, "Raw meat and fish in #impactsfood:raw_risky can give Hunger.");
        }
    }

    public static final class CookingSwitches {
        public final ConfigValue<Boolean> areStylesOn;
        public final ConfigValue<Boolean> isOvercookingOn;
        public final ConfigValue<Boolean> isDriedOn;
        public final ConfigValue<Boolean> isToastedOn;
        public final ConfigValue<Boolean> isBoiledOn;

        private CookingSwitches(ConfigSpecBuilder builder) {
            areStylesOn = builder.defineBoolean("styles", true, "Dried, toasted and boiled versions of cooked food.");
            isOvercookingOn = builder.defineBoolean("overcooking", true, "Burnt Food and Mush.");
            isDriedOn = builder.defineBoolean("dried", true, "Dried food, from a furnace or smoker.");
            isToastedOn = builder.defineBoolean("toasted", true, "Toasted food, from a campfire or spit.");
            isBoiledOn = builder.defineBoolean("boiled", true, "Boiled food, from a cauldron or campfire bucket.");
        }
    }
}
