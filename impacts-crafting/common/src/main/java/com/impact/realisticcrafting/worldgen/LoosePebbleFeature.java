package com.impact.realisticcrafting.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Loose pebbles for one chunk, on the surface or on cave floors. The feature makes its own tries across the chunk (the
 * number comes from the server config), so its placed feature needs no placement modifiers.
 */
public final class LoosePebbleFeature extends Feature<NoneFeatureConfiguration> {
    private final Spot spot;

    public LoosePebbleFeature(Spot spot) {
        super(Codec.unit(NoneFeatureConfiguration.INSTANCE));
        this.spot = spot;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int placed = spot == Spot.SURFACE
                ? LoosePebblePlacer.placeOnSurface(context.level(), context.origin(), context.random())
                : LoosePebblePlacer.placeInCaves(context.level(), context.origin(), context.random());
        return placed > 0;
    }

    public enum Spot {
        SURFACE,
        CAVE
    }
}
