package com.impact.realisticinventory.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticinventory.InventoryMod;

import java.util.List;

/** Inventory's SERVER file: {@code <world>/serverconfig/impactslib/impactsinventory-server.toml}. Every gameplay number. */
public final class InventoryServerConfig {
    private static volatile InventoryServerConfig registered;

    /** creativeVanilla: creative mode keeps vanilla's hotbar and inventory. */
    public final ConfigValue<Boolean> isCreativeVanilla;
    public final HotbarSettings hotbar;
    public final PickupSettings pickup;
    public final PackSettings packs;
    public final FirstJoinSettings firstJoin;
    public final ConfigFile file;

    private InventoryServerConfig(ConfigSpecBuilder builder) {
        isCreativeVanilla = builder.defineBoolean("creativeVanilla", true,
                "Creative mode keeps vanilla's 9-slot hotbar and full inventory.",
                "Switching back to survival moves items out of locked slots, as on first join.");
        builder.pushSection("hotbar", "The two-slot hotbar.");
        hotbar = new HotbarSettings(builder);
        builder.popSection();
        builder.pushSection("pickup", "Picking items up.");
        pickup = new PickupSettings(builder);
        builder.popSection();
        builder.pushSection("packs", "Satchels, backpacks and frame packs.");
        packs = new PackSettings(builder);
        builder.popSection();
        builder.pushSection("firstJoin", "Joining a world with items in now-locked slots.");
        firstJoin = new FirstJoinSettings(builder);
        builder.popSection();
        file = builder.register();
    }

    public static InventoryServerConfig register() {
        registered = new InventoryServerConfig(ImpactsConfig.builder(InventoryMod.MOD_ID, ConfigType.SERVER));
        return registered;
    }

    public static InventoryServerConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Inventory's server config is read before it was registered.");
        }
        return registered;
    }

    public static final class HotbarSettings {
        private static final int MAXIMUM_SWAP_COOLDOWN_TICKS = 200;

        public final ConfigValue<Integer> swapCooldownTicks;
        public final ConfigValue<Boolean> isActiveSlotKeptWithKeepInventory;

        private HotbarSettings(ConfigSpecBuilder builder) {
            swapCooldownTicks = builder.defineInteger("swapCooldownTicks", 0, 0, MAXIMUM_SWAP_COOLDOWN_TICKS,
                    "Ticks after switching slots before the new main-hand item can attack or be used.");
            isActiveSlotKeptWithKeepInventory = builder.defineBoolean("keepActiveSlotWithKeepInventory", true,
                    "With the keepInventory game rule, the active slot survives death too.");
        }
    }

    public static final class PickupSettings {
        private static final int DEFAULT_FULL_MESSAGE_INTERVAL_TICKS = 60;
        private static final int MINIMUM_FULL_MESSAGE_INTERVAL_TICKS = 1;
        private static final int MAXIMUM_FULL_MESSAGE_INTERVAL_TICKS = 6000;

        public final ConfigValue<Boolean> isIntoPack;
        public final ConfigValue<Boolean> isFullMessageShown;
        public final ConfigValue<Integer> fullMessageIntervalTicks;
        public final ConfigValue<List<String>> order;

        private PickupSettings(ConfigSpecBuilder builder) {
            isIntoPack = builder.defineBoolean("intoPack", true,
                    "Items that don't fit in the hands or pockets go into the worn pack.");
            isFullMessageShown = builder.defineBoolean("fullMessage", true,
                    "Show \"Pockets full\" in the action bar when an item can't be picked up.");
            fullMessageIntervalTicks = builder.defineInteger("fullMessageIntervalTicks", DEFAULT_FULL_MESSAGE_INTERVAL_TICKS,
                    MINIMUM_FULL_MESSAGE_INTERVAL_TICKS, MAXIMUM_FULL_MESSAGE_INTERVAL_TICKS,
                    "Ticks between two \"Pockets full\" messages.");
            order = builder.defineStringList("order", List.of("hands", "pockets", "pack"),
                    "Where picked-up items go, in order. Allowed: hands, pockets, pack.");
        }
    }

    public static final class PackSettings {
        private static final int DEFAULT_SATCHEL_SLOTS = 3;
        private static final int DEFAULT_BACKPACK_SLOTS = 9;
        private static final int DEFAULT_FRAME_PACK_SLOTS = 18;
        private static final int MINIMUM_PACK_SLOTS = 1;
        private static final int MAXIMUM_PACK_SLOTS = 54;
        private static final double DEFAULT_FRAME_PACK_SLOWNESS = 0.1;

        public final ConfigValue<Boolean> isShulkerAllowed;
        public final ConfigValue<Double> framePackSlowness;
        public final ConfigValue<Integer> satchelSlots;
        public final ConfigValue<Integer> backpackSlots;
        public final ConfigValue<Integer> framePackSlots;
        public final ConfigValue<List<String>> nestingBlacklist;

        private PackSettings(ConfigSpecBuilder builder) {
            isShulkerAllowed = builder.defineBoolean("allowShulkers", false, "Shulker boxes may go inside a pack.");
            framePackSlowness = builder.defineDouble("framePackSlowness", DEFAULT_FRAME_PACK_SLOWNESS, 0.0, 1.0,
                    "How much slower a worn Frame Pack makes walking (0.1 = 10% slower).");
            satchelSlots = builder.defineInteger("satchelSlots", DEFAULT_SATCHEL_SLOTS, MINIMUM_PACK_SLOTS, MAXIMUM_PACK_SLOTS, "Slots in a Satchel.");
            backpackSlots = builder.defineInteger("backpackSlots", DEFAULT_BACKPACK_SLOTS, MINIMUM_PACK_SLOTS, MAXIMUM_PACK_SLOTS, "Slots in a Backpack.");
            framePackSlots = builder.defineInteger("framePackSlots", DEFAULT_FRAME_PACK_SLOTS, MINIMUM_PACK_SLOTS, MAXIMUM_PACK_SLOTS, "Slots in a Frame Pack.");
            nestingBlacklist = builder.defineStringList("nestingBlacklist", List.of("#impactsinventory:packs"),
                    "Items that can't go inside a pack, as item IDs or #tags. Shulker boxes follow allowShulkers.");
        }
    }

    public static final class FirstJoinSettings {
        public final ConfigValue<OverflowTarget> overflow;

        private FirstJoinSettings(ConfigSpecBuilder builder) {
            overflow = builder.defineEnum("overflow", OverflowTarget.CHEST,
                    "Where items go that fit neither the pockets nor a worn pack: a chest placed at the player's feet, or dropped.");
        }
    }

    /** Where overflow items go on first join and when leaving creative mode. */
    public enum OverflowTarget { CHEST, DROP }
}
