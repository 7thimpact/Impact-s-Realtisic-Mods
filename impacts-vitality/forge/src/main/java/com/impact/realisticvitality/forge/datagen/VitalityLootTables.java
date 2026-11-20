package com.impact.realisticvitality.forge.datagen;

import com.impact.realisticvitality.registry.VitalityBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

/** Loot tables: each water block drops itself (the Water Filter and Copper Still also drop what's inside, in code). */
final class VitalityLootTables {
    private VitalityLootTables() {
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
            VitalityBlocks.all().forEach(this::dropSelf);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return VitalityBlocks.all();
        }
    }
}
