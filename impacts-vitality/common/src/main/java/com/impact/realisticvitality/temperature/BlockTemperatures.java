package com.impact.realisticvitality.temperature;

import com.impact.lib.ResourceIds;
import com.impact.lib.heat.HeatSources;
import com.impact.realisticvitality.config.TaggedEntries;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Warmth and cold from blocks within a few blocks: lit heat sources ({@code #impactslib:heat_sources}, through Lib's
 * {@code isHeated}) and cold sources ({@code #impactslib:cold_sources}), each worth its {@code temperature.blockTemps}
 * value right next to you and less further away. Heat and cold are capped separately.
 */
public final class BlockTemperatures {
    private static final Map<Block, Double> VALUE_BY_BLOCK = new ConcurrentHashMap<>();
    private static volatile List<String> cachedEntries = List.of();

    private BlockTemperatures() {
    }

    /** Tags or the config changed: values are looked up again. */
    public static void clearCache() {
        VALUE_BY_BLOCK.clear();
    }

    /** The °F nearby blocks add (heat) and take away (cold) at the spot, with heat and cold each capped. */
    public static double near(Level level, BlockPos center) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        refreshCacheIfChanged(settings.blockTemps.get());
        int radius = settings.blockRadius.get();
        double heat = 0.0;
        double cold = 0.0;
        for (BlockPos position : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            double value = valueAt(level, position);
            double weighted = value * closeness(center, position, radius);
            if (weighted > 0) {
                heat += weighted;
            } else {
                cold += weighted;
            }
        }
        return Math.min(heat, settings.blockHeatCap.get()) + Math.max(cold, -settings.blockColdCap.get());
    }

    /** The °F one block gives right next to you: positive for a lit heat source, negative for a cold source, else 0. */
    public static double valueAt(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        if (HeatSources.isHeated(level, position)) {
            return listedValue(state).orElse(VitalityServerConfig.get().temperature.unlistedHeatSource.get());
        }
        if (state.is(HeatSources.COLD_SOURCES)) {
            return listedValue(state).orElse(-VitalityServerConfig.get().temperature.unlistedColdSource.get());
        }
        return 0.0;
    }

    // Right next to you a block counts fully; at the edge of the radius it counts 1/radius.
    private static double closeness(BlockPos center, BlockPos position, int radius) {
        double distance = Math.sqrt(center.distSqr(position));
        return Math.max(0.0, Math.min(1.0, 1.0 - (distance - 1.0) / radius));
    }

    private static OptionalDouble listedValue(BlockState state) {
        Double value = VALUE_BY_BLOCK.computeIfAbsent(state.getBlock(), block -> TaggedEntries.findNumber(cachedEntries,
                ResourceIds.blockId(block)::equals, tagId -> state.is(TagKey.create(Registries.BLOCK, tagId))).orElse(Double.NaN));
        return value.isNaN() ? OptionalDouble.empty() : OptionalDouble.of(value);
    }

    private static void refreshCacheIfChanged(List<String> entries) {
        if (!entries.equals(cachedEntries)) {
            cachedEntries = List.copyOf(entries);
            clearCache();
        }
    }
}
