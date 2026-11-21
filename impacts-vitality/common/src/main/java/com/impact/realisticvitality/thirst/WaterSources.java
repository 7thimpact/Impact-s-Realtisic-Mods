package com.impact.realisticvitality.thirst;

import com.impact.lib.ImpactsLib;
import com.impact.realisticvitality.config.TaggedEntries;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** Where water comes out purified (boiling, says Lib), salty (ocean and beach biomes) or unclean (anywhere else). */
public final class WaterSources {
    private WaterSources() {
    }

    public static WaterPurity purityAt(Level level, BlockPos position) {
        if (ImpactsLib.isBoiling(level, position)) {
            return WaterPurity.PURIFIED;
        }
        return isSalty(level.getBiome(position)) ? WaterPurity.SALT : WaterPurity.UNCLEAN;
    }

    /** Water in a cauldron is never salty: it's unclean unless it boils. */
    public static WaterPurity cauldronPurity(Level level, BlockPos position) {
        return ImpactsLib.isBoiling(level, position) ? WaterPurity.PURIFIED : WaterPurity.UNCLEAN;
    }

    public static boolean isSalty(Holder<Biome> biome) {
        return TaggedEntries.contains(VitalityServerConfig.get().purity.saltWaterBiomes.get(), biome::is,
                tagId -> biome.is(TagKey.create(Registries.BIOME, tagId)));
    }
}
