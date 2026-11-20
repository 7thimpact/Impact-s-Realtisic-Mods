package com.impact.realisticvitality.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.thirst.WaterContainer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Vitality's SERVER file: {@code <world>/serverconfig/impactslib/impactsvitality-server.toml}, synced to clients. Every
 * gameplay number in the spec, with the spec's value as its default. Each need's numbers arrive with that need.
 */
public final class VitalityServerConfig {
    private static final int MAXIMUM_TICKS = 72000;
    private static final int THIRST_BAR = 20;
    private static final double MAXIMUM_MULTIPLIER = 10.0;
    private static volatile VitalityServerConfig registered;

    public final ThirstSettings thirst;
    public final DrinkSettings drinks;
    public final PuritySettings purity;
    public final ContainerSettings containers;
    public final TemperatureSettings temperature;
    public final HealthCapSettings healthCap;
    public final ConfigFile file;

    private VitalityServerConfig(ConfigSpecBuilder builder) {
        builder.pushSection("thirst", "The thirst bar: 20 points, with Hydration that drains first, as hunger has saturation.");
        thirst = new ThirstSettings(builder);
        builder.popSection();
        builder.pushSection("drinks", "What each drink and food does to thirst, as item=thirst,hydration (or #tag=thirst,hydration).");
        drinks = new DrinkSettings(builder);
        builder.popSection();
        builder.pushSection("purity", "Where water comes out clean, unclean or salty.");
        purity = new PuritySettings(builder);
        builder.popSection();
        builder.pushSection("containers", "Drink containers: how many drinks each holds and how the Tidebound Canteen refills.");
        containers = new ContainerSettings(builder);
        builder.popSection();
        builder.pushSection("temperature", "Ambient and body temperature.");
        temperature = new TemperatureSettings(builder);
        builder.popSection();
        builder.pushSection("healthCap", "Natural regeneration depends on both hunger and thirst.");
        healthCap = new HealthCapSettings(builder);
        builder.popSection();
        file = builder.register();
    }

    public static VitalityServerConfig register() {
        registered = new VitalityServerConfig(ImpactsConfig.builder(VitalityMod.MOD_ID, ConfigType.SERVER));
        return registered;
    }

