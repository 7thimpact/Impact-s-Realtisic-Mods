package com.impact.realisticcrafting.worldgen;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.LoosePebbleBlock;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.item.PebbleKind;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;

/**
 * Where loose pebbles generate: on top of a pebble base ({@code #impactscrafting:pebble_base}: stone, soil, sand,
 * gravel, terracotta) with air or still water above, a few tries per chunk on the surface (river beds included) and on
 * cave floors. The pebble's kind follows the ground: sandstone on sand, granite on granite, mostly stone on soil.
 */
public final class LoosePebblePlacer {
    private static final int CHUNK_WIDTH = 16;
    private static final int CAVE_FLOOR_SEARCH_BLOCKS = 24;
    private static final int CAVE_TOP_Y = 50;
    private static final String TAG_PREFIX = "#";
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private LoosePebblePlacer() {
    }

    /** One chunk's surface tries, from the chunk's corner. Returns how many pebbles were placed. */
    public static int placeOnSurface(WorldGenLevel level, BlockPos chunkCorner, RandomSource random) {
        if (!isOn(level)) {
            return 0;
        }
        int placed = 0;
        for (int attempt = 0; attempt < CraftingServerConfig.get().pebbles.surfaceTriesPerChunk.get(); attempt++) {
            int x = chunkCorner.getX() + random.nextInt(CHUNK_WIDTH);
            int z = chunkCorner.getZ() + random.nextInt(CHUNK_WIDTH);
            BlockPos spot = new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), z);
            placed += tryPlace(level, spot, random) ? 1 : 0;
        }
        return placed;
    }

    /** One chunk's cave tries: a random height below Y 50, then down to the first floor. */
    public static int placeInCaves(WorldGenLevel level, BlockPos chunkCorner, RandomSource random) {
        if (!isOn(level)) {
            return 0;
        }
        int placed = 0;
        int bottom = level.getMinBuildHeight() + 1;
        for (int attempt = 0; attempt < CraftingServerConfig.get().pebbles.caveTriesPerChunk.get(); attempt++) {
            int x = chunkCorner.getX() + random.nextInt(CHUNK_WIDTH);
            int z = chunkCorner.getZ() + random.nextInt(CHUNK_WIDTH);
            int startY = bottom + random.nextInt(Math.max(1, CAVE_TOP_Y - bottom));
            placed += findCaveFloor(level, new BlockPos(x, startY, z)).map(spot -> tryPlace(level, spot, random) ? 1 : 0).orElse(0);
        }
        return placed;
    }

    /** Places a pebble cluster at the spot when the block under it is a pebble base and the spot is open (air or water). */
    public static boolean tryPlace(WorldGenLevel level, BlockPos spot, RandomSource random) {
        BlockState below = level.getBlockState(spot.below());
        BlockState here = level.getBlockState(spot);
        boolean isWater = here.getFluidState().is(FluidTags.WATER) && here.getFluidState().isSource();
        boolean isOpen = here.isAir() || isWater && here.is(Blocks.WATER);
        if (!below.is(CraftingTags.PEBBLE_BASE) || !isOpen || isBlacklistedBiome(level.getBiome(spot))) {
            return false;
        }
        PebbleKind kind = PebbleKind.forGround(below, random);
        BlockState pebbles = CraftingBlocks.loosePebble(kind).defaultBlockState()
                .setValue(LoosePebbleBlock.VARIANT, random.nextInt(3))
                .setValue(LoosePebbleBlock.WATERLOGGED, isWater);
        if (!pebbles.canSurvive(level, spot)) {
            return false;
        }
        boolean isPlaced = level.setBlock(spot, pebbles, Block.UPDATE_CLIENTS);
        if (isPlaced && LOG.isVerbose(CraftingLogCategories.PEBBLES)) {
            LOG.verbose(CraftingLogCategories.PEBBLES, "loose pebbles placed", "pos", spot.toShortString(),
                    "on", ResourceIds.blockId(below.getBlock()), "kind", kind.stoneName, "waterlogged", isWater);
        }
        return isPlaced;
    }

    private static Optional<BlockPos> findCaveFloor(WorldGenLevel level, BlockPos start) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int step = 0; step < CAVE_FLOOR_SEARCH_BLOCKS && cursor.getY() > level.getMinBuildHeight() + 1; step++) {
            if (level.getBlockState(cursor).isAir() && !level.getBlockState(cursor.below()).isAir()) {
                return Optional.of(cursor.immutable());
            }
            cursor.move(0, -1, 0);
        }
        return Optional.empty();
    }

    private static boolean isOn(WorldGenLevel level) {
        CraftingServerConfig.PebbleSettings settings = CraftingServerConfig.get().pebbles;
        String dimensionId = level.getLevel().dimension().location().toString();
        return CraftingFeature.PEBBLE_WORLDGEN.isOn() && settings.isWorldgenOn.get() && settings.worldgenDimensions.get().contains(dimensionId);
    }

    private static boolean isBlacklistedBiome(Holder<Biome> biome) {
        return CraftingServerConfig.get().pebbles.worldgenBiomeBlacklist.get().stream().anyMatch(entry -> isBiomeEntry(biome, entry));
    }

    private static boolean isBiomeEntry(Holder<Biome> biome, String entry) {
        if (entry.startsWith(TAG_PREFIX)) {
            return ResourceIds.tryParse(entry.substring(TAG_PREFIX.length()))
                    .map(tagId -> biome.is(TagKey.create(Registries.BIOME, tagId)))
                    .orElse(false);
        }
        return ResourceIds.tryParse(entry).map(biome::is).orElse(false);
    }
}
