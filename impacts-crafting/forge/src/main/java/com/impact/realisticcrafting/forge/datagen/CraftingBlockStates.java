package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.LoosePebbleBlock;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.block.MakeshiftTableBlock;
import com.impact.realisticcrafting.block.TableWear;
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
 * by its variant and turns it a random quarter so neighboring clusters don't match. Each makeshift table has a model
 * per wear stage.
 */
final class CraftingBlockStates extends BlockStateProvider {
    private static final String STONE_TEXTURE_KEY = "stone";
    /** Each cluster's stones as {x, z, width, depth, height} in sixteenths of a block. */
    private static final List<List<int[]>> PEBBLE_CLUSTERS = List.of(
            List.of(new int[]{3, 4, 3, 3, 2}, new int[]{9, 3, 4, 3, 2}, new int[]{6, 9, 3, 4, 1}, new int[]{11, 10, 2, 2, 1}),
            List.of(new int[]{4, 6, 4, 3, 2}, new int[]{10, 8, 3, 3, 1}, new int[]{5, 11, 2, 2, 1}),
            List.of(new int[]{2, 9, 3, 2, 1}, new int[]{6, 5, 3, 3, 2}, new int[]{10, 3, 2, 3, 1}, new int[]{9, 10, 4, 3, 2},
                    new int[]{4, 12, 2, 2, 1}));
    private static final String TABLE_TOP_KEY = "top";
    private static final String TABLE_SIDE_KEY = "side";
    private static final int TABLE_TOP_BOTTOM = 12;
    private static final int TABLE_LEG_WIDTH = 3;
    private static final int[][] TABLE_LEG_CORNERS = {{1, 1}, {12, 1}, {1, 12}, {12, 12}};
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
        for (MakeshiftTable table : MakeshiftTable.values()) {
            List<ModelFile> wearModels = IntStream.rangeClosed(TableWear.NEW, TableWear.FALLING_APART).mapToObj(wear -> tableModel(table, wear)).toList();
            getVariantBuilder(table.block()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(wearModels.get(state.getValue(MakeshiftTableBlock.WEAR))).build());
            simpleBlockItem(table.block(), wearModels.get(TableWear.NEW));
        }
    }

    // A thick top on four legs. The top's faces use the top texture; its edges and the legs use the side texture.
    private ModelFile tableModel(MakeshiftTable table, int wear) {
        String name = table.path + "_" + wear;
        BlockModelBuilder model = models().withExistingParent("block/" + name, mcLoc("block/block"))
                .texture("particle", modLoc("block/" + table.path + "_side_" + wear))
                .texture(TABLE_TOP_KEY, modLoc("block/" + table.path + "_top_" + wear))
                .texture(TABLE_SIDE_KEY, modLoc("block/" + table.path + "_side_" + wear));
        model.element().from(0, TABLE_TOP_BOTTOM, 0).to(16, 16, 16)
                .allFaces((direction, face) -> {
                    boolean isFlatFace = direction.getAxis() == Direction.Axis.Y;
                    face.texture("#" + (isFlatFace ? TABLE_TOP_KEY : TABLE_SIDE_KEY)).uvs(0, 0, 16, isFlatFace ? 16 : 16 - TABLE_TOP_BOTTOM);
                    face.cullface(isFlatFace && direction == Direction.UP ? Direction.UP : (isFlatFace ? null : direction));
                })
                .end();
        for (int[] corner : TABLE_LEG_CORNERS) {
            model.element().from(corner[0], 0, corner[1]).to(corner[0] + TABLE_LEG_WIDTH, TABLE_TOP_BOTTOM, corner[1] + TABLE_LEG_WIDTH)
                    .allFaces((direction, face) -> face.texture("#" + TABLE_SIDE_KEY)
                            .uvs(corner[0], 16 - TABLE_TOP_BOTTOM, corner[0] + TABLE_LEG_WIDTH, 16)
                            .cullface(direction == Direction.DOWN ? Direction.DOWN : null))
                    .end();
        }
        return model;
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
