package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Arrays;

/** Crafting's item models: flat sprites, held like tools for the grid tools. Per-wood parts share one tinted sprite. */
final class CraftingItemModels extends ItemModelProvider {
    CraftingItemModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, CraftingMod.MOD_ID, existingFiles);
    }

    private static final String HANDHELD_PARENT = "item/handheld";

    @Override
    protected void registerModels() {
        basicItem(CraftingItems.stonePebble());
        basicItem(CraftingItems.trimmedPlanks());
        Arrays.stream(CraftingPart.values()).map(CraftingItems::part).forEach(this::basicItem);
        Arrays.stream(CraftingTool.values()).map(CraftingItems::tool).forEach(this::handheld);
    }

    private void handheld(Item tool) {
        String path = ResourceIds.itemId(tool).getPath();
        withExistingParent(path, HANDHELD_PARENT).texture("layer0", modLoc("item/" + path));
    }
}
