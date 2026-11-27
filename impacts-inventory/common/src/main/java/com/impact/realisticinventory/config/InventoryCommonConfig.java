package com.impact.realisticinventory.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.DebugConfigSection;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticinventory.InventoryMod;

/** Inventory's COMMON file: feature toggles that recipe conditions read, and debug settings. */
public final class InventoryCommonConfig {
    private static volatile InventoryCommonConfig registered;

    public final FeatureSettings features;
    public final DebugConfigSection debug;
    public final ConfigFile file;

    private InventoryCommonConfig(ConfigSpecBuilder builder) {
        builder.pushSection("packs", "Turning packs off also turns off their recipes (applies on /reload).");
        features = new FeatureSettings(builder);
        builder.popSection();
        debug = DebugConfigSection.define(builder);
        file = builder.register();
    }

    public static InventoryCommonConfig register() {
        registered = new InventoryCommonConfig(ImpactsConfig.builder(InventoryMod.MOD_ID, ConfigType.COMMON));
        return registered;
    }

    public static InventoryCommonConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Inventory's common config is read before it was registered.");
        }
        return registered;
    }

    public static final class FeatureSettings {
        public final ConfigValue<Boolean> arePacksEnabled;
        public final ConfigValue<Boolean> arePlacedPacksEnabled;
        public final ConfigValue<Boolean> isBackSlotEnabled;
        public final ConfigValue<Boolean> isChestSlotEnabled;

        private FeatureSettings(ConfigSpecBuilder builder) {
            arePacksEnabled = builder.defineBoolean("enabled", true, "Satchels, backpacks and frame packs, and their recipes.");
            arePlacedPacksEnabled = builder.defineBoolean("placedPacks", true, "Setting a pack down as a block.");
            isBackSlotEnabled = builder.defineBoolean("backSlot", true, "The back slot. Without it, packs can't be worn on the back.");
            isChestSlotEnabled = builder.defineBoolean("chestSlot", true, "The chest slot (Curios's body slot): a second pack worn on the chest.");
        }
    }
}
