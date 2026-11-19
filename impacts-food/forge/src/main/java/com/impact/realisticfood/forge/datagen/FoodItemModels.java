package com.impact.realisticfood.forge.datagen;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.item.KnifeMaterial;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Item models: knives are held like tools, food is a flat item. */
final class FoodItemModels extends ItemModelProvider {
    private static final String GENERATED_ITEM_PARENT = "item/generated";
    private static final String HANDHELD_ITEM_PARENT = "item/handheld";

    FoodItemModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, FoodMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerModels() {
        for (KnifeMaterial material : KnifeMaterial.values()) {
            flatItem(material.itemPath, HANDHELD_ITEM_PARENT);
        }
        for (FoodPiece piece : FoodPiece.values()) {
            flatItem(piece.path, GENERATED_ITEM_PARENT);
        }
    }

    private void flatItem(String path, String parent) {
        withExistingParent(path, mcLoc(parent)).texture("layer0", modLoc(ITEM_FOLDER + "/" + path));
    }
}
