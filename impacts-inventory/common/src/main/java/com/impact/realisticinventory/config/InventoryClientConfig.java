package com.impact.realisticinventory.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.ImpactsConfig;
import com.impact.realisticinventory.InventoryMod;

/** Inventory's CLIENT file: the hotbar's look and the worn pack's rendering. */
public final class InventoryClientConfig {
    private static volatile InventoryClientConfig registered;

    public final HotbarDisplaySettings hotbar;
    public final PackDisplaySettings packs;
    public final ConfigFile file;

    private InventoryClientConfig(ConfigSpecBuilder builder) {
        builder.pushSection("hotbar", "How the two-slot hotbar is drawn.");
        hotbar = new HotbarDisplaySettings(builder);
        builder.popSection();
        builder.pushSection("packs", "How packs are drawn.");
        packs = new PackDisplaySettings(builder);
        builder.popSection();
        file = builder.register();
    }

    public static InventoryClientConfig register() {
        registered = new InventoryClientConfig(ImpactsConfig.builder(InventoryMod.MOD_ID, ConfigType.CLIENT));
        return registered;
    }

    public static InventoryClientConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Inventory's client config is read before it was registered.");
        }
        return registered;
    }

    public static final class HotbarDisplaySettings {
        private static final int MAXIMUM_OFFSET_PIXELS = 1000;

        public final ConfigValue<Integer> offsetX;
        public final ConfigValue<Integer> offsetY;
        public final ConfigValue<Double> scale;
        public final ConfigValue<Boolean> areSlotLabelsShown;

        private HotbarDisplaySettings(ConfigSpecBuilder builder) {
            offsetX = builder.defineInteger("offsetX", 0, -MAXIMUM_OFFSET_PIXELS, MAXIMUM_OFFSET_PIXELS, "Pixels to move the hotbar sideways.");
            offsetY = builder.defineInteger("offsetY", 0, -MAXIMUM_OFFSET_PIXELS, MAXIMUM_OFFSET_PIXELS, "Pixels to move the hotbar up (negative) or down.");
            scale = builder.defineDouble("scale", 1.0, 0.5, 3.0, "Size of the hotbar.");
            areSlotLabelsShown = builder.defineBoolean("showSlotLabels", true, "Show the labels 1 and 2 on the hotbar slots.");
        }
    }

    public static final class PackDisplaySettings {
        public final ConfigValue<Boolean> isRenderedOnPlayer;

        private PackDisplaySettings(ConfigSpecBuilder builder) {
            isRenderedOnPlayer = builder.defineBoolean("renderOnPlayer", true, "Draw the worn pack on players' backs.");
        }
    }
}
