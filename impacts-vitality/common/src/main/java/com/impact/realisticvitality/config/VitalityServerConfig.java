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
    public final WaterBlockSettings waterBlocks;
    public final FilterSettings filters;
    public final TemperatureSettings temperature;
    public final InsulationSettings insulation;
    public final AirSettings air;
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
        builder.pushSection("waterBlocks", "The Rain Barrel, Water Filter and Copper Still.");
        waterBlocks = new WaterBlockSettings(builder);
        builder.popSection();
        builder.pushSection("filters", "Charcoal Filters, shared by the Water Filter and the Respirator.");
        filters = new FilterSettings(builder);
        builder.popSection();
        builder.pushSection("temperature", "Ambient and body temperature.");
        temperature = new TemperatureSettings(builder);
        builder.popSection();
        builder.pushSection("insulation", "Points of warmth and cooling from armor, linings and clothing. Each point moves an edge of the comfortable range.");
        insulation = new InsulationSettings(builder);
        builder.popSection();
        builder.pushSection("air", "Breathing outside water: thin air up high and deep down, smoke in the Nether and near lava.");
        air = new AirSettings(builder);
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
        public final ConfigValue<Boolean> isRainClean;

        private PuritySettings(ConfigSpecBuilder builder) {
            isRainClean = builder.defineBoolean("rainIsClean", true, "Rain Barrel water counts as purified (otherwise unclean).");
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

    public static final class WaterBlockSettings {
        private static final int MAXIMUM_DRINKS = 1000;

        public final ConfigValue<Integer> bucketDrinks;
        public final ConfigValue<Integer> rainBarrelTicksPerDrink;
        public final ConfigValue<Integer> rainBarrelDrinks;
        public final ConfigValue<Integer> waterFilterTicksPerDrink;
        public final ConfigValue<Integer> waterFilterDrinks;
        public final ConfigValue<Integer> waterFilterWearPerDrink;
        public final ConfigValue<Integer> copperStillTicksPerBucket;
        public final ConfigValue<Integer> copperStillDrinks;

        private WaterBlockSettings(ConfigSpecBuilder builder) {
            bucketDrinks = builder.defineInteger("bucketDrinks", 4, 1, MAXIMUM_DRINKS, "Drinks in a bucket of water.");
            rainBarrelTicksPerDrink = builder.defineInteger("rainBarrelTicksPerDrink", 1200, 1, MAXIMUM_TICKS,
                    "Ticks of rain under open sky for the Rain Barrel to collect 1 drink.");
            rainBarrelDrinks = builder.defineInteger("rainBarrelDrinks", 16, 1, MAXIMUM_DRINKS, "Drinks a Rain Barrel holds.");
            waterFilterTicksPerDrink = builder.defineInteger("waterFilterTicksPerDrink", 600, 1, MAXIMUM_TICKS,
                    "Ticks for the Water Filter to drip 1 drink of purified water.");
            waterFilterDrinks = builder.defineInteger("waterFilterDrinks", 16, 1, MAXIMUM_DRINKS,
                    "Drinks the Water Filter holds on each side (water poured in, purified water dripped out).");
            waterFilterWearPerDrink = builder.defineInteger("waterFilterWearPerDrink", 10, 0, MAXIMUM_TICKS,
                    "Charcoal Filter life each filtered drink uses up (a filter has 600: 10 minutes of smoky air in a Respirator).");
            copperStillTicksPerBucket = builder.defineInteger("copperStillTicksPerBucket", 1200, 1, MAXIMUM_TICKS,
                    "Ticks for a heated Copper Still to purify a bucket of water.");
            copperStillDrinks = builder.defineInteger("copperStillDrinks", 16, 1, MAXIMUM_DRINKS,
                    "Drinks the Copper Still holds on each side (water to distil, purified water).");
        }
    }

    public static final class FilterSettings {
        public final ConfigValue<Integer> charcoalFilterLife;

        private FilterSettings(ConfigSpecBuilder builder) {
            charcoalFilterLife = builder.defineInteger("charcoalFilterLife", 600, 1, MAXIMUM_TICKS,
                    "A Charcoal Filter's life: seconds of smoky air in a Respirator. The Water Filter uses it up too.");
        }
    }

    public static final class TemperatureSettings {
        private static final double MAXIMUM_DEGREES = 1000.0;
        private static final int MAXIMUM_HEIGHT = 2048;

        public final ConfigValue<Double> biomeBase;
        public final ConfigValue<Double> biomeScale;
        public final ConfigValue<List<String>> biomeOverrides;
        public final ConfigValue<List<String>> dimensionOffsets;
        public final ConfigValue<List<String>> dimensionFixed;
        public final ConfigValue<Integer> coolingAboveY;
        public final ConfigValue<Double> blocksPerDegreeAbove;
        public final ConfigValue<Integer> undergroundBelowY;
        public final ConfigValue<Double> undergroundTemperature;
        public final ConfigValue<Integer> undergroundBlendBlocks;
        public final ConfigValue<Integer> deepBelowY;
        public final ConfigValue<Integer> deepestY;
        public final ConfigValue<Double> deepTemperature;
        public final ConfigValue<Double> nightDrop;
        public final ConfigValue<Double> desertNightDrop;
        public final ConfigValue<List<String>> desertBiomes;
        public final ConfigValue<Double> rainDrop;
        public final ConfigValue<Double> snowDrop;
        public final ConfigValue<Double> sunBonus;
        public final ConfigValue<Integer> blockRadius;
        public final ConfigValue<Double> blockHeatCap;
        public final ConfigValue<Double> blockColdCap;
        public final ConfigValue<List<String>> blockTemps;
        public final ConfigValue<Double> unlistedHeatSource;
        public final ConfigValue<Double> unlistedColdSource;
        public final ConfigValue<Double> waterDrop;
        public final ConfigValue<Integer> wetTicks;
        public final ConfigValue<Double> comfortableFrom;
        public final ConfigValue<Double> comfortableTo;
        public final ConfigValue<Double> degreesPerInsulation;
        public final ConfigValue<Double> driftDegreesPerPoint;
        public final ConfigValue<Double> recoveryPerSecond;
        public final ConfigValue<Double> warmAt;
        public final ConfigValue<Double> hotAt;
        public final ConfigValue<Double> veryHotAt;
        public final ConfigValue<Integer> damageIntervalSeconds;
        public final ConfigValue<Double> freezingDamage;
        public final ConfigValue<Double> overheatingDamage;
        public final ConfigValue<Double> coldMiningSpeed;
        public final ConfigValue<List<String>> foodTemperatures;

        private TemperatureSettings(ConfigSpecBuilder builder) {
            biomeBase = builder.defineDouble("biomeBase", 30.0, -MAXIMUM_DEGREES, MAXIMUM_DEGREES, "Ambient °F = biomeBase + biomeScale x the biome's vanilla temperature.");
            biomeScale = builder.defineDouble("biomeScale", 40.0, -MAXIMUM_DEGREES, MAXIMUM_DEGREES, "See biomeBase. Plains (0.8) is 62°F, desert (2.0) 110°F.");
            biomeOverrides = builder.defineStringList("biomeOverrides", List.of(), "Per-biome base °F that replaces the formula, as biome=°F.");
            dimensionOffsets = builder.defineStringList("dimensionOffsets", List.of("minecraft:the_nether=40"),
                    "°F added to a dimension's biomes, as dimension=°F.");
            dimensionFixed = builder.defineStringList("dimensionFixed", List.of("minecraft:the_end=20"),
                    "Dimensions with one flat ambient °F (before nearby blocks and water), as dimension=°F.");
            coolingAboveY = builder.defineInteger("coolingAboveY", 100, -MAXIMUM_HEIGHT, MAXIMUM_HEIGHT, "Above this height it gets colder.");
            blocksPerDegreeAbove = builder.defineDouble("blocksPerDegreeAbove", 4.0, 0.1, MAXIMUM_DEGREES, "Blocks of height per 1°F colder above coolingAboveY.");
            undergroundBelowY = builder.defineInteger("undergroundBelowY", 50, -MAXIMUM_HEIGHT, MAXIMUM_HEIGHT,
                    "Below this, with no sky above, the air drifts toward undergroundTemperature.");
            undergroundTemperature = builder.defineDouble("undergroundTemperature", 55.0, -MAXIMUM_DEGREES, MAXIMUM_DEGREES, "The steady cave °F.");
            undergroundBlendBlocks = builder.defineInteger("undergroundBlendBlocks", 20, 1, MAXIMUM_HEIGHT,
                    "Blocks below undergroundBelowY until the surface weather is gone and only the cave °F is left.");
            deepBelowY = builder.defineInteger("deepBelowY", 0, -MAXIMUM_HEIGHT, MAXIMUM_HEIGHT, "Below this the caves warm up toward deepTemperature.");
            deepestY = builder.defineInteger("deepestY", -64, -MAXIMUM_HEIGHT, MAXIMUM_HEIGHT, "Where the caves reach deepTemperature.");
            deepTemperature = builder.defineDouble("deepTemperature", 75.0, -MAXIMUM_DEGREES, MAXIMUM_DEGREES, "The °F of the deepest caves.");
            nightDrop = builder.defineDouble("nightDrop", 15.0, 0.0, MAXIMUM_DEGREES, "°F colder at midnight.");
            desertNightDrop = builder.defineDouble("desertNightDrop", 35.0, 0.0, MAXIMUM_DEGREES, "°F colder at midnight in deserts and badlands.");
            desertBiomes = builder.defineStringList("desertBiomes", List.of("minecraft:desert", "#minecraft:is_badlands"),
                    "Biomes (or #biome tags) with cold desert nights.");
            rainDrop = builder.defineDouble("rainDrop", 10.0, 0.0, MAXIMUM_DEGREES, "°F colder in rain under open sky.");
            snowDrop = builder.defineDouble("snowDrop", 15.0, 0.0, MAXIMUM_DEGREES, "°F colder in snowfall under open sky.");
            sunBonus = builder.defineDouble("sunBonus", 10.0, 0.0, MAXIMUM_DEGREES, "°F warmer at midday under open, clear sky.");
            blockRadius = builder.defineInteger("blockRadius", 5, 0, 16, "How far nearby hot and cold blocks reach. Closer blocks count more.");
            blockHeatCap = builder.defineDouble("blockHeatCap", 40.0, 0.0, MAXIMUM_DEGREES, "The most °F nearby hot blocks add together.");
            blockColdCap = builder.defineDouble("blockColdCap", 40.0, 0.0, MAXIMUM_DEGREES, "The most °F nearby cold blocks take away together.");
            blockTemps = builder.defineStringList("blockTemps", List.of(
                    "minecraft:lava=25", "minecraft:lava_cauldron=25", "#minecraft:fire=15", "#minecraft:campfires=15",
                    "minecraft:furnace=10", "minecraft:smoker=10", "minecraft:blast_furnace=10", "minecraft:magma_block=8",
                    "minecraft:blue_ice=-20", "minecraft:packed_ice=-15", "minecraft:ice=-10", "minecraft:powder_snow=-10", "minecraft:snow_block=-5"),
                    "°F a hot (#impactslib:heat_sources, when lit) or cold (#impactslib:cold_sources) block gives right next to you, as block=°F or #tag=°F.");
            unlistedHeatSource = builder.defineDouble("unlistedHeatSource", 10.0, 0.0, MAXIMUM_DEGREES, "°F from a heat source not in blockTemps.");
            unlistedColdSource = builder.defineDouble("unlistedColdSource", 5.0, 0.0, MAXIMUM_DEGREES, "°F taken away by a cold source not in blockTemps.");
            waterDrop = builder.defineDouble("waterDrop", 20.0, 0.0, MAXIMUM_DEGREES, "Standing in water counts as this much colder.");
            wetTicks = builder.defineInteger("wetTicks", 600, 0, MAXIMUM_TICKS, "Ticks you stay Wet after water, rain or pouring a drink over yourself.");
            comfortableFrom = builder.defineDouble("comfortableFrom", 50.0, -MAXIMUM_DEGREES, MAXIMUM_DEGREES, "The cold edge of the comfortable ambient range, °F.");
            comfortableTo = builder.defineDouble("comfortableTo", 80.0, -MAXIMUM_DEGREES, MAXIMUM_DEGREES, "The hot edge of the comfortable ambient range, °F.");
            degreesPerInsulation = builder.defineDouble("degreesPerInsulation", 3.0, 0.0, MAXIMUM_DEGREES,
                    "Each point of warmth lowers the cold edge, and each point of cooling raises the hot edge, by this many °F.");
            driftDegreesPerPoint = builder.defineDouble("driftDegreesPerPoint", 10.0, 0.1, MAXIMUM_DEGREES,
                    "Outside the comfortable range, body temperature moves 1 point per second for every this many °F beyond it.");
            recoveryPerSecond = builder.defineDouble("recoveryPerSecond", 2.0, 0.0, 100.0, "Inside the range, body temperature returns to 0 by this much per second.");
            warmAt = builder.defineDouble("warmAt", 25.0, 0.0, 100.0, "Body temperature where Warm (and, below zero, Chilly) starts.");
            hotAt = builder.defineDouble("hotAt", 50.0, 0.0, 100.0, "Body temperature where Hot (and Cold) starts.");
            veryHotAt = builder.defineDouble("veryHotAt", 75.0, 0.0, 100.0, "Body temperature where Very Hot (and Very Cold) starts. Overheating and Freezing are at 100.");
            damageIntervalSeconds = builder.defineInteger("damageIntervalSeconds", 3, 1, 3600, "Seconds between freezing or heat damage.");
            freezingDamage = builder.defineDouble("freezingDamage", 1.0, 0.0, 100.0, "Damage while Freezing (vanilla's freeze damage).");
            overheatingDamage = builder.defineDouble("overheatingDamage", 1.0, 0.0, 100.0, "Damage while Overheating (heatstroke).");
            coldMiningSpeed = builder.defineDouble("coldMiningSpeed", 0.85, 0.0, 1.0, "Mining speed while Cold or colder.");
            foodTemperatures = builder.defineStringList("foodTemperatures", List.of(
                    "impactsvitality:cold_water=-15", "minecraft:melon_slice=-15", "#impactsvitality:soups=15"),
                    "Body temperature a drink or food changes right away, as item=points or #tag=points.");
        }
    }

    public static final class InsulationSettings {
        private static final double MAXIMUM_POINTS = 100.0;
        private static final int MAXIMUM_MATERIALS = 64;

        public final ConfigValue<Double> warmArmorWarmth;
        public final ConfigValue<Double> warmArmorChestWarmth;
        public final ConfigValue<Double> coolingArmorCooling;
        public final ConfigValue<Double> conductiveArmorPenalty;
        public final ConfigValue<Double> woolLiningWarmth;
        public final ConfigValue<Double> furLiningWarmth;
        public final ConfigValue<Double> leatherLiningCooling;
        public final ConfigValue<Integer> woolLiningMaterials;
        public final ConfigValue<Integer> furLiningMaterials;
        public final ConfigValue<Integer> leatherLiningMaterials;
        public final ConfigValue<Integer> anvilLiningLevels;
        public final ConfigValue<Double> strawHatCooling;
        public final ConfigValue<Double> wetCooling;

        private InsulationSettings(ConfigSpecBuilder builder) {
            warmArmorWarmth = builder.defineDouble("warmArmorWarmth", 2.0, -MAXIMUM_POINTS, MAXIMUM_POINTS, "Warmth per piece of #impactsvitality:warm_armor (leather).");
            warmArmorChestWarmth = builder.defineDouble("warmArmorChestWarmth", 3.0, -MAXIMUM_POINTS, MAXIMUM_POINTS, "Warmth of a warm chestplate.");
            coolingArmorCooling = builder.defineDouble("coolingArmorCooling", 1.0, -MAXIMUM_POINTS, MAXIMUM_POINTS,
                    "Cooling per piece of #impactsvitality:cooling_armor (chainmail breathes).");
            conductiveArmorPenalty = builder.defineDouble("conductiveArmorPenalty", 1.0, -MAXIMUM_POINTS, MAXIMUM_POINTS,
                    "Warmth and cooling lost per piece of #impactsvitality:conductive_armor (iron: metal makes heat and cold worse).");
            woolLiningWarmth = builder.defineDouble("woolLiningWarmth", 2.0, -MAXIMUM_POINTS, MAXIMUM_POINTS, "Warmth of a Wool Lining.");
            furLiningWarmth = builder.defineDouble("furLiningWarmth", 3.0, -MAXIMUM_POINTS, MAXIMUM_POINTS, "Warmth of a Fur Lining.");
            leatherLiningCooling = builder.defineDouble("leatherLiningCooling", 2.0, -MAXIMUM_POINTS, MAXIMUM_POINTS, "Cooling of a Leather Lining.");
            woolLiningMaterials = builder.defineInteger("woolLiningMaterials", 3, 1, MAXIMUM_MATERIALS, "Wool a Wool Lining takes.");
            furLiningMaterials = builder.defineInteger("furLiningMaterials", 4, 1, MAXIMUM_MATERIALS, "Rabbit hide a Fur Lining takes.");
            leatherLiningMaterials = builder.defineInteger("leatherLiningMaterials", 3, 1, MAXIMUM_MATERIALS, "Leather a Leather Lining takes.");
            anvilLiningLevels = builder.defineInteger("anvilLiningLevels", 1, 0, 40, "Experience levels to sew a lining in at an anvil (without Crafting).");
            strawHatCooling = builder.defineDouble("strawHatCooling", 4.0, -MAXIMUM_POINTS, MAXIMUM_POINTS, "Cooling of a worn Straw Hat, in the sun only.");
            wetCooling = builder.defineDouble("wetCooling", 6.0, -MAXIMUM_POINTS, MAXIMUM_POINTS, "Cooling while Wet.");
        }
    }

    public static final class AirSettings {
        private static final int MAXIMUM_HEIGHT = 2048;

        public final ConfigValue<Integer> goodFromY;
        public final ConfigValue<Integer> thinAboveY;
        public final ConfigValue<Integer> veryThinAboveY;
        public final ConfigValue<List<String>> heightBandDimensions;
        public final ConfigValue<Integer> thinBubbleTicks;
        public final ConfigValue<Integer> veryThinBubbleTicks;
        public final ConfigValue<Integer> smokyBubbleTicks;
        public final ConfigValue<Integer> verySmokyBubbleTicks;
        public final ConfigValue<Integer> lavaSmokyRadius;
        public final ConfigValue<Integer> lavaVerySmokyRadius;
        public final ConfigValue<Double> damage;
        public final ConfigValue<Integer> damageIntervalTicks;
        public final ConfigValue<Boolean> doesWaterBreathingProtect;
        public final ConfigValue<Double> respirationSlowingPerLevel;

        private AirSettings(ConfigSpecBuilder builder) {
            goodFromY = builder.defineInteger("goodFromY", -40, -MAXIMUM_HEIGHT, MAXIMUM_HEIGHT, "Below this, deep air is stale and Thin.");
            thinAboveY = builder.defineInteger("thinAboveY", 160, -MAXIMUM_HEIGHT, MAXIMUM_HEIGHT, "Above this the air is Thin.");
            veryThinAboveY = builder.defineInteger("veryThinAboveY", 220, -MAXIMUM_HEIGHT, MAXIMUM_HEIGHT, "Above this the air is Very Thin.");
            heightBandDimensions = builder.defineStringList("heightBandDimensions", List.of("minecraft:overworld"),
                    "Dimensions whose air gets thin by height. Other dimensions take their air from data/<namespace>/air_quality files.");
            thinBubbleTicks = builder.defineInteger("thinBubbleTicks", 120, 1, MAXIMUM_TICKS, "Ticks per air bubble lost in Thin air.");
            veryThinBubbleTicks = builder.defineInteger("veryThinBubbleTicks", 60, 1, MAXIMUM_TICKS, "Ticks per air bubble lost in Very Thin air.");
            smokyBubbleTicks = builder.defineInteger("smokyBubbleTicks", 80, 1, MAXIMUM_TICKS, "Ticks per air bubble lost in Smoky air.");
            verySmokyBubbleTicks = builder.defineInteger("verySmokyBubbleTicks", 40, 1, MAXIMUM_TICKS, "Ticks per air bubble lost in Very Smoky air.");
            lavaSmokyRadius = builder.defineInteger("lavaSmokyRadius", 3, 0, 16, "Lava within this many blocks makes the air Smoky.");
            lavaVerySmokyRadius = builder.defineInteger("lavaVerySmokyRadius", 1, 0, 16, "Lava within this many blocks makes the air Very Smoky.");
            damage = builder.defineDouble("damage", 2.0, 0.0, 100.0, "Damage once the bubbles run out, as with drowning.");
            damageIntervalTicks = builder.defineInteger("damageIntervalTicks", 40, 1, MAXIMUM_TICKS, "Ticks between air damage.");
            doesWaterBreathingProtect = builder.defineBoolean("waterBreathingProtects", true, "Water Breathing and Conduit Power protect against bad air.");
            respirationSlowingPerLevel = builder.defineDouble("respirationSlowingPerLevel", 1.0 / 3.0, 0.0, 1.0,
                    "Each level of Respiration slows bubble loss by this share (a third: Respiration III stops it).");
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
