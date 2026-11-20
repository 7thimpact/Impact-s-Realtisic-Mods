package com.impact.realisticvitality.forge.datagen;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Vitality's item models. Water bottles reuse vanilla's potion bottle, with the liquid layer tinted per water; containers use their own textures. */
final class VitalityItemModels extends ItemModelProvider {
    private static final String GENERATED_PARENT = "item/generated";
    private static final String LIQUID_TEXTURE = "minecraft:item/potion_overlay";
    private static final String BOTTLE_TEXTURE = "minecraft:item/potion";

    VitalityItemModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, VitalityMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerModels() {
        for (BottledWater water : BottledWater.values()) {
            withExistingParent(water.itemPath, GENERATED_PARENT)
                    .texture("layer0", LIQUID_TEXTURE)
                    .texture("layer1", BOTTLE_TEXTURE);
        }
        for (WaterContainer container : WaterContainer.values()) {
            basicItem(VitalityItems.container(container));
        }
        basicItem(VitalityItems.charcoalFilter());
        basicItem(VitalityItems.strawHat());
        basicItem(VitalityItems.thermometer());
    }
}
