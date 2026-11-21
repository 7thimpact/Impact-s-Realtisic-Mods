package com.impact.realisticcrafting.recipe;

import com.google.gson.JsonElement;
import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.wood.WoodData;
import com.impact.lib.wood.WoodKind;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The vanilla recipes Crafting replaces, each with its catalog category. Datagen writes a switched-off copy of each
 * (for the recipe audit and data pack tools), and the recipe manager drops them as recipes load, so they're off even
 * where another pack (Forge's own copies of tool and fence recipes) would win over Crafting's file.
 */
public final class VanillaRecipeReplacements {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final List<WoodKind> WOOD_CATALOG_KINDS = List.of(WoodKind.SLAB, WoodKind.STAIRS, WoodKind.FENCE, WoodKind.FENCE_GATE,
            WoodKind.DOOR, WoodKind.TRAPDOOR, WoodKind.SIGN, WoodKind.BUTTON, WoodKind.PRESSURE_PLATE, WoodKind.BOAT);
    private static final String[] WOOD_CATALOG_RECIPES = {"bowl", "chest", "barrel", "composter", "beehive", "bookshelf", "chiseled_bookshelf",
            "lectern", "loom", "cartography_table", "fletching_table", "painting", "item_frame", "armor_stand", "campfire", "soul_campfire", "ladder",
            "scaffolding", "smithing_table"};
    private static final String[] IRON_CATALOG_RECIPES = {"bucket", "cauldron", "hopper", "iron_bars", "chain", "lantern", "soul_lantern",
            "iron_door", "iron_trapdoor", "heavy_weighted_pressure_plate", "tripwire_hook", "rail", "minecart"};
    private static final String[] STONE_CATALOG_RECIPES = {"stone_bricks", "end_stone_bricks", "bricks", "nether_bricks", "red_nether_bricks",
            "mud_bricks", "flower_pot", "furnace", "smoker", "grindstone", "lever", "white_concrete_powder", "blast_furnace", "stonecutter",
            "glass_pane", "glass_bottle", "smooth_stone_slab"};
    // Every dye and powder comes from a grinder; green and lime dye stay smelted.
    private static final String[] GROUND_RECIPES = {"white_dye", "white_dye_from_lily_of_the_valley", "light_gray_dye_from_azure_bluet",
            "light_gray_dye_from_oxeye_daisy", "light_gray_dye_from_white_tulip", "light_gray_dye_from_black_white_dye", "light_gray_dye_from_gray_white_dye",
            "black_dye", "black_dye_from_wither_rose", "brown_dye", "red_dye_from_poppy", "red_dye_from_tulip", "red_dye_from_beetroot",
            "red_dye_from_rose_bush", "orange_dye_from_orange_tulip", "orange_dye_from_torchflower", "orange_dye_from_red_yellow",
            "yellow_dye_from_dandelion", "yellow_dye_from_sunflower", "blue_dye", "blue_dye_from_cornflower", "light_blue_dye_from_blue_orchid",
            "light_blue_dye_from_blue_white_dye", "magenta_dye_from_allium", "magenta_dye_from_lilac", "magenta_dye_from_purple_and_pink",
            "magenta_dye_from_blue_red_pink", "magenta_dye_from_blue_red_white_dye", "pink_dye_from_pink_tulip", "pink_dye_from_pink_petals",
            "pink_dye_from_peony", "pink_dye_from_red_white_dye", "cyan_dye", "cyan_dye_from_pitcher_plant", "purple_dye", "lime_dye", "gray_dye",
            "bone_meal", "sugar_from_sugar_cane", "blaze_powder"};
    private static final Map<ResourceLocation, OverhaulCategory> REPLACED = buildReplaced();

    private VanillaRecipeReplacements() {
    }

    /** Every replaced vanilla recipe ID, with the category whose switch turns the replacement on. */
    public static Map<ResourceLocation, OverhaulCategory> all() {
        return REPLACED;
    }

    /** Whether the vanilla recipe is replaced right now: its category is on and it isn't in {@code recipes.keepVanilla}. */
    public static boolean isReplaced(ResourceLocation recipeId) {
        OverhaulCategory category = REPLACED.get(recipeId);
        return category != null && category.isOn() && !CraftingCommonConfig.get().keptVanillaRecipes.get().contains(recipeId.toString());
    }

    /** The recipe files to load: the ones read, minus the vanilla recipes Crafting replaces right now. */
    public static Map<ResourceLocation, JsonElement> withoutReplaced(Map<ResourceLocation, JsonElement> recipeFiles) {
        Map<ResourceLocation, JsonElement> kept = new LinkedHashMap<>(recipeFiles);
        int before = kept.size();
        kept.keySet().removeIf(VanillaRecipeReplacements::isReplaced);
        LOG.debug(CraftingLogCategories.AUDIT, "vanilla recipes switched off", "count", before - kept.size(), "listed", REPLACED.size());
        return kept;
    }

    private static Map<ResourceLocation, OverhaulCategory> buildReplaced() {
        Map<ResourceLocation, OverhaulCategory> replaced = new LinkedHashMap<>();
        put(replaced, OverhaulCategory.WOOD, "crafting_table", "stick", "stick_from_bamboo_item");
        WoodData.VANILLA_WOODS.forEach(wood -> put(replaced, OverhaulCategory.WOOD, wood.getPath() + "_planks"));
        for (ToolMaterial material : ToolMaterial.values()) {
            for (ToolKind kind : ToolKind.values()) {
                put(replaced, OverhaulCategory.COMBAT, kind.vanillaPath(material));
            }
        }
        for (ResourceLocation wood : WoodData.VANILLA_WOODS) {
            WOOD_CATALOG_KINDS.forEach(kind -> WoodData.vanillaItem(wood, kind).ifPresent(item -> putItem(replaced, OverhaulCategory.WOOD, item)));
        }
        put(replaced, OverhaulCategory.WOOD, WOOD_CATALOG_RECIPES);
        WoodData.VANILLA_WOODS.forEach(wood -> WoodData.vanillaItem(wood, WoodKind.HANGING_SIGN).ifPresent(item -> putItem(replaced, OverhaulCategory.WOOD, item)));
        put(replaced, OverhaulCategory.IRON, IRON_CATALOG_RECIPES);
        put(replaced, OverhaulCategory.DYES, GROUND_RECIPES);
        put(replaced, OverhaulCategory.DYES, "armor_dye", "shulker_box_coloring");
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName();
            put(replaced, OverhaulCategory.DYES, "dye_" + name + "_wool", "dye_" + name + "_carpet", "dye_" + name + "_bed", name + "_stained_glass",
                    name + "_stained_glass_pane_from_glass_pane", name + "_terracotta", name + "_candle");
            if (color != DyeColor.WHITE) {
                put(replaced, OverhaulCategory.DYES, name + "_concrete_powder");
            }
        }
        put(replaced, OverhaulCategory.TEXTILES, "paper");
        put(replaced, OverhaulCategory.REDSTONE, "redstone_torch", "repeater", "piston", "dropper", "dispenser", "note_block", "compass", "tnt",
                "comparator", "daylight_detector", "observer", "jukebox");
        Arrays.stream(DyeColor.values()).forEach(color -> put(replaced, OverhaulCategory.BEDS, color.getName() + "_bed"));
        put(replaced, OverhaulCategory.COMBAT, "bow", "arrow", "crossbow", "shield", "fishing_rod", "lead", "leather_helmet", "leather_chestplate",
                "leather_leggings", "leather_boots", "leather_horse_armor", "book");
        put(replaced, OverhaulCategory.TEXTILES, "candle", "white_wool_from_string");
        Arrays.stream(DyeColor.values()).forEach(color -> put(replaced, OverhaulCategory.TEXTILES, color.getName() + "_carpet", color.getName() + "_banner"));
        Arrays.stream(DyeColor.values()).forEach(color -> put(replaced, OverhaulCategory.STONE, color.getName() + "_stained_glass_pane"));
        put(replaced, OverhaulCategory.STONE, STONE_CATALOG_RECIPES);
        for (BlockFamily family : StoneFamilies.all()) {
            StoneFamilies.chiseledVariants(family).values().forEach(block -> putItem(replaced, OverhaulCategory.STONE, block.asItem()));
        }
        return Collections.unmodifiableMap(replaced);
    }

    // Vanilla names these recipes after the item they make.
    private static void putItem(Map<ResourceLocation, OverhaulCategory> replaced, OverhaulCategory category, Item item) {
        replaced.put(ResourceIds.itemId(item), category);
    }

    private static void put(Map<ResourceLocation, OverhaulCategory> replaced, OverhaulCategory category, String... recipeNames) {
        for (String recipeName : recipeNames) {
            replaced.put(ResourceIds.of(MINECRAFT_NAMESPACE, recipeName), category);
        }
    }
}
