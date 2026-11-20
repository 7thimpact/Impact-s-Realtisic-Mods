package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

/** Loot tables: loose pebbles always drop one Stone Pebble. */
final class CraftingLootTables {
    private CraftingLootTables() {
    }

    static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)));
    }

    private static final class BlockLoot extends BlockLootSubProvider {
        BlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            dropOther(CraftingBlocks.loosePebble(), CraftingItems.stonePebble());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(CraftingBlocks.loosePebble());
        }
    }
}
