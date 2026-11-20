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
    public final ConfigFile file;

    private CraftingServerConfig(ConfigSpecBuilder builder) {
        builder.pushSection("pebbles", "Stone Pebbles: the first tool. Loose pebbles, gravel and stone are the only sources.");
        pebbles = new PebbleSettings(builder);
        builder.popSection();
        builder.pushSection("durability", "Uses of Crafting's tools.");
        durability = new DurabilitySettings(builder);
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
}
