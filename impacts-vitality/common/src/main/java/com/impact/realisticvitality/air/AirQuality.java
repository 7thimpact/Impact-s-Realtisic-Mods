package com.impact.realisticvitality.air;

import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

/** How breathable the air is. Thin air comes from height or the dimension; smoke from the Nether and nearby lava. */
public enum AirQuality {
    GOOD("good", false),
    THIN("thin", false),
    VERY_THIN("very_thin", false),
    SMOKY("smoky", true),
    VERY_SMOKY("very_smoky", true);

    /** The name in data files, logs and triggers. */
    public final String serializedName;
    /** Smoke (fumes) rather than thin air: a filtered Respirator answers smoke, an Air Tank thin air. */
    public final boolean isSmoke;

    AirQuality(String serializedName, boolean isSmoke) {
        this.serializedName = serializedName;
        this.isSmoke = isSmoke;
    }

    public static Optional<AirQuality> bySerializedName(String name) {
        return Arrays.stream(values()).filter(quality -> quality.serializedName.equals(name)).findFirst();
    }

    /** Ticks per bubble lost in this air, or nothing for good air. */
    public OptionalInt bubbleTicks() {
        VitalityServerConfig.AirSettings settings = VitalityServerConfig.get().air;
        return switch (this) {
            case GOOD -> OptionalInt.empty();
            case THIN -> OptionalInt.of(settings.thinBubbleTicks.get());
            case VERY_THIN -> OptionalInt.of(settings.veryThinBubbleTicks.get());
            case SMOKY -> OptionalInt.of(settings.smokyBubbleTicks.get());
            case VERY_SMOKY -> OptionalInt.of(settings.verySmokyBubbleTicks.get());
        };
    }

    /** The worse of two kinds of air: the one that takes bubbles faster. */
    public AirQuality worseOf(AirQuality other) {
        int ownTicks = bubbleTicks().orElse(Integer.MAX_VALUE);
        int otherTicks = other.bubbleTicks().orElse(Integer.MAX_VALUE);
        return otherTicks < ownTicks ? other : this;
    }

    public ResourceKey<DamageType> damageType() {
        return isSmoke ? VitalityDamageTypes.FUMES : VitalityDamageTypes.THIN_AIR;
    }
}
