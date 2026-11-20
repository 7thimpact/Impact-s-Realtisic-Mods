package com.impact.realisticvitality.air;

import com.impact.realisticvitality.config.TaggedEntries;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * The air at a spot. There's no air circulation: only height, the dimension and nearby lava matter, so a furnace or
 * campfire in a sealed house never affects breathing. Of everything that applies, the air that takes bubbles fastest wins.
 */
public final class AirQualities {
    private AirQualities() {
    }

    /** The air around a body: its dimension and height, and lava within reach of its bounding box. */
    public static AirQuality around(Level level, AABB body) {
        BlockPos eyes = BlockPos.containing(body.getCenter().x, body.maxY, body.getCenter().z);
        return ofPlace(level.dimension(), eyes.getY()).worseOf(ofLava(level, body));
    }

    /** The air of a dimension at a height: its data file's quality, or height bands in the Overworld. */
    public static AirQuality ofPlace(ResourceKey<Level> dimension, int y) {
        AirQuality dimensionAir = DimensionAirQualities.INSTANCE.of(dimension.location()).orElse(AirQuality.GOOD);
        boolean hasHeightBands = TaggedEntries.contains(VitalityServerConfig.get().air.heightBandDimensions.get(),
                dimension.location()::equals, tagId -> false);
        return hasHeightBands ? dimensionAir.worseOf(ofHeight(y)) : dimensionAir;
    }

    public static AirQuality ofHeight(int y) {
        VitalityServerConfig.AirSettings settings = VitalityServerConfig.get().air;
        if (y > settings.veryThinAboveY.get()) {
            return AirQuality.VERY_THIN;
        }
        if (y > settings.thinAboveY.get() || y < settings.goodFromY.get()) {
            return AirQuality.THIN;
        }
        return AirQuality.GOOD;
    }

    /** Lava (source, flowing or in a cauldron) right next to the body is Very Smoky; within 3 blocks, Smoky. */
    public static AirQuality ofLava(Level level, AABB body) {
        VitalityServerConfig.AirSettings settings = VitalityServerConfig.get().air;
        if (hasLavaWithin(level, body, settings.lavaVerySmokyRadius.get())) {
            return AirQuality.VERY_SMOKY;
        }
        return hasLavaWithin(level, body, settings.lavaSmokyRadius.get()) ? AirQuality.SMOKY : AirQuality.GOOD;
    }

    private static boolean hasLavaWithin(Level level, AABB body, int radius) {
        AABB reach = body.inflate(radius);
        BlockPos from = BlockPos.containing(reach.minX, reach.minY, reach.minZ);
        BlockPos to = BlockPos.containing(reach.maxX, reach.maxY, reach.maxZ);
        for (BlockPos position : BlockPos.betweenClosed(from, to)) {
            boolean isLava = level.getFluidState(position).is(FluidTags.LAVA) || level.getBlockState(position).is(Blocks.LAVA_CAULDRON);
            if (isLava) {
                return true;
            }
        }
        return false;
    }
}