    public static VitalityServerConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Vitality's server config is read before it was registered.");
        }
        return registered;
    }

    public static final class ThirstSettings {
        public final ConfigValue<Double> exhaustionPerPoint;
        public final ConfigValue<Double> exhaustionMultiplier;
        public final ConfigValue<Integer> restingDrainTicks;
        public final ConfigValue<Double> hotDrainMultiplier;
        public final ConfigValue<Double> veryHotDrainMultiplier;
        public final ConfigValue<Integer> noSprintAtOrBelow;
        public final ConfigValue<Double> parchedChance;
        public final ConfigValue<Integer> parchedTicks;
        public final ConfigValue<Double> parchedExhaustionPerTick;
        public final ConfigValue<Integer> sipThirst;
        public final ConfigValue<Integer> sipCooldownTicks;
        public final ConfigValue<Double> dehydrationDamage;
        public final ConfigValue<Integer> dehydrationIntervalTicks;

        private ThirstSettings(ConfigSpecBuilder builder) {
            exhaustionPerPoint = builder.defineDouble("exhaustionPerPoint", 4.0, 0.1, 100.0,
                    "Thirst exhaustion that costs 1 point of Hydration, or of thirst once Hydration is empty (vanilla hunger uses 4).");
            exhaustionMultiplier = builder.defineDouble("exhaustionMultiplier", 1.0, 0.0, MAXIMUM_MULTIPLIER,
                    "Thirst exhaustion from actions, times the hunger exhaustion the same action costs.");
            restingDrainTicks = builder.defineInteger("restingDrainTicks", 6000, 1, MAXIMUM_TICKS,
                    "A small constant drain: 1 point of thirst this often, even at rest.");
            hotDrainMultiplier = builder.defineDouble("hotDrainMultiplier", 1.5, 1.0, MAXIMUM_MULTIPLIER, "Thirst drain while Hot.");
            veryHotDrainMultiplier = builder.defineDouble("veryHotDrainMultiplier", 2.0, 1.0, MAXIMUM_MULTIPLIER, "Thirst drain while Very Hot or worse.");
            noSprintAtOrBelow = builder.defineInteger("noSprintAtOrBelow", 6, 0, THIRST_BAR, "At this much thirst or less you can't sprint.");
            parchedChance = builder.defineDouble("parchedChance", 0.3, 0.0, 1.0, "Chance that Unclean Water (or a sip from a source) makes you Parched.");
            parchedTicks = builder.defineInteger("parchedTicks", 600, 1, MAXIMUM_TICKS, "Ticks Parched lasts.");
            parchedExhaustionPerTick = builder.defineDouble("parchedExhaustionPerTick", 0.005, 0.0, 1.0,
                    "Thirst exhaustion Parched adds each tick, per level (as Hunger does for food).");
            sipThirst = builder.defineInteger("sipThirst", 1, 0, THIRST_BAR, "Thirst a sip from a water source restores.");
            sipCooldownTicks = builder.defineInteger("sipCooldownTicks", 20, 0, MAXIMUM_TICKS,
                    "Ticks between sips from a water source, so holding the button doesn't drink a lake in a second.");
            dehydrationDamage = builder.defineDouble("dehydrationDamage", 1.0, 0.0, 100.0, "Damage at 0 thirst, on vanilla's starvation rules.");
            dehydrationIntervalTicks = builder.defineInteger("dehydrationIntervalTicks", 80, 1, MAXIMUM_TICKS, "Ticks between dehydration damage.");
        }
    }

    public static final class DrinkSettings {
        /** The spec's drink table. Items and tags are checked in this order; the first match wins. */
        public static final List<String> SPEC_VALUES = List.of(
                "impactsvitality:unclean_water=4,1",
                "impactsvitality:salt_water=-2,0",
                "impactsvitality:purified_water=6,4",
                "impactsvitality:cold_water=6,4",
                "minecraft:milk_bucket=6,3",
                "minecraft:potion=3,1",
                "minecraft:honey_bottle=2,0",
                "#impactsvitality:soups=4,2",
                "minecraft:melon_slice=2,1",
                "#impactsvitality:juicy_foods=1,0",
                "#impactsvitality:dry_foods=-1,0");

        public final ConfigValue<List<String>> values;
        public final ConfigValue<List<String>> overrides;

        private DrinkSettings(ConfigSpecBuilder builder) {
            values = builder.defineStringList("values", SPEC_VALUES,
                    "Thirst and Hydration per drink or bite. A vanilla water bottle drinks as Unclean Water.");
            overrides = builder.defineStringList("overrides", List.of(), "Extra item=thirst,hydration entries that win over the values above.");
        }
    }

    public static final class PuritySettings {
        public final ConfigValue<List<String>> saltWaterBiomes;

        private PuritySettings(ConfigSpecBuilder builder) {
            saltWaterBiomes = builder.defineStringList("saltWaterBiomes", List.of("#minecraft:is_ocean", "#minecraft:is_beach"),
                    "Biomes (or #biome tags) where water is salty. Water anywhere else is unclean until boiled.");
        }
    }

    public static final class ContainerSettings {
        private static final int MAXIMUM_DRINKS = 1000;

        public final Map<WaterContainer, ConfigValue<Integer>> capacities;
        public final ConfigValue<Integer> cupPurifyingTicks;
        public final ConfigValue<Integer> tideboundRefillTicks;
        public final ConfigValue<Integer> tideboundWetRefillTicks;

        private ContainerSettings(ConfigSpecBuilder builder) {
            Map<WaterContainer, ConfigValue<Integer>> containerCapacities = new EnumMap<>(WaterContainer.class);
            for (WaterContainer container : WaterContainer.values()) {
                containerCapacities.put(container, builder.defineInteger(container.gear.configKey + "Drinks", container.defaultCapacity, 1, MAXIMUM_DRINKS,
                        "Drinks the " + container.itemPath + " holds."));
            }
            capacities = Collections.unmodifiableMap(containerCapacities);
            cupPurifyingTicks = builder.defineInteger("cupPurifyingTicks", 300, 1, MAXIMUM_TICKS, "Ticks a filled Wooden Cup takes to purify on a campfire.");
            tideboundRefillTicks = builder.defineInteger("tideboundRefillTicks", 400, 1, MAXIMUM_TICKS,
                    "Ticks between the Tidebound Canteen's refills (1 drink of purified water).");
            tideboundWetRefillTicks = builder.defineInteger("tideboundWetRefillTicks", 100, 1, MAXIMUM_TICKS,
                    "Ticks between its refills while it's in rain or water.");
        }
    }

    public static final class TemperatureSettings {
        public final ConfigValue<Integer> wetTicks;

        private TemperatureSettings(ConfigSpecBuilder builder) {
            wetTicks = builder.defineInteger("wetTicks", 600, 0, MAXIMUM_TICKS, "Ticks you stay Wet after water, rain or pouring a drink over yourself.");
        }
    }

    public static final class HealthCapSettings {
        public final ConfigValue<Boolean> isOn;
        public final ConfigValue<Boolean> capsAllHealing;
        public final ConfigValue<Integer> fullBarAtOrAbove;
        public final ConfigValue<Integer> lowBarAtOrBelow;
        public final ConfigValue<Double> middleShare;
        public final ConfigValue<Double> lowShare;

        private HealthCapSettings(ConfigSpecBuilder builder) {
            isOn = builder.defineBoolean("enabled", true, "Hunger and thirst share healing: the emptier bar caps natural regeneration.");
            capsAllHealing = builder.defineBoolean("capAllHealing", false, "Also cap potions, golden apples and other healing (normally only natural regeneration).");
            fullBarAtOrAbove = builder.defineInteger("fullBarAtOrAbove", 18, 0, THIRST_BAR,
                    "The fuller bar must reach this for any natural regeneration, and both must for full health.");
            lowBarAtOrBelow = builder.defineInteger("lowBarAtOrBelow", 6, 0, THIRST_BAR, "An emptier bar at this or less caps health at the low share.");
            middleShare = builder.defineDouble("middleShare", 0.75, 0.0, 1.0, "The share of max health regeneration fills while the emptier bar is between the two.");
            lowShare = builder.defineDouble("lowShare", 0.5, 0.0, 1.0, "The share of max health regeneration fills while the emptier bar is low.");
        }
    }
}
