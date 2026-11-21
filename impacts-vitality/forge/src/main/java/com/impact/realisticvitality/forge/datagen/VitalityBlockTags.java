package com.impact.realisticvitality.forge.datagen;

import com.impact.lib.heat.HeatSources;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Block tags: which tool breaks each water block fastest, and Vitality's additions to Lib's heat sources. */
final class VitalityBlockTags extends BlockTagsProvider {
    VitalityBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, VitalityMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(VitalityBlocks.rainBarrel(), VitalityBlocks.waterFilter());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(VitalityBlocks.copperStill(), VitalityBlocks.airCompressor());
        // Lib's tag holds fire, campfires, magma and lava; Vitality's heat table also warms you by lit furnaces and lava cauldrons.
        tag(HeatSources.HEAT_SOURCES).add(Blocks.FURNACE, Blocks.SMOKER, Blocks.BLAST_FURNACE, Blocks.LAVA_CAULDRON);
    }
}
