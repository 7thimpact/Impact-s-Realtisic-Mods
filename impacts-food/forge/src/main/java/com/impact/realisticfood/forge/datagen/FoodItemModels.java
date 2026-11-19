package com.impact.realisticfood.forge.datagen;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.food.StyledTextures;
import com.impact.realisticfood.item.KnifeMaterial;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Item models: knives are held like tools, food is a flat item, and each styled food has two layers, its base food's
 * texture and an overlay sprite the client makes from the style's shared overlay.
 */
final class FoodItemModels extends ItemModelProvider {
    private static final String GENERATED_ITEM_PARENT = "item/generated";
    private static final String HANDHELD_ITEM_PARENT = "item/handheld";
    private static final String SPIT = "spit";

    FoodItemModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, FoodMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerModels() {
        for (KnifeMaterial material : KnifeMaterial.values()) {
            flatItem(material.itemPath, HANDHELD_ITEM_PARENT);
        }
        flatItem(SPIT, HANDHELD_ITEM_PARENT);
        for (FoodPiece piece : FoodPiece.values()) {
            flatItem(piece.path, GENERATED_ITEM_PARENT);
        }
        for (FixedFood fixedFood : FixedFood.values()) {
            flatItem(fixedFood.itemPath, GENERATED_ITEM_PARENT);
        }
        for (FoodBase base : FoodBase.values()) {
            for (FoodStyle style : FoodStyle.values()) {
                if (base.has(style)) {
                    styledItem(base, style);
                }
            }
        }
    }

    private void styledItem(FoodBase base, FoodStyle style) {
        ResourceLocation overlay = StyledTextures.overlaySprite(base, style);
        // The client builds this sprite as the atlas loads; there is no file for the existing-file check to find.
        existingFileHelper.trackGenerated(overlay, TEXTURE);
        withExistingParent(style.itemPath(base), mcLoc(GENERATED_ITEM_PARENT))
                .texture("layer0", StyledTextures.baseTexture(base))
                .texture("layer1", overlay);
    }

    private void flatItem(String path, String parent) {
        withExistingParent(path, mcLoc(parent)).texture("layer0", modLoc(ITEM_FOLDER + "/" + path));
    }
}
