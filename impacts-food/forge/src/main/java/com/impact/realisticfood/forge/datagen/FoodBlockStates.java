package com.impact.realisticfood.forge.datagen;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.bucket.CampfireBucketBlock;
import com.impact.realisticfood.bucket.CampfireBuckets;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Block states: the campfire bucket, with water or empty. Its models are hand-written, since they reach into the campfire below. */
final class FoodBlockStates extends BlockStateProvider {
    private static final String CAMPFIRE_BUCKET_MODEL = "block/campfire_bucket";
    private static final String EMPTY_SUFFIX = "_empty";

    FoodBlockStates(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, FoodMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        getVariantBuilder(CampfireBuckets.block()).forAllStates(state -> {
            String model = state.getValue(CampfireBucketBlock.HAS_WATER) ? CAMPFIRE_BUCKET_MODEL : CAMPFIRE_BUCKET_MODEL + EMPTY_SUFFIX;
            return ConfiguredModel.builder().modelFile(models().getExistingFile(modLoc(model))).build();
        });
    }
}
