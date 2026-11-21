package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Block tags: where loose pebbles generate, which broken blocks drop pebbles, and the tools that break the tables fastest. */
final class CraftingBlockTags extends BlockTagsProvider {
    CraftingBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CraftingMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(CraftingTags.PEBBLE_BASE).add(Blocks.STONE, Blocks.COBBLESTONE, Blocks.DEEPSLATE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
                Blocks.TUFF, Blocks.CALCITE, Blocks.MOSSY_COBBLESTONE);
        tag(CraftingTags.PEBBLE_WORLDGEN_ONLY).add(Blocks.CALCITE, Blocks.MOSSY_COBBLESTONE);
        tag(CraftingTags.PEBBLE_GRAVEL).add(Blocks.GRAVEL);
        tag(BlockTags.MINEABLE_WITH_AXE).add(MakeshiftTable.WOODEN.block(), CraftingBlocks.dyeVat());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MakeshiftTable.STONE.block());
    }
}
