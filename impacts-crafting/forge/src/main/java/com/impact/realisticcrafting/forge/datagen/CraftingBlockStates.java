package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.LoosePebbleBlock;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Block states and models. Loose pebbles have three cluster models, each a few small stones; the block state picks one
 * by its variant and turns it a random quarter so neighboring clusters don't match.
 */
final class CraftingBlockStates extends BlockStateProvider {
    private static final String STONE_TEXTURE_KEY = "stone";
    /** Each cluster's stones as {x, z, width, depth, height} in sixteenths of a block. */
    private static final List<List<int[]>> PEBBLE_CLUSTERS = List.of(
            List.of(new int[]{3, 4, 3, 3, 2}, new int[]{9, 3, 4, 3, 2}, new int[]{6, 9, 3, 4, 1}, new int[]{11, 10, 2, 2, 1}),
            List.of(new int[]{4, 6, 4, 3, 2}, new int[]{10, 8, 3, 3, 1}, new int[]{5, 11, 2, 2, 1}),
            List.of(new int[]{2, 9, 3, 2, 1}, new int[]{6, 5, 3, 3, 2}, new int[]{10, 3, 2, 3, 1}, new int[]{9, 10, 4, 3, 2},
                    new int[]{4, 12, 2, 2, 1}));
    private static final int QUARTER_TURN_DEGREES = 90;
    private static final int QUARTER_TURNS = 4;

    CraftingBlockStates(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, CraftingMod.MOD_ID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation stoneTexture = modLoc("block/loose_pebble");
        List<ModelFile> clusterModels = IntStream.range(0, PEBBLE_CLUSTERS.size())
                .mapToObj(variant -> clusterModel(variant, PEBBLE_CLUSTERS.get(variant), stoneTexture))
                .toList();
        getVariantBuilder(CraftingBlocks.loosePebble()).forAllStatesExcept(state -> quarterTurns(clusterModels.get(state.getValue(LoosePebbleBlock.VARIANT))),
                LoosePebbleBlock.WATERLOGGED);
    }

    private ModelFile clusterModel(int variant, List<int[]> stones, ResourceLocation stoneTexture) {
        BlockModelBuilder model = models().getBuilder("block/loose_pebble_" + variant)
                .texture("particle", stoneTexture)
                .texture(STONE_TEXTURE_KEY, stoneTexture);
        for (int[] stone : stones) {
            addStone(model, stone[0], stone[1], stone[2], stone[3], stone[4]);
        }
        return model;
    }

    private static void addStone(BlockModelBuilder model, int x, int z, int width, int depth, int height) {
        model.element()
                .from(x, 0, z)
                .to(x + width, height, z + depth)
                .allFaces((direction, face) -> face.texture("#" + STONE_TEXTURE_KEY)
                        .uvs(x, z, x + faceWidth(direction, width, depth), z + faceHeight(direction, depth, height))
                        .cullface(direction == Direction.DOWN ? Direction.DOWN : null))
                .end();
    }

    private static int faceWidth(Direction direction, int width, int depth) {
        return direction.getAxis() == Direction.Axis.X ? depth : width;
    }

    private static int faceHeight(Direction direction, int depth, int height) {
        return direction.getAxis() == Direction.Axis.Y ? depth : height;
    }

    private static ConfiguredModel[] quarterTurns(ModelFile model) {
        ConfiguredModel[] turns = new ConfiguredModel[QUARTER_TURNS];
        for (int turn = 0; turn < QUARTER_TURNS; turn++) {
            turns[turn] = new ConfiguredModel(model, 0, turn * QUARTER_TURN_DEGREES, false);
        }
        return turns;
    }
}
