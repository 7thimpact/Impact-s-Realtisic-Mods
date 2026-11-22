package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.PlacedPacks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** A placed pack is storage, so piglins guard it like a chest or a barrel. */
final class InventoryBlockTags extends BlockTagsProvider {
    InventoryBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, InventoryMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(BlockTags.GUARDED_BY_PIGLINS).add(PlacedPacks.block());
    }
}
