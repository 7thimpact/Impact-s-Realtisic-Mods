package com.impact.realisticcrafting.recipe;

import com.impact.lib.ResourceIds;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The stone catalog's block families, from vanilla's own list: every stone, brick and sandstone family, without wood,
 * bamboo and copper. Each family's slabs, stairs, walls, polished, cut and chiseled blocks, buttons and pressure plates
 * are cut with a chisel.
 */
public final class StoneFamilies {
    /** The variants Crafting cuts with a chisel; anything else in a family (fences, cracked blocks, signs) stays vanilla. */
    public static final Set<BlockFamily.Variant> CHISELED_VARIANTS = Set.of(BlockFamily.Variant.SLAB, BlockFamily.Variant.STAIRS,
            BlockFamily.Variant.WALL, BlockFamily.Variant.POLISHED, BlockFamily.Variant.CUT, BlockFamily.Variant.CHISELED,
            BlockFamily.Variant.BUTTON, BlockFamily.Variant.PRESSURE_PLATE);
    private static final List<String> SKIPPED_NAME_PARTS = List.of("copper", "bamboo");

    private StoneFamilies() {
    }

    public static List<BlockFamily> all() {
        return BlockFamilies.getAllFamilies()
                .filter(family -> family.getRecipeGroupPrefix().isEmpty())
                .filter(family -> SKIPPED_NAME_PARTS.stream().noneMatch(ResourceIds.blockId(family.getBaseBlock()).getPath()::contains))
                .toList();
    }

    /** The family's variants Crafting cuts, each with its block. */
    public static Map<BlockFamily.Variant, Block> chiseledVariants(BlockFamily family) {
        return family.getVariants().entrySet().stream()
                .filter(entry -> CHISELED_VARIANTS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** The slab of a family, which its chiseled block is cut from. */
    public static Optional<Block> slabOf(BlockFamily family) {
        return Optional.ofNullable(family.get(BlockFamily.Variant.SLAB));
    }
}
