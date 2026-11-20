package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Crafting's item models: flat sprites. */
final class CraftingItemModels extends ItemModelProvider {
    CraftingItemModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, CraftingMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerModels() {
        basicItem(CraftingItems.stonePebble());
    }
}
