package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.InventoryTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Inventory's item tags: {@code #impactsinventory:packs}. */
final class InventoryItemTags extends ItemTagsProvider {
    InventoryItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), InventoryMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(InventoryTags.PACKS).add(InventoryItems.packs().toArray(Item[]::new));
    }
}
