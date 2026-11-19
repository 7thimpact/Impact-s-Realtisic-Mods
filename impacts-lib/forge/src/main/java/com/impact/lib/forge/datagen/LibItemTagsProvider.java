package com.impact.lib.forge.datagen;

import com.impact.lib.ImpactsLib;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.forge.crafting.WaterBottleIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Writes Lib's item tags. They start empty; the mods that use them add their own entries. */
final class LibItemTagsProvider extends ItemTagsProvider {
    LibItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), ImpactsLib.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(BackSlot.BACK_SLOT_ITEMS);
        tag(WaterBottleIngredient.WATER_BOTTLES);
    }
}
