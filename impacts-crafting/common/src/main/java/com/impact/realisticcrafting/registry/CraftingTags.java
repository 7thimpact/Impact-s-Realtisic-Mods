package com.impact.realisticcrafting.registry;

import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Crafting's tags. */
public final class CraftingTags {
    /** Blocks loose pebbles generate on, and (minus {@link #PEBBLE_WORLDGEN_ONLY}) that drop pebbles when broken. */
    public static final TagKey<Block> PEBBLE_BASE = block("pebble_base");
    /** Pebble bases that only host worldgen and never drop pebbles: calcite and mossy cobblestone. */
    public static final TagKey<Block> PEBBLE_WORLDGEN_ONLY = block("pebble_worldgen_only");
    /** Blocks that drop a pebble by chance when broken: gravel. */
    public static final TagKey<Block> PEBBLE_GRAVEL = block("pebble_gravel");
    public static final TagKey<Item> HAMMERS = item("hammers");
    /** Every kind of pebble. */
    public static final TagKey<Item> PEBBLES = item("pebbles");
    public static final TagKey<Item> TRIMMING_TOOLS = item("trimming_tools");
    public static final TagKey<Item> LOOSE_TOOLS = item("loose_tools");
    public static final TagKey<Item> TIER_STONE = item("tier/stone");
    public static final TagKey<Item> TIER_ADVANCED = item("tier/advanced");
    public static final TagKey<Item> IRON_PLATES = item("iron_plates");
    public static final TagKey<Item> IRON_RODS = item("iron_rods");
    public static final TagKey<Item> BASE_MATERIALS = item("base_materials");
    public static final TagKey<Item> VAT_DYEABLE = item("vat_dyeable");

    private CraftingTags() {
    }

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, ResourceIds.of(CraftingMod.MOD_ID, path));
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, ResourceIds.of(CraftingMod.MOD_ID, path));
    }
}
