package com.impact.lib.forge.datagen;

import com.impact.lib.ImpactsLib;
import com.impact.lib.heat.HeatSources;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Lib's block tags: heat sources (fire, campfires, magma, lava) and cold sources (ice and snow). Modded blocks can be added. */
final class LibBlockTagsProvider extends BlockTagsProvider {
    LibBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, ImpactsLib.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(HeatSources.HEAT_SOURCES).add(Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.MAGMA_BLOCK, Blocks.LAVA);
        tag(HeatSources.COLD_SOURCES).add(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
    }
}
