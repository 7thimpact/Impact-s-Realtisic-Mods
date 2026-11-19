package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.PlacedPacks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * The placed pack's block state. Its block entity draws the pack (in its dye color), so the block model only names the
 * texture for breaking particles.
 */
final class InventoryBlockStates extends BlockStateProvider {
    private static final String PARTICLE_TEXTURE = "block/brown_wool";

    InventoryBlockStates(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, InventoryMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile particlesOnly = models().getBuilder("placed_pack").texture("particle", mcLoc(PARTICLE_TEXTURE));
        getVariantBuilder(PlacedPacks.block()).forAllStates(state -> ConfiguredModel.builder().modelFile(particlesOnly).build());
    }
}
