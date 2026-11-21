package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.item.UsesDurability;
import com.impact.realisticcrafting.bed.BedFrameBlock;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.PebbleKind;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
            Arrays.stream(PebbleKind.values()).forEach(kind -> dropOther(CraftingBlocks.loosePebble(kind), CraftingItems.pebble(kind)));
            for (MakeshiftTable table : MakeshiftTable.values()) {
                add(table.block(), keepingUses(table.block()));
            }
            dropSelf(CraftingBlocks.dyeVat());
            add(CraftingBlocks.bedFrame(), bedFrameDrops());
        }

        // Only the head half drops: the frame, and the comforter if one was laid.
        private LootTable.Builder bedFrameDrops() {
            Block frame = CraftingBlocks.bedFrame();
            LootItemCondition.Builder isHead = LootItemBlockStatePropertyCondition.hasBlockStateProperties(frame)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BedFrameBlock.PART, BedPart.HEAD));
            LootItemCondition.Builder hasComforter = LootItemBlockStatePropertyCondition.hasBlockStateProperties(frame)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BedFrameBlock.PART, BedPart.HEAD)
                            .hasProperty(BedFrameBlock.COMFORTER, true));
            return LootTable.lootTable()
                    .withPool(applyExplosionCondition(frame, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(frame).when(isHead))))
                    .withPool(applyExplosionCondition(frame, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(CraftingItems.comforter()).when(hasComforter))));
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
            Stream<Block> otherBlocks = Stream.of(MakeshiftTable.WOODEN.block(), MakeshiftTable.STONE.block(), CraftingBlocks.dyeVat(), CraftingBlocks.bedFrame());
            return Stream.concat(CraftingBlocks.loosePebbles().stream(), otherBlocks).toList();
        }
    }
}
