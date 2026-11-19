package com.impact.realisticfood.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FixedFood;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Food's SERVER file: {@code <world>/serverconfig/impactslib/impactsfood-server.toml}. Every gameplay number in the
 * spec, with the spec's value as its default. Forge sends this file to clients, so food values match on both sides.
 */
public final class FoodServerConfig {
    private static final int MAXIMUM_TICKS = 72000;
    private static final int MAXIMUM_EAT_TICKS = 200;
    private static final int MAXIMUM_DURABILITY = 10000;
    private static final int MAXIMUM_HUNGER = 20;
    private static final double MAXIMUM_MULTIPLIER = 10.0;
    private static final double MAXIMUM_SATURATION = 2.0;

    private static volatile FoodServerConfig registered;

    public final KnifeSettings knives;
    public final CuttingSettings cutting;
    public final PieceSettings pieces;
    public final BoilingSettings boiling;
    public final SoupSettings soup;
    public final StoneBoilingSettings stoneBoiling;
    public final SpitSettings spit;
    public final RawFoodSettings rawFood;
    public final StyleSettings styles;
    public final Map<FixedFood, FixedFoodSettings> fixedFoods;
    public final BurntFoodSettings burntFood;
    public final MushSettings mush;
    public final CounterSettings counters;
    public final ConfigFile file;

    private FoodServerConfig(ConfigSpecBuilder builder) {
        builder.pushSection("knives", "Flint and Iron Knives.");
        knives = new KnifeSettings(builder);
        builder.popSection();
        builder.pushSection("cutting", "Cutting raw food with a knife.");
        cutting = new CuttingSettings(builder);
        builder.popSection();
        builder.pushSection("pieces", "Cuts, pieces, fillets and slices.");
        pieces = new PieceSettings(builder);
        builder.popSection();
        builder.pushSection("boiling", "Boiling water in cauldrons and campfire buckets.");
        boiling = new BoilingSettings(builder);
        builder.popSection();
        builder.pushSection("soup", "Soup batches.");
        soup = new SoupSettings(builder);
        builder.popSection();
        builder.pushSection("stoneBoiling", "Stone boiling with Hot Pebbles (only with Impact's Realistic Crafting).");
        stoneBoiling = new StoneBoilingSettings(builder);
        builder.popSection();
        builder.pushSection("spit", "Spit roasting on campfires.");
        spit = new SpitSettings(builder);
        builder.popSection();
        builder.pushSection("rawFood", "Eating raw meat or fish.");
        rawFood = new RawFoodSettings(builder);
        builder.popSection();
        builder.pushSection("cooking", "The three cooking styles: food values relative to the base food, and cooking times.");
        styles = new StyleSettings(builder);
        builder.popSection();
        builder.pushSection("foods", "Food values of Food's own foods (pieces and styled foods follow other foods instead).");
        fixedFoods = defineFixedFoods(builder);
        builder.popSection();
        builder.pushSection("burntFood", "Burnt Food as fuel and compost (its food values are under foods.burntFood).");
        burntFood = new BurntFoodSettings(builder);
        builder.popSection();
        builder.pushSection("mush", "Mush as compost (its food values are under foods.mush).");
        mush = new MushSettings(builder);
        builder.popSection();
        builder.pushSection("counters", "Achievement counts.");
        counters = new CounterSettings(builder);
        builder.popSection();
        file = builder.register();
    }

    public static FoodServerConfig register() {
        registered = new FoodServerConfig(ImpactsConfig.builder(FoodMod.MOD_ID, ConfigType.SERVER));
        return registered;
    }

