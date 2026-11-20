package com.impact.realisticvitality.forge.datagen;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.thirst.block.RainBarrelBlock;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Block states and models for the water blocks: plain bottom-and-top cubes. The Rain Barrel shows water in its top once
 * it holds any; the Water Filter and Copper Still show that they work with particles only.
 */
final class VitalityBlockStates extends BlockStateProvider {
    VitalityBlockStates(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, VitalityMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile emptyBarrel = cube("rain_barrel", "rain_barrel_top");
        ModelFile filledBarrel = cube("rain_barrel_filled", "rain_barrel_top_filled");
        getVariantBuilder(VitalityBlocks.rainBarrel()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(RainBarrelBlock.FILL) == 0 ? emptyBarrel : filledBarrel).build());
        simpleBlockItem(VitalityBlocks.rainBarrel(), emptyBarrel);
        ModelFile waterFilter = cube("water_filter", "water_filter_top");
        getVariantBuilder(VitalityBlocks.waterFilter()).forAllStates(state -> ConfiguredModel.builder().modelFile(waterFilter).build());
        simpleBlockItem(VitalityBlocks.waterFilter(), waterFilter);
        ModelFile copperStill = cube("copper_still", "copper_still_top");
        getVariantBuilder(VitalityBlocks.copperStill()).forAllStates(state -> ConfiguredModel.builder().modelFile(copperStill).build());
        simpleBlockItem(VitalityBlocks.copperStill(), copperStill);
    }

    // Each block's side and bottom textures share its first word set, e.g. rain_barrel_side and rain_barrel_bottom.
    private ModelFile cube(String modelName, String topTexture) {
        String blockName = topTexture.substring(0, topTexture.indexOf("_top"));
        return models().cubeBottomTop(modelName, modLoc("block/" + blockName + "_side"), modLoc("block/" + blockName + "_bottom"),
                modLoc("block/" + topTexture));
    }
}
