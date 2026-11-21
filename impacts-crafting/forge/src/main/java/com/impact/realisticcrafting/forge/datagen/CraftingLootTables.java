package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.item.UsesDurability;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

/** Loot tables: loose pebbles always drop one Stone Pebble, and makeshift tables drop themselves with their uses. */
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
            for (MakeshiftTable table : MakeshiftTable.values()) {
                add(table.block(), keepingUses(table.block()));
            }
            dropSelf(CraftingBlocks.dyeVat());
        }

        // The table drops itself carrying its uses spent; an unused table's item has no tag, so fresh tables stack.
        private LootTable.Builder keepingUses(Block tableBlock) {
            return LootTable.lootTable().withPool(applyExplosionCondition(tableBlock, LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(tableBlock)
                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(UsesDurability.USES_KEY, UsesDurability.USES_KEY)))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(CraftingBlocks.loosePebble(), MakeshiftTable.WOODEN.block(), MakeshiftTable.STONE.block(), CraftingBlocks.dyeVat());
        }
    }
}
