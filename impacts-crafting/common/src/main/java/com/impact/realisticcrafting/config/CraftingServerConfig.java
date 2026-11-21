package com.impact.realisticcrafting.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticcrafting.CraftingMod;

import java.util.List;

/**
 * Crafting's SERVER file: {@code <world>/serverconfig/impactslib/impactscrafting-server.toml}, synced to clients. Every
 * gameplay number in the spec, with the spec's value as its default. Each system's numbers arrive with that system.
 */
public final class CraftingServerConfig {
    private static final int MAXIMUM_USES = 100_000;
    private static volatile CraftingServerConfig registered;

    public final PebbleSettings pebbles;
    public final DurabilitySettings durability;
    public final TableSettings tables;
    public final TableTierSettings tableTiers;
    public final TierSettings tiers;
    public final WoodScrapSettings woodScraps;
    public final HammeringSettings hammering;
    public final TimedCraftingSettings timedCrafting;
    public final DyeVatSettings dyeVat;
    public final AchievementSettings achievements;
    public final ConfigValue<Boolean> isFuelOn;
    public final ConfigValue<Integer> woodenBucketUses;
    public final ConfigValue<Boolean> bedsDropParts;
    public final ConfigFile file;

    private CraftingServerConfig(ConfigSpecBuilder builder) {
        builder.pushSection("pebbles", "Stone Pebbles: the first tool. Loose pebbles, gravel and stone are the only sources.");
        pebbles = new PebbleSettings(builder);
        builder.popSection();
        builder.pushSection("durability", "Uses of Crafting's tools.");
        durability = new DurabilitySettings(builder);
        builder.popSection();
        builder.pushSection("tables", "The makeshift tables, which wear out like an anvil.");
        tables = new TableSettings(builder);
        builder.popSection();
        builder.pushSection("tableTiers", "Which crafting grids refuse recipes above their tier.");
        tableTiers = new TableTierSettings(builder);
        builder.popSection();
        builder.pushSection("tiers", "Items whose tier the tier tags don't set. IDs or #tags.");
        tiers = new TierSettings(builder);
        builder.popSection();
        builder.pushSection("woodScraps", "Wood Scraps: the byproduct of every wood trimming or cutting recipe.");
        woodScraps = new WoodScrapSettings(builder);
        builder.popSection();
        builder.pushSection("hammering", "Finishing loose items with a hammer in the off hand.");
        hammering = new HammeringSettings(builder);
        builder.popSection();
        builder.pushSection("timedCrafting", "Every crafting grid takes time, by how deep the recipe sits in the crafting tree.");
        timedCrafting = new TimedCraftingSettings(builder);
        builder.popSection();
        woodenBucketUses = builder.defineInteger("woodenBucketUses", 32, 1, MAXIMUM_USES,
                "Uses of a Wooden Bucket. Each scoop and each pour costs 1.");
        builder.pushSection("dyeVat", "The Dye Vat, where all dyeing happens.");
        dyeVat = new DyeVatSettings(builder);
        builder.popSection();
        bedsDropParts = builder.defineBoolean("bedsDropParts", true, "A broken bed drops its frame, comforter and sheet instead of itself.");
        builder.pushSection("achievements", "The numbers behind the Realistic Crafting achievements.");
        achievements = new AchievementSettings(builder);
        builder.popSection();
        builder.pushSection("fuel", "Crafting's wooden parts as furnace fuel. Per-item numbers go in Lib's fuel.overrides.");
        isFuelOn = builder.defineBoolean("enabled", true, "Crafting's wooden parts burn in furnaces.");
        builder.popSection();
        file = builder.register();
    }

    public static CraftingServerConfig register() {
        registered = new CraftingServerConfig(ImpactsConfig.builder(CraftingMod.MOD_ID, ConfigType.SERVER));
        return registered;
    }