    public static FoodServerConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Food's server config is read before it was registered.");
        }
        return registered;
    }

    private static Map<FixedFood, FixedFoodSettings> defineFixedFoods(ConfigSpecBuilder builder) {
        Map<FixedFood, FixedFoodSettings> settings = new EnumMap<>(FixedFood.class);
        for (FixedFood food : FixedFood.values()) {
            settings.put(food, new FixedFoodSettings(builder, food));
        }
        return Collections.unmodifiableMap(settings);
    }

    public static final class FixedFoodSettings {
        public final ConfigValue<Integer> hunger;
        public final ConfigValue<Double> saturation;
        public final ConfigValue<Integer> eatTicks;
        /** Only for foods with an effect (Burnt Food's Nausea, Cooked Rotten Flesh's Hunger). */
        public final Optional<ConfigValue<Double>> effectChance;
        public final Optional<ConfigValue<Integer>> effectTicks;

        private FixedFoodSettings(ConfigSpecBuilder builder, FixedFood food) {
            builder.pushSection(food.configKey(), food.displayName + ".");
            hunger = builder.defineInteger("hunger", food.defaultHunger, 0, MAXIMUM_HUNGER, "Hunger it restores.");
            saturation = builder.defineDouble("saturation", food.defaultSaturation, 0.0, MAXIMUM_SATURATION, "Its saturation modifier.");
            eatTicks = builder.defineInteger("eatTicks", food.defaultEatTicks, 1, MAXIMUM_EAT_TICKS, "Ticks to eat it (vanilla food takes 32).");
            effectChance = food.effect.map(effect -> builder.defineDouble("effectChance", effect.defaultChance(), 0.0, 1.0,
                    "Chance that eating it gives its effect."));
            effectTicks = food.effect.map(effect -> builder.defineInteger("effectTicks", effect.defaultTicks(), 1, MAXIMUM_TICKS,
                    "Ticks the effect lasts."));
            builder.popSection();
        }
    }

    public static final class KnifeSettings {
        public final ConfigValue<Integer> flintDurability;
        public final ConfigValue<Integer> ironDurability;

        private KnifeSettings(ConfigSpecBuilder builder) {
            flintDurability = builder.defineInteger("flintDurability", 64, 1, MAXIMUM_DURABILITY, "Cuts a Flint Knife makes before it breaks.");
            ironDurability = builder.defineInteger("ironDurability", 250, 1, MAXIMUM_DURABILITY, "Cuts an Iron Knife makes before it breaks.");
        }
    }

    public static final class CuttingSettings {
        public final ConfigValue<Boolean> areByproductsOn;
        public final ConfigValue<List<String>> yieldOverrides;
        public final ConfigValue<List<String>> byproductChanceOverrides;

        private CuttingSettings(ConfigSpecBuilder builder) {
            areByproductsOn = builder.defineBoolean("byproducts", true, "Cutting can give bones, feathers, rabbit hide and bone meal.");
            yieldOverrides = builder.defineStringList("yieldOverrides", List.of(),
                    "Pieces one cut gives, as input=count. Unlisted inputs use the recipe's count (2). Example: [\"minecraft:beef=3\"].");
            byproductChanceOverrides = builder.defineStringList("byproductChanceOverrides", List.of(),
                    "Byproduct chance per input, as input=chance (0 to 1). Unlisted inputs use the recipe's chance. Example: [\"minecraft:beef=0.5\"].");
        }
    }

    public static final class PieceSettings {
        public final ConfigValue<Double> foodShare;
        public final ConfigValue<Integer> furnaceTicks;
        public final ConfigValue<Integer> smokerTicks;
        public final ConfigValue<Integer> campfireTicks;

        private PieceSettings(ConfigSpecBuilder builder) {
            foodShare = builder.defineDouble("foodShare", 0.5, 0.0, 1.0,
                    "A piece restores this share of the whole item's hunger (rounded down, at least 1). Cutting never gains food.");
            furnaceTicks = builder.defineInteger("furnaceTicks", 100, 1, MAXIMUM_TICKS, "Ticks a raw piece takes to cook in a furnace.");
            smokerTicks = builder.defineInteger("smokerTicks", 50, 1, MAXIMUM_TICKS, "Ticks a raw piece takes to cook in a smoker.");
            campfireTicks = builder.defineInteger("campfireTicks", 300, 1, MAXIMUM_TICKS, "Ticks a raw piece takes to cook on a campfire.");
        }
    }

    public static final class BoilingSettings {
        public final ConfigValue<Integer> heatUpTicks;
        public final ConfigValue<Integer> coolDownTicks;
        public final ConfigValue<Integer> evaporationTicks;
        public final ConfigValue<Integer> bucketBoils;
        public final ConfigValue<Integer> bucketHeatUpTicks;
        public final ConfigValue<Integer> bucketItemSlots;
        public final ConfigValue<Integer> cauldronItemSlots;
        public final ConfigValue<List<String>> boilTickOverrides;

        private BoilingSettings(ConfigSpecBuilder builder) {
            heatUpTicks = builder.defineInteger("heatUpTicks", 200, 1, MAXIMUM_TICKS, "Ticks a water cauldron over a heat source takes to boil.");
            coolDownTicks = builder.defineInteger("coolDownTicks", 200, 0, MAXIMUM_TICKS, "Ticks a cauldron keeps boiling after its heat is taken away.");
            evaporationTicks = builder.defineInteger("evaporationTicks", 2400, 1, MAXIMUM_TICKS, "A boiling cauldron loses 1 water level this often.");
            bucketBoils = builder.defineInteger("bucketBoils", 8, 1, 1000, "Boils a water bucket on a campfire lasts.");
            bucketHeatUpTicks = builder.defineInteger("bucketHeatUpTicks", 100, 1, MAXIMUM_TICKS, "Ticks a campfire bucket takes to boil.");
            bucketItemSlots = builder.defineInteger("bucketItemSlots", 2, 1, 16, "Single items a campfire bucket boils at once.");
            cauldronItemSlots = builder.defineInteger("cauldronItemSlots", 8, 1, 64, "Single items a cauldron boils at once.");
            boilTickOverrides = builder.defineStringList("boilTickOverrides", List.of(),
                    "Boil time per input, as input=ticks. Unlisted inputs use their recipe's boil_ticks. Example: [\"minecraft:egg=150\"].");
        }
    }

    public static final class SoupSettings {
        public final ConfigValue<Integer> cookTicks;
        public final ConfigValue<Integer> maxBowls;
        public final ConfigValue<Integer> bucketBowls;
        public final ConfigValue<Integer> servingsPerWaterLevel;
        public final ConfigValue<Integer> waterySoupHunger;

        private SoupSettings(ConfigSpecBuilder builder) {
            cookTicks = builder.defineInteger("cookTicks", 400, 1, MAXIMUM_TICKS, "Ticks a soup batch cooks before it can be served.");
            maxBowls = builder.defineInteger("maxBowls", 6, 1, 64, "Servings one cauldron batch makes at most.");
            bucketBowls = builder.defineInteger("bucketBowls", 2, 1, 64, "Servings one campfire bucket batch makes at most.");
            servingsPerWaterLevel = builder.defineInteger("servingsPerWaterLevel", 2, 1, 64, "Servings that use up 1 water level.");
            waterySoupHunger = builder.defineInteger("waterySoupHunger", 2, 0, MAXIMUM_HUNGER, "Hunger a bowl of Watery Soup restores.");
        }
    }

    public static final class StoneBoilingSettings {
        public final ConfigValue<Integer> boilTicks;
        public final ConfigValue<Integer> pebbleCoolDownTicks;

        private StoneBoilingSettings(ConfigSpecBuilder builder) {
            boilTicks = builder.defineInteger("boilTicks", 600, 1, MAXIMUM_TICKS, "Ticks one Hot Pebble keeps a stone-boiling pot boiling.");
            pebbleCoolDownTicks = builder.defineInteger("pebbleCoolDownTicks", 600, 1, MAXIMUM_TICKS, "Ticks a Hot Pebble takes to cool back into a Stone Pebble.");
        }
    }

    public static final class SpitSettings {
        public final ConfigValue<Integer> slots;
        public final ConfigValue<Double> speed;
        public final ConfigValue<Double> turnSpeed;
        public final ConfigValue<Integer> turnTicks;

        private SpitSettings(ConfigSpecBuilder builder) {
            slots = builder.defineInteger("slots", 6, 1, 16, "Items a spit holds, on top of the campfire's own 4.");
            speed = builder.defineDouble("speed", 1.25, 0.1, MAXIMUM_MULTIPLIER, "Spit items cook this many times faster than on the campfire.");
            turnSpeed = builder.defineDouble("turnSpeed", 1.5, 0.1, MAXIMUM_MULTIPLIER, "Cooking speeds up by this much more for a while after the spit is turned.");
            turnTicks = builder.defineInteger("turnTicks", 100, 1, MAXIMUM_TICKS, "Ticks a turn's speed-up lasts.");
        }
    }

    public static final class RawFoodSettings {
        public final ConfigValue<Double> chance;
        public final ConfigValue<Integer> durationTicks;
        public final ConfigValue<List<String>> overrides;

        private RawFoodSettings(ConfigSpecBuilder builder) {
            chance = builder.defineDouble("chance", 0.3, 0.0, 1.0, "Chance that eating a food in #impactsfood:raw_risky gives Hunger.");
            durationTicks = builder.defineInteger("durationTicks", 300, 1, MAXIMUM_TICKS, "Ticks of Hunger raw food gives.");
            overrides = builder.defineStringList("overrides", List.of(),
                    "Chance per food, as item=chance. Example: [\"minecraft:beef=0.5\"].");
        }
    }

    public static final class StyleSettings {
        public final StyleValues dried;
        public final StyleValues toasted;
        public final StyleValues boiled;
        public final ConfigValue<Double> boiledNegativeEffectChance;
        public final ConfigValue<Integer> dryTicks;
        public final ConfigValue<Integer> smokerDryTicks;
        public final ConfigValue<Integer> toastTicks;
        public final ConfigValue<Integer> rawFurnaceTicks;
        public final ConfigValue<Integer> rawSmokerTicks;
        public final ConfigValue<Integer> rawCampfireTicks;

        private StyleSettings(ConfigSpecBuilder builder) {
            dried = new StyleValues(builder, "dried", "Dried food (furnace, smoker).", 0, 0.75, 16);
            toasted = new StyleValues(builder, "toasted", "Toasted food (campfire, spit).", 1, 1.25, 32);
            boiled = new StyleValues(builder, "boiled", "Boiled food (cauldron, campfire bucket).", 0, 0.9, 24);
            boiledNegativeEffectChance = builder.defineDouble("boiledNegativeEffectChance", 0.5, 0.0, 1.0,
                    "Boiling multiplies the chance of a food's negative effects by this (0.5 halves it).");
            dryTicks = builder.defineInteger("dryTicks", 200, 1, MAXIMUM_TICKS,
                    "Ticks a furnace takes to dry cooked food, or to burn dried and toasted food.");
            smokerDryTicks = builder.defineInteger("smokerDryTicks", 100, 1, MAXIMUM_TICKS, "Ticks a smoker takes for the same.");
            toastTicks = builder.defineInteger("toastTicks", 300, 1, MAXIMUM_TICKS,
                    "Ticks a campfire takes to toast cooked food, or to burn dried and toasted food.");
            rawFurnaceTicks = builder.defineInteger("rawFurnaceTicks", 200, 1, MAXIMUM_TICKS,
                    "Ticks a furnace takes to cook Food's new raw foods (tropical fish, rotten flesh).");
            rawSmokerTicks = builder.defineInteger("rawSmokerTicks", 100, 1, MAXIMUM_TICKS, "Ticks a smoker takes for the same.");
            rawCampfireTicks = builder.defineInteger("rawCampfireTicks", 600, 1, MAXIMUM_TICKS,
                    "Ticks a campfire takes for the same, and to fry an egg.");
        }
    }

    public static final class StyleValues {
        public final ConfigValue<Integer> hungerBonus;
        public final ConfigValue<Double> saturationMultiplier;
        public final ConfigValue<Integer> eatTicks;

        private StyleValues(ConfigSpecBuilder builder, String styleName, String comment, int defaultHungerBonus,
                double defaultSaturationMultiplier, int defaultEatTicks) {
            builder.pushSection(styleName, comment);
            hungerBonus = builder.defineInteger("hungerBonus", defaultHungerBonus, -MAXIMUM_HUNGER, MAXIMUM_HUNGER,
                    "Hunger restored on top of the base food's.");
            saturationMultiplier = builder.defineDouble("saturationMultiplier", defaultSaturationMultiplier, 0.0, MAXIMUM_MULTIPLIER,
                    "Saturation, times the base food's.");
            eatTicks = builder.defineInteger("eatTicks", defaultEatTicks, 1, MAXIMUM_EAT_TICKS, "Ticks to eat it (vanilla food takes 32).");
            builder.popSection();
        }
    }

    public static final class BurntFoodSettings {
        public final ConfigValue<Integer> burnTicks;
        public final ConfigValue<Double> compostChance;

        private BurntFoodSettings(ConfigSpecBuilder builder) {
            burnTicks = builder.defineInteger("burnTicks", 400, 0, MAXIMUM_TICKS, "Ticks Burnt Food burns as furnace fuel.");
            compostChance = builder.defineDouble("compostChance", 0.3, 0.0, 1.0, "Chance Burnt Food raises a composter's level.");
        }
    }

    public static final class MushSettings {
        public final ConfigValue<Double> compostChance;

        private MushSettings(ConfigSpecBuilder builder) {
            compostChance = builder.defineDouble("compostChance", 0.65, 0.0, 1.0, "Chance Mush raises a composter's level.");
        }
    }

    public static final class CounterSettings {
        public final ConfigValue<Integer> dayInAndDayOutEats;
        public final ConfigValue<Integer> tasteTesterFoods;

        private CounterSettings(ConfigSpecBuilder builder) {
            dayInAndDayOutEats = builder.defineInteger("dayInAndDayOutEats", 100, 1, 100000,
                    "Times one boiled or toasted food must be eaten for Day in and Day out.");
            tasteTesterFoods = builder.defineInteger("tasteTesterFoods", 25, 1, 1000, "Different styled foods to eat for Taste Tester.");
        }
    }
}
