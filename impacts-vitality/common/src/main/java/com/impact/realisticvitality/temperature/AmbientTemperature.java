package com.impact.realisticvitality.temperature;

import com.impact.realisticvitality.config.TaggedEntries;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.OptionalDouble;

/**
 * The ambient temperature at a spot, in °F: the biome, the dimension, height, time of day, weather and sun (with open
 * sky above), caves drifting toward their own steady warmth, nearby hot and cold blocks, and standing in water.
 */
public final class AmbientTemperature {
    private static final double FULL_DAY = 2.0 * Math.PI;

    private AmbientTemperature() {
    }

    public static AmbientReading at(Level level, BlockPos position, boolean isInWater) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        double blocks = BlockTemperatures.near(level, position);
        double water = isInWater ? -settings.waterDrop.get() : 0.0;
        OptionalDouble fixed = dimensionFixed(level.dimension());
        if (fixed.isPresent()) {
            return new AmbientReading(0.0, fixed.getAsDouble(), 0.0, 0.0, 0.0, 0.0, 0.0, blocks, water);
        }
        Holder<Biome> biome = level.getBiome(position);
        boolean hasOpenSky = level.canSeeSky(position);
        double biomeBase = biomeBase(biome);
        double dimension = dimensionOffset(level.dimension());
        double height = heightAdjustment(position.getY());
        double time = timeAdjustment(level, biome);
        double weather = hasOpenSky ? weatherAdjustment(level, position, biome) : 0.0;
        double sun = hasOpenSky ? sunAdjustment(level) : 0.0;
        double surface = biomeBase + dimension + height + time + weather + sun;
        double underground = hasOpenSky ? 0.0 : undergroundAdjustment(surface, position.getY());
        return new AmbientReading(biomeBase, dimension, height, time, weather, sun, underground, blocks, water);
    }

    /** {@code biomeBase + biomeScale x} the biome's vanilla temperature, unless the biome has an override. */
    public static double biomeBase(Holder<Biome> biome) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        OptionalDouble override = TaggedEntries.findNumber(settings.biomeOverrides.get(), biome::is,
                tagId -> biome.is(TagKey.create(Registries.BIOME, tagId)));
        if (override.isPresent()) {
            return override.getAsDouble();
        }
        return settings.biomeBase.get() + settings.biomeScale.get() * biome.value().getBaseTemperature();
    }

    public static double dimensionOffset(ResourceKey<Level> dimension) {
        return TaggedEntries.findNumber(VitalityServerConfig.get().temperature.dimensionOffsets.get(), dimension.location()::equals,
                tagId -> false).orElse(0.0);
    }

    public static OptionalDouble dimensionFixed(ResourceKey<Level> dimension) {
        return TaggedEntries.findNumber(VitalityServerConfig.get().temperature.dimensionFixed.get(), dimension.location()::equals,
                tagId -> false);
    }

    /** Colder above {@code coolingAboveY}: 1°F per {@code blocksPerDegreeAbove} blocks. */
    public static double heightAdjustment(int y) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        int above = y - settings.coolingAboveY.get();
        return above > 0 ? -above / settings.blocksPerDegreeAbove.get() : 0.0;
    }

    /**
     * With no sky above and below {@code undergroundBelowY}, the surface temperature gives way to the cave's: fully
     * {@code undergroundBlendBlocks} lower. The cave's own temperature rises from 55°F at Y 0 to 75°F at the bottom.
     */
    public static double undergroundAdjustment(double surface, int y) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        int depth = settings.undergroundBelowY.get() - y;
        if (depth <= 0) {
            return 0.0;
        }
        double blend = Math.min(1.0, (double) depth / settings.undergroundBlendBlocks.get());
        return (caveTemperature(y) - surface) * blend;
    }

    private static double caveTemperature(int y) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        int deepStart = settings.deepBelowY.get();
        if (y >= deepStart) {
            return settings.undergroundTemperature.get();
        }
        double deepShare = Mth.clamp((double) (deepStart - y) / (deepStart - settings.deepestY.get()), 0.0, 1.0);
        return Mth.lerp(deepShare, settings.undergroundTemperature.get(), settings.deepTemperature.get());
    }

    // Night cools smoothly to its full drop at midnight; deserts and badlands drop further.
    private static double timeAdjustment(Level level, Holder<Biome> biome) {
        if (!hasDayCycle(level)) {
            return 0.0;
        }
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        double nightShare = (1.0 - Math.cos(level.getTimeOfDay(1.0F) * FULL_DAY)) / 2.0;
        boolean isDesert = TaggedEntries.contains(settings.desertBiomes.get(), biome::is, tagId -> biome.is(TagKey.create(Registries.BIOME, tagId)));
        double drop = isDesert ? settings.desertNightDrop.get() : settings.nightDrop.get();
        return -drop * nightShare;
    }

    private static double weatherAdjustment(Level level, BlockPos position, Holder<Biome> biome) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        double rainLevel = level.getRainLevel(1.0F);
        return switch (biome.value().getPrecipitationAt(position)) {
            case RAIN -> -settings.rainDrop.get() * rainLevel;
            case SNOW -> -settings.snowDrop.get() * rainLevel;
            case NONE -> 0.0;
        };
    }

    // The sun warms most at midday, and not through rain clouds.
    private static double sunAdjustment(Level level) {
        if (!hasDayCycle(level)) {
            return 0.0;
        }
        double sunShare = Math.max(0.0, Math.cos(level.getTimeOfDay(1.0F) * FULL_DAY));
        return VitalityServerConfig.get().temperature.sunBonus.get() * sunShare * (1.0 - level.getRainLevel(1.0F));
    }

    private static boolean hasDayCycle(Level level) {
        return level.dimensionType().hasSkyLight() && !level.dimensionType().hasFixedTime();
    }
}
