package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.PackKind;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Item models: each pack is a flat item with a dyed leather layer and an undyed layer for buckles and the frame. */
final class InventoryItemModels extends ItemModelProvider {
    private static final String GENERATED_ITEM_PARENT = "item/generated";
    private static final String OVERLAY_SUFFIX = "_overlay";

    InventoryItemModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, InventoryMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerModels() {
        for (PackKind kind : PackKind.values()) {
            String path = kind.itemPath();
            withExistingParent(path, mcLoc(GENERATED_ITEM_PARENT))
                    .texture("layer0", modLoc(ITEM_FOLDER + "/" + path))
                    .texture("layer1", modLoc(ITEM_FOLDER + "/" + path + OVERLAY_SUFFIX));
        }
    }
}