    public static CraftingServerConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Crafting's server config is read before it was registered.");
        }
        return registered;
    }

    public static final class PebbleSettings {
        public final ConfigValue<Double> gravelChance;
        public final ConfigValue<Double> stonePickaxeChance;
        public final ConfigValue<Boolean> doesHandBreakingDrop;
        public final ConfigValue<Boolean> isWorldgenOn;
        public final ConfigValue<Integer> surfaceTriesPerChunk;
        public final ConfigValue<Integer> caveTriesPerChunk;
        public final ConfigValue<List<String>> worldgenDimensions;
        public final ConfigValue<List<String>> worldgenBiomeBlacklist;

        private PebbleSettings(ConfigSpecBuilder builder) {
            gravelChance = builder.defineDouble("gravelChance", 0.25, 0.0, 1.0, "Chance that breaking gravel drops a pebble.");
            stonePickaxeChance = builder.defineDouble("stonePickaxeChance", 0.15, 0.0, 1.0,
                    "Chance that breaking stone with a pickaxe drops a pebble too.");
            doesHandBreakingDrop = builder.defineBoolean("handBreakDrops", true, "Breaking stone by hand always drops a pebble.");
            isWorldgenOn = builder.defineBoolean("worldgen", true, "Loose pebbles generate in new chunks.");
            surfaceTriesPerChunk = builder.defineInteger("surfaceTriesPerChunk", 6, 0, 256,
                    "Tries per chunk to place a loose pebble on the surface (about as many as a desert's dead bushes).");
            caveTriesPerChunk = builder.defineInteger("caveTriesPerChunk", 6, 0, 256, "Tries per chunk to place a loose pebble on a cave floor.");
            worldgenDimensions = builder.defineStringList("worldgenDimensions", List.of("minecraft:overworld"),
                    "Dimensions loose pebbles generate in.");
            worldgenBiomeBlacklist = builder.defineStringList("worldgenBiomeBlacklist", List.of(),
                    "Biomes (IDs or #tags) where loose pebbles never generate.");
        }
    }

    public static final class DurabilitySettings {
        public final ConfigValue<Integer> pebbleUses;
        public final ConfigValue<Integer> needleUses;
        public final ConfigValue<Integer> flintChiselUses;
        public final ConfigValue<Integer> ironChiselUses;
        public final ConfigValue<Integer> ironToolUses;

        private DurabilitySettings(ConfigSpecBuilder builder) {
            pebbleUses = builder.defineInteger("pebble", 32, 1, MAXIMUM_USES, "Uses of a Stone Pebble.");
            needleUses = builder.defineInteger("needle", 64, 1, MAXIMUM_USES, "Uses of a Bone Needle.");
            flintChiselUses = builder.defineInteger("flintChisel", 48, 1, MAXIMUM_USES, "Uses of a Flint Chisel.");
            ironChiselUses = builder.defineInteger("ironChisel", 192, 1, MAXIMUM_USES, "Uses of an Iron Chisel.");
            ironToolUses = builder.defineInteger("ironTools", 128, 1, MAXIMUM_USES, "Uses of the Iron Saw, Iron Hammer and Iron Tweezers.");
        }
    }

    public static final class TableSettings {
        public final ConfigValue<Integer> makeshiftTableUses;
        public final ConfigValue<Integer> makeshiftStoneTableUses;
        public final ConfigValue<Double> wornBelow;
        public final ConfigValue<Double> fallingApartBelow;

        private TableSettings(ConfigSpecBuilder builder) {
            makeshiftTableUses = builder.defineInteger("makeshiftTableUses", 128, 1, MAXIMUM_USES, "Crafts a Makeshift Table lasts.");
            makeshiftStoneTableUses = builder.defineInteger("makeshiftStoneTableUses", 256, 1, MAXIMUM_USES, "Crafts a Makeshift Stone Table lasts.");
            wornBelow = builder.defineDouble("wornBelow", 2.0 / 3.0, 0.0, 1.0, "Share of uses left below which a table looks worn.");
            fallingApartBelow = builder.defineDouble("fallingApartBelow", 1.0 / 3.0, 0.0, 1.0,
                    "Share of uses left below which a table looks like it's falling apart.");
        }
    }

    public static final class TableTierSettings {
        public final ConfigValue<Boolean> isOn;
        public final ConfigValue<Boolean> checksInventoryGrid;
        public final ConfigValue<Boolean> checksMakeshiftTable;
        public final ConfigValue<Boolean> checksMakeshiftStoneTable;

        private TableTierSettings(ConfigSpecBuilder builder) {
            isOn = builder.defineBoolean("enabled", true, "Crafting grids refuse recipes above their tier.");
            checksInventoryGrid = builder.defineBoolean("inventoryGrid", true, "The 2x2 inventory grid takes non-metal recipes only.");
            checksMakeshiftTable = builder.defineBoolean("makeshiftTable", true, "The Makeshift Table takes non-metal recipes only.");
            checksMakeshiftStoneTable = builder.defineBoolean("makeshiftStoneTable", true,
                    "The Makeshift Stone Table takes non-metal recipes plus iron, copper and redstone.");
        }
    }

    public static final class TierSettings {
        public final ConfigValue<List<String>> extraStone;
        public final ConfigValue<List<String>> extraAdvanced;

        private TierSettings(ConfigSpecBuilder builder) {
            extraStone = builder.defineStringList("extraStone", List.of(), "Items that count as stone tier (the Makeshift Stone Table).");
            extraAdvanced = builder.defineStringList("extraAdvanced", List.of(), "Items that count as advanced tier (the Crafting Table).");
        }
    }

    public static final class WoodScrapSettings {
        public final ConfigValue<Integer> perCraft;
        public final ConfigValue<Integer> burnTicks;
        public final ConfigValue<Double> compostChance;

        private WoodScrapSettings(ConfigSpecBuilder builder) {
            perCraft = builder.defineInteger("perCraft", 1, 0, 64, "Wood Scraps each wood trimming or cutting craft gives (0 for none).");
            burnTicks = builder.defineInteger("burnTicks", 100, 0, 32000, "How long Wood Scraps burn, in ticks.");
            compostChance = builder.defineDouble("compostChance", 0.3, 0.0, 1.0, "Chance that Wood Scraps raise a composter's level.");
        }
    }

    public static final class HammeringSettings {
        public final ConfigValue<Integer> hammerUseTicks;
        public final ConfigValue<Integer> ironHammerUseTicks;

        private HammeringSettings(ConfigSpecBuilder builder) {
            hammerUseTicks = builder.defineInteger("hammerUseTicks", 40, 1, 1200, "Ticks of holding right-click to hammer a loose item.");
            ironHammerUseTicks = builder.defineInteger("ironHammerUseTicks", 20, 1, 1200,
                    "The same with the Iron Hammer (recipes with their own time are scaled alike).");
        }
    }

    public static final class TimedCraftingSettings {
        public final ConfigValue<Boolean> isOn;
        public final ConfigValue<Integer> ticksPerDepth;
        public final ConfigValue<Integer> maxTicks;
        public final ConfigValue<Integer> fallbackTicks;
        public final ConfigValue<Boolean> timesInventoryGrid;
        public final ConfigValue<Boolean> timesMakeshiftTables;
        public final ConfigValue<Boolean> timesCraftingTable;
        public final ConfigValue<List<String>> recipeOverrides;
        public final ConfigValue<List<String>> modMultipliers;

        private TimedCraftingSettings(ConfigSpecBuilder builder) {
            isOn = builder.defineBoolean("enabled", true, "Crafting grids take time.");
            ticksPerDepth = builder.defineInteger("ticksPerDepth", 10, 0, 1200, "Ticks per level of depth in the crafting tree.");
            maxTicks = builder.defineInteger("maxTicks", 100, 0, 6000, "The longest a craft takes, in ticks.");
            fallbackTicks = builder.defineInteger("fallbackTicks", 20, 0, 6000,
                    "Ticks for recipes the depth table can't place (special recipes, loops with no base material).");
            timesInventoryGrid = builder.defineBoolean("inventoryGrid", true, "The 2x2 inventory grid is timed.");
            timesMakeshiftTables = builder.defineBoolean("makeshiftTables", true, "The makeshift tables are timed.");
            timesCraftingTable = builder.defineBoolean("craftingTable", true, "The Crafting Table (and other mods' grids) is timed.");
            recipeOverrides = builder.defineStringList("recipeOverrides", List.of(), "Fixed times for single recipes: recipe=ticks.");
            modMultipliers = builder.defineStringList("modMultipliers", List.of(), "Time multipliers for a mod's recipes: modid=1.0.");
        }
    }

    public static final class DyeVatSettings {
        public final ConfigValue<Integer> chargesPerDye;
        public final ConfigValue<Integer> capacity;
        public final ConfigValue<Integer> dipTicks;
        public final ConfigValue<Integer> rainFillTicks;
        public final ConfigValue<Integer> paperPulpTicks;

        private DyeVatSettings(ConfigSpecBuilder builder) {
            chargesPerDye = builder.defineInteger("vatChargesPerDye", 8, 1, 1024, "Charges each dye adds to the vat.");
            capacity = builder.defineInteger("capacity", 32, 1, 4096, "The most charges a vat holds.");
            dipTicks = builder.defineInteger("dipTicks", 10, 1, 1200, "Ticks to dip one item.");
            rainFillTicks = builder.defineInteger("rainFillTicks", 1200, 1, 72000, "Ticks of rain falling on an empty vat to fill it.");
            paperPulpTicks = builder.defineInteger("paperPulpTicks", 20, 1, 1200, "Ticks to soak one sugar cane into Paper Pulp.");
        }
    }

    public static final class AchievementSettings {
        public final ConfigValue<Integer> longCraftTicks;
        public final ConfigValue<Integer> scrapCollectorScraps;
        public final ConfigValue<Integer> masterCraftsmanCrafts;

        private AchievementSettings(ConfigSpecBuilder builder) {
            longCraftTicks = builder.defineInteger("longCraftTicks", 100, 1, 72000, "A craft at least this long counts for \"Patience Is a Virtue\".");
            scrapCollectorScraps = builder.defineInteger("scrapCollectorScraps", 64, 1, 1000000, "Wood Scraps collected for \"Scrap Collector\".");
            masterCraftsmanCrafts = builder.defineInteger("masterCraftsmanCrafts", 1000, 1, 1000000,
                    "Items crafted with Crafting's recipes for \"Master Craftsman\".");
        }
    }
}
