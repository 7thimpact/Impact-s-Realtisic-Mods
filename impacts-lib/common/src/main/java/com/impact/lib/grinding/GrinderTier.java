package com.impact.lib.grinding;

import com.impact.lib.config.LibServerConfig;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Optional;

/** A grinder's material. Wood handles soft items (flowers, berries); stone also grinds hard ones (bone, wheat, lapis). */
public enum GrinderTier implements StringRepresentable {
    WOOD("wood"),
    STONE("stone");

    private final String serializedName;

    GrinderTier(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /** Whether a grinder of this tier can grind a recipe that needs {@code required}. */
    public boolean reaches(GrinderTier required) {
        return ordinal() >= required.ordinal();
    }

    /** Uses before a bowl or stick of this tier breaks: 64 wood, 256 stone (server config). */
    public int durability() {
        LibServerConfig.GrinderSettings grinder = LibServerConfig.get().grinder;
        return this == WOOD ? grinder.woodenDurability.get() : grinder.stoneDurability.get();
    }

    /** Ticks a stick of this tier takes to grind one item: 30 wood, 20 stone (server config). */
    public int grindTicks() {
        LibServerConfig.GrinderSettings grinder = LibServerConfig.get().grinder;
        return this == WOOD ? grinder.woodenStickGrindTicks.get() : grinder.stoneStickGrindTicks.get();
    }

    public static Optional<GrinderTier> byName(String name) {
        return Arrays.stream(values()).filter(tier -> tier.serializedName.equals(name)).findFirst();
    }
}
