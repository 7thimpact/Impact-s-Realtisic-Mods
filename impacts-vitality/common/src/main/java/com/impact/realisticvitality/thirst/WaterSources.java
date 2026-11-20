package com.impact.realisticvitality.thirst;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** Where water comes out purified (boiling, says Lib), salty (ocean and beach biomes) or unclean (anywhere else). */
public final class WaterSources {
    private static final String TAG_PREFIX = "#";

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
        return VitalityServerConfig.get().purity.saltWaterBiomes.get().stream().anyMatch(entry -> matches(entry.trim(), biome));
    }

    private static boolean matches(String entry, Holder<Biome> biome) {
        if (entry.startsWith(TAG_PREFIX)) {
            return ResourceIds.tryParse(entry.substring(TAG_PREFIX.length()))
                    .map(tagId -> biome.is(TagKey.create(Registries.BIOME, tagId)))
                    .orElse(false);
        }
        return ResourceIds.tryParse(entry).map(biome::is).orElse(false);
    }
}
