package com.impact.realisticcrafting.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The non-metal vanilla crafting recipes Crafting keeps on purpose, with the reason, and what counts as food (Impact's
 * Realistic Food's). Metal recipes, special recipes and storage packing are recognized by the audit on its own.
 */
final class KeptVanillaRecipes {
    private static final String EARLY_LIGHT = "early lighting stays easy";
    private static final String COPPER_VARIANTS = "copper block variants (cutting, waxing)";
    private static final Map<String, String> REASONS = buildReasons();

    private KeptVanillaRecipes() {
    }

    static Optional<String> reasonFor(ResourceLocation recipeId) {
        return recipeId.getNamespace().equals("minecraft") ? Optional.ofNullable(REASONS.get(recipeId.getPath())) : Optional.empty();
    }

    /** Food belongs to Impact's Realistic Food: anything edible, plus cake and the seeds cut from melons and pumpkins. */
    static boolean isFood(ItemStack result) {
        return result.isEdible() || result.is(Items.CAKE) || result.is(Items.MELON_SEEDS) || result.is(Items.PUMPKIN_SEEDS);
    }

    private static Map<String, String> buildReasons() {
        Map<String, String> reasons = new LinkedHashMap<>();
        put(reasons, EARLY_LIGHT, "torch", "soul_torch", "jack_o_lantern");
        put(reasons, "built on a finished item", "sticky_piston", "carrot_on_a_stick", "warped_fungus_on_a_stick", "trapped_chest", "glow_item_frame",
                "writable_book", "map", "recovery_compass", "chest_minecart", "furnace_minecart", "hopper_minecart", "tnt_minecart");
        put(reasons, "a chest on a boat", "oak_chest_boat", "spruce_chest_boat", "birch_chest_boat", "jungle_chest_boat", "acacia_chest_boat",
                "dark_oak_chest_boat", "mangrove_chest_boat", "cherry_chest_boat", "bamboo_chest_raft");
        put(reasons, "bamboo mosaic family", "bamboo_mosaic", "bamboo_mosaic_slab", "bamboo_mosaic_stairs");
        put(reasons, "natural block mixing", "andesite", "coarse_dirt", "packed_mud", "muddy_mangrove_roots", "moss_carpet", "snow",
                "mossy_cobblestone_from_moss_block", "mossy_cobblestone_from_vine", "mossy_stone_bricks_from_moss_block", "mossy_stone_bricks_from_vine");
        put(reasons, "banner patterns stay at the loom", "creeper_banner_pattern", "flower_banner_pattern", "mojang_banner_pattern",
                "skull_banner_pattern");
        put(reasons, "brewing ingredient", "fermented_spider_eye");
        put(reasons, "decoration", "decorated_pot_simple", "nether_brick_fence", "purpur_pillar", "turtle_helmet", "firework_rocket_simple");
        put(reasons, "music disc from fragments", "music_disc_5");
        for (String weathering : List.of("", "exposed_", "weathered_", "oxidized_")) {
            put(reasons, COPPER_VARIANTS, weathering + "cut_copper_slab", weathering + "cut_copper_stairs", "waxed_" + weathering + "cut_copper_slab",
                    "waxed_" + weathering + "cut_copper_stairs", "waxed_" + weathering + "cut_copper_from_honeycomb",
                    "waxed_" + weathering + "cut_copper_slab_from_honeycomb", "waxed_" + weathering + "cut_copper_stairs_from_honeycomb");
            if (!weathering.isEmpty()) {
                put(reasons, COPPER_VARIANTS, "waxed_" + weathering + "copper_from_honeycomb");
            }
        }
        return reasons;
    }

    private static void put(Map<String, String> reasons, String reason, String... recipePaths) {
        for (String recipePath : recipePaths) {
            reasons.put(recipePath, reason);
        }
    }
}
