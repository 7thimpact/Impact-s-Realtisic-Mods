package com.impact.lib.config;

import com.impact.lib.ImpactsLib;

/** Lib's own CLIENT file: {@code config/impactslib/impactslib-client.toml}. */
public final class LibClientConfig {
    private static volatile LibClientConfig registered;

    /** holdToUse.*: the progress shown while holding right-click. */
    public final HoldToUseSettings holdToUse;
    /** hud.*: offsets for every Impact HUD row. */
    public final HudSettings hud;
    public final ConfigFile file;

    private LibClientConfig(ConfigSpecBuilder builder) {
        builder.pushSection("holdToUse", "Hold-to-use actions such as hammering and grinding.");
        holdToUse = new HoldToUseSettings(builder);
        builder.popSection();
        builder.pushSection("hud", "Impact HUD elements (Vitality's thirst and air rows and gauge).");
        hud = new HudSettings(builder);
        builder.popSection();
        file = builder.register();
    }

    /** Builds and registers the file. Called once, during Lib's mod construction. */
    public static LibClientConfig register() {
        registered = new LibClientConfig(ImpactsConfig.builder(ImpactsLib.MOD_ID, ConfigType.CLIENT));
        return registered;
    }

    public static LibClientConfig get() {
        if (registered == null) {
            throw new IllegalStateException("Impact's Lib client config is read before Lib registered it.");
        }
        return registered;
    }

    public static final class HoldToUseSettings {
        public final ConfigValue<Boolean> isProgressBarShown;

        private HoldToUseSettings(ConfigSpecBuilder builder) {
            isProgressBarShown = builder.defineBoolean("showProgressBar", true,
                    "Show a progress bar under the crosshair while holding right-click on a hold-to-use action.");
        }
    }

    public static final class HudSettings {
        private static final int MAXIMUM_OFFSET_PIXELS = 1000;

        public final ConfigValue<Integer> rowOffsetX;
        public final ConfigValue<Integer> rowOffsetY;

        private HudSettings(ConfigSpecBuilder builder) {
            rowOffsetX = builder.defineInteger("rowOffsetX", 0, -MAXIMUM_OFFSET_PIXELS, MAXIMUM_OFFSET_PIXELS,
                    "Pixels to shift every Impact HUD row sideways. Positive is to the right.");
            rowOffsetY = builder.defineInteger("rowOffsetY", 0, -MAXIMUM_OFFSET_PIXELS, MAXIMUM_OFFSET_PIXELS,
                    "Pixels to shift every Impact HUD row up or down. Positive is down.");
        }
    }
}
