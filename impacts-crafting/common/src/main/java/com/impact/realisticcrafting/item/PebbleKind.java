package com.impact.realisticcrafting.item;

import com.impact.lib.ResourceIds;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * The kinds of pebble: one for each common stone, and sandstone and red sandstone for sandy ground. Every kind is a
 * pebble in every way (trimming, hammering, 9 make its own block); the kind follows the ground it's found on.
 */
public enum PebbleKind {
    STONE("stone", () -> Blocks.COBBLESTONE),
    GRANITE("granite", () -> Blocks.GRANITE),
    DIORITE("diorite", () -> Blocks.DIORITE),
    ANDESITE("andesite", () -> Blocks.ANDESITE),
    DEEPSLATE("deepslate", () -> Blocks.COBBLED_DEEPSLATE),
    SANDSTONE("sandstone", () -> Blocks.SANDSTONE),
    RED_SANDSTONE("red_sandstone", () -> Blocks.RED_SANDSTONE);

    /** On plain soil a pebble is usually stone, and now and then one of the colored stones. */
    private static final float SOIL_COLORED_STONE_CHANCE = 0.3F;
    private static final PebbleKind[] SOIL_COLORED_STONES = {GRANITE, DIORITE, ANDESITE};

    public final String stoneName;
    public final String itemPath;
    public final String blockPath;
    private final Supplier<Block> packedBlock;

    PebbleKind(String stoneName, Supplier<Block> packedBlock) {
        this.stoneName = stoneName;
        this.itemPath = stoneName + "_pebble";
        // The stone kind keeps the block ID it had before the other kinds existed.
        this.blockPath = stoneName.equals("stone") ? "loose_pebble" : "loose_" + stoneName + "_pebble";
        this.packedBlock = packedBlock;
    }

    /** The block 9 of these pebbles make. */
    public Block packedBlock() {
        return packedBlock.get();
    }

    /** The path of the recipe that packs 9 into a block: {@code pebbles_to_cobblestone} for stone (its ID from before the other kinds). */
    public String packingRecipePath() {
        return this == STONE ? "pebbles_to_cobblestone" : itemPath + "s_to_" + ResourceIds.blockId(packedBlock()).getPath();
    }

    /** The kind of pebble lying on this ground (soil gets a random stone, mostly plain stone). */
    public static PebbleKind forGround(BlockState ground, RandomSource random) {
        if (ground.is(BlockTags.DIRT) || ground.is(Blocks.GRAVEL)) {
            boolean isColored = random.nextFloat() < SOIL_COLORED_STONE_CHANCE;
            return isColored ? SOIL_COLORED_STONES[random.nextInt(SOIL_COLORED_STONES.length)] : STONE;
        }
        return forBlock(ground);
    }

    /** The kind of pebble a block is made of, or crumbles into. */
    public static PebbleKind forBlock(BlockState block) {
        if (block.is(Blocks.RED_SAND) || block.is(Blocks.RED_SANDSTONE) || block.is(BlockTags.TERRACOTTA)) {
            return RED_SANDSTONE;
        }
        if (block.is(BlockTags.SAND) || block.is(Blocks.SANDSTONE)) {
            return SANDSTONE;
        }
        if (block.is(Blocks.GRANITE)) {
            return GRANITE;
        }
        if (block.is(Blocks.DIORITE) || block.is(Blocks.CALCITE)) {
            return DIORITE;
        }
        if (block.is(Blocks.ANDESITE) || block.is(Blocks.TUFF)) {
            return ANDESITE;
        }
        if (block.is(Blocks.DEEPSLATE) || block.is(Blocks.COBBLED_DEEPSLATE)) {
            return DEEPSLATE;
        }
        return STONE;
    }
}
