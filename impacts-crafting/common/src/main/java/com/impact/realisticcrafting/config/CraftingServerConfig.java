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
            surfaceTriesPerChunk = builder.defineInteger("surfaceTriesPerChunk", 4, 0, 256, "Tries per chunk to place a loose pebble on the surface.");
            caveTriesPerChunk = builder.defineInteger("caveTriesPerChunk", 6, 0, 256, "Tries per chunk to place a loose pebble on a cave floor.");
            worldgenDimensions = builder.defineStringList("worldgenDimensions", List.of("minecraft:overworld"),
                    "Dimensions loose pebbles generate in.");
            worldgenBiomeBlacklist = builder.defineStringList("worldgenBiomeBlacklist", List.of(),
                    "Biomes (IDs or #tags) where loose pebbles never generate.");
        }
    }

    public static final class DurabilitySettings {
        public final ConfigValue<Integer> pebbleUses;

        private DurabilitySettings(ConfigSpecBuilder builder) {
            pebbleUses = builder.defineInteger("pebble", 32, 1, MAXIMUM_USES, "Uses of a Stone Pebble.");
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
}
