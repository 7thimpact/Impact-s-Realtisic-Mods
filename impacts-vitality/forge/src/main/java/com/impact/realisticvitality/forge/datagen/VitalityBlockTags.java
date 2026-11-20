package com.impact.realisticvitality.forge.datagen;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Block tags: which tool breaks each water block fastest. */
final class VitalityBlockTags extends BlockTagsProvider {
    VitalityBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, VitalityMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(VitalityBlocks.rainBarrel(), VitalityBlocks.waterFilter());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(VitalityBlocks.copperStill());
    }
}
