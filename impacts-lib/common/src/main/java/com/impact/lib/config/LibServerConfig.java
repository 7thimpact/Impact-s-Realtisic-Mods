package com.impact.lib.config;

import com.impact.lib.ImpactsLib;

import java.util.List;

/**
 * Lib's own SERVER file: {@code <world>/serverconfig/impactslib/impactslib-server.toml}. The fuel and grinder
 * systems that read it arrive with Impact's Realistic Food (ROADMAP step 3).
 */
public final class LibServerConfig {
    private static volatile LibServerConfig registered;

    /** fuel.*: per-item burn time overrides shared by every Impact mod. */
    public final FuelSettings fuel;
    /** grinder.*: Lib's grinder bowls and grinding sticks. */
    public final GrinderSettings grinder;
    public final ConfigFile file;

    private LibServerConfig(ConfigSpecBuilder builder) {
        builder.pushSection("fuel", "Burn times shared by every Impact mod.");
        fuel = new FuelSettings(builder);
        builder.popSection();
        builder.pushSection("grinder", "Lib's grinder bowls and grinding sticks.");
        grinder = new GrinderSettings(builder);
        builder.popSection();
        file = builder.register();
    }

    /** Builds and registers the file. Called once, during Lib's mod construction. */
    public static LibServerConfig register() {
        registered = new LibServerConfig(ImpactsConfig.builder(ImpactsLib.MOD_ID, ConfigType.SERVER));
        return registered;
    }

    public static LibServerConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Impact's Lib server config is read before Lib registered it.");
        }
        return registered;
    }

    public static final class FuelSettings {
        public final ConfigValue<List<String>> overrides;

        private FuelSettings(ConfigSpecBuilder builder) {
            overrides = builder.defineStringList("overrides", List.of(),
                    "Burn time overrides, as item=ticks. Example: [\"impactscrafting:wood_scraps=100\"]. 200 ticks smelt one item.");
        }
    }

    public static final class GrinderSettings {
        private static final int DEFAULT_WOODEN_DURABILITY = 64;
        private static final int DEFAULT_STONE_DURABILITY = 256;
        private static final int MINIMUM_DURABILITY = 1;
        private static final int MAXIMUM_DURABILITY = 10000;
        private static final int DEFAULT_WOODEN_STICK_GRIND_TICKS = 30;
        private static final int DEFAULT_STONE_STICK_GRIND_TICKS = 20;
        private static final int MINIMUM_GRIND_TICKS = 1;
        private static final int MAXIMUM_GRIND_TICKS = 1200;

        public final ConfigValue<Integer> woodenDurability;
        public final ConfigValue<Integer> stoneDurability;
        public final ConfigValue<Integer> woodenStickGrindTicks;
        public final ConfigValue<Integer> stoneStickGrindTicks;

        private GrinderSettings(ConfigSpecBuilder builder) {
            woodenDurability = builder.defineInteger("woodenDurability", DEFAULT_WOODEN_DURABILITY,
                    MINIMUM_DURABILITY, MAXIMUM_DURABILITY, "Items a wooden grinder bowl or grinding stick grinds before it breaks.");
            stoneDurability = builder.defineInteger("stoneDurability", DEFAULT_STONE_DURABILITY,
                    MINIMUM_DURABILITY, MAXIMUM_DURABILITY, "Items a stone grinder bowl or grinding stick grinds before it breaks.");
            woodenStickGrindTicks = builder.defineInteger("woodenStickGrindTicks", DEFAULT_WOODEN_STICK_GRIND_TICKS,
                    MINIMUM_GRIND_TICKS, MAXIMUM_GRIND_TICKS, "Ticks to grind one item with a wooden grinding stick.");
            stoneStickGrindTicks = builder.defineInteger("stoneStickGrindTicks", DEFAULT_STONE_STICK_GRIND_TICKS,
                    MINIMUM_GRIND_TICKS, MAXIMUM_GRIND_TICKS, "Ticks to grind one item with a stone grinding stick.");
        }
    }
}
