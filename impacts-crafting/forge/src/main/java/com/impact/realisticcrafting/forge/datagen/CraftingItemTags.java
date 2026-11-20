package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Crafting's item tags. The tier tags hold materials, not finished goods: iron, copper and redstone are stone tier,
 * every other ore material is advanced. Stone tier wins where both match (the resolver checks it first).
 */
final class CraftingItemTags extends ItemTagsProvider {
    CraftingItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), CraftingMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(CraftingTags.TIER_STONE).addTags(Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_COPPER, Tags.Items.NUGGETS_IRON,
                Tags.Items.RAW_MATERIALS_IRON, Tags.Items.RAW_MATERIALS_COPPER, Tags.Items.ORES_IRON, Tags.Items.ORES_COPPER,
                Tags.Items.STORAGE_BLOCKS_IRON, Tags.Items.STORAGE_BLOCKS_COPPER, Tags.Items.STORAGE_BLOCKS_RAW_IRON,
                Tags.Items.STORAGE_BLOCKS_RAW_COPPER, Tags.Items.DUSTS_REDSTONE, Tags.Items.STORAGE_BLOCKS_REDSTONE);
        tag(CraftingTags.TRIMMING_TOOLS).add(CraftingItems.stonePebble(), Items.FLINT);
        tag(CraftingTags.HAMMERS).add(CraftingItems.stonePebble());
        tag(CraftingTags.LOOSE_TOOLS).add(CraftingItems.looseItems().toArray(Item[]::new));
        tag(CraftingTags.TIER_ADVANCED).addTags(Tags.Items.ORES, Tags.Items.RAW_MATERIALS, Tags.Items.INGOTS, Tags.Items.NUGGETS,
                Tags.Items.GEMS, Tags.Items.DUSTS, Tags.Items.STORAGE_BLOCKS);
    }
}
