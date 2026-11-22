package com.impact.realisticfood.forge.datagen;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.bucket.CampfireBuckets;
import com.impact.realisticfood.stoneboiling.StoneBoilingPots;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Food's blocks in vanilla's tool tags: the campfire bucket is metal (a pickaxe breaks it fastest), the stone-boiling pot wood (an axe). */
final class FoodBlockTags extends BlockTagsProvider {
    FoodBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, FoodMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(CampfireBuckets.block());
        tag(BlockTags.MINEABLE_WITH_AXE).add(StoneBoilingPots.block());
    }
}
