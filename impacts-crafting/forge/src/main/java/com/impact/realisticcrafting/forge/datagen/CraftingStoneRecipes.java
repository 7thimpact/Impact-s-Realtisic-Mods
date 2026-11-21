package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.recipe.StoneFamilies;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.chisels;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.has;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.id;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.overhaulRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.partRecipe;

/**
 * The stone branch: pebbles to cobblestone, the Stone Casing, Stone Base and Mortar, every stone family's chiseled
 * variants (vanilla's shapes plus a chisel: slabs 6, stairs 6, walls 6; one block to one polished or cut block; 2
 * slabs to a chiseled block; 2 diagonal blocks to 4 buttons, 2 side by side to a pressure plate), brick blocks with
 * Mortar, and the non-iron stone machines.
 */
final class CraftingStoneRecipes {
    private static final int SLABS = 6;
    private static final int STAIRS = 6;
    private static final int WALLS = 6;
    private static final int BUTTONS = 4;

    private CraftingStoneRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> parts = partRecipe(output);
        Consumer<FinishedRecipe> stone = overhaulRecipe(output, OverhaulCategory.STONE);
        parts(parts);
        StoneFamilies.all().forEach(family -> family(stone, family));
        chiseledBlock(stone, Items.STONE, Items.STONE_BRICKS);
        chiseledBlock(stone, Items.END_STONE, Items.END_STONE_BRICKS);
        // Smooth stone isn't one of vanilla's block families, but its slab is cut like theirs.
        ToolCraftingRecipeBuilder.of(Items.SMOOTH_STONE_SLAB, SLABS).pattern("XXX").define('X', Items.SMOOTH_STONE).tool(chisels())
                .save(stone, id("smooth_stone_slab"));
        catalog(stone);
    }

    private static void parts(Consumer<FinishedRecipe> parts) {
        Item pebble = CraftingItems.stonePebble();
        Item shapedStone = CraftingItems.part(CraftingPart.SHAPED_STONE);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Items.COBBLESTONE).pattern("PPP").pattern("PPP").pattern("PPP").define('P', pebble)
                .unlockedBy("has_pebble", has(pebble)).save(parts, id("pebbles_to_cobblestone"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CraftingItems.part(CraftingPart.STONE_CASING)).pattern("SSS").pattern("S S").pattern("SSS")
                .define('S', shapedStone).unlockedBy("has_shaped_stone", has(shapedStone)).save(parts, id("stone_casing"));
        ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.STONE_BASE), 2).requires(Items.SMOOTH_STONE_SLAB).tool(chisels())
                .save(parts, id("stone_base"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CraftingItems.part(CraftingPart.MORTAR), 4).requires(Items.CLAY_BALL)
                .requires(ItemTags.SAND).requires(Items.GRAVEL).unlockedBy("has_clay", has(Items.CLAY_BALL)).save(parts, id("mortar"));
    }

    private static void family(Consumer<FinishedRecipe> stone, BlockFamily family) {
        Item base = family.getBaseBlock().asItem();
        for (Map.Entry<BlockFamily.Variant, Block> variant : StoneFamilies.chiseledVariants(family).entrySet()) {
            Item result = variant.getValue().asItem();
            String path = ResourceIds.itemId(result).getPath();
            ToolCraftingRecipeBuilder recipe = switch (variant.getKey()) {
                case SLAB -> ToolCraftingRecipeBuilder.of(result, SLABS).pattern("XXX").define('X', base);
                case STAIRS -> ToolCraftingRecipeBuilder.of(result, STAIRS).pattern("X  ").pattern("XX ").pattern("XXX").define('X', base);
                case WALL -> ToolCraftingRecipeBuilder.of(result, WALLS).pattern("XXX").pattern("XXX").define('X', base);
                case BUTTON -> ToolCraftingRecipeBuilder.of(result, BUTTONS).pattern("X ").pattern(" X").define('X', base);
                case PRESSURE_PLATE -> ToolCraftingRecipeBuilder.of(result, 1).pattern("XX").define('X', base);
                case CHISELED -> StoneFamilies.slabOf(family)
                        .map(slab -> ToolCraftingRecipeBuilder.of(result, 1).pattern("X").pattern("X").define('X', slab.asItem()))
                        .orElse(ToolCraftingRecipeBuilder.of(result, 1).requires(base));
                default -> ToolCraftingRecipeBuilder.of(result, 1).requires(base);
            };
            recipe.tool(chisels()).save(stone, id(path));
        }
    }

    private static void chiseledBlock(Consumer<FinishedRecipe> stone, Item block, Item result) {
        ToolCraftingRecipeBuilder.of(result, 1).requires(block).tool(chisels()).save(stone, id(ResourceIds.itemId(result).getPath()));
    }

    private static void catalog(Consumer<FinishedRecipe> stone) {
        Item mortar = CraftingItems.part(CraftingPart.MORTAR);
        Item stoneHinge = CraftingItems.part(CraftingPart.STONE_HINGE);
        Item trimmedStick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        shapeless(Items.BRICKS, 1).requires(Items.BRICK, 4).requires(mortar).unlockedBy("has_mortar", has(mortar)).save(stone, id("bricks"));
        shapeless(Items.NETHER_BRICKS, 1).requires(Items.NETHER_BRICK, 4).requires(mortar).unlockedBy("has_mortar", has(mortar)).save(stone, id("nether_bricks"));
        shapeless(Items.RED_NETHER_BRICKS, 1).requires(Items.NETHER_BRICK, 2).requires(Items.NETHER_WART, 2).requires(mortar)
                .unlockedBy("has_mortar", has(mortar)).save(stone, id("red_nether_bricks"));
        shapeless(Items.MUD_BRICKS, 4).requires(Items.PACKED_MUD, 4).requires(mortar).unlockedBy("has_mortar", has(mortar)).save(stone, id("mud_bricks"));
        shapeless(Items.FLOWER_POT, 1).requires(Items.BRICK, 3).requires(mortar).unlockedBy("has_mortar", has(mortar)).save(stone, id("flower_pot"));
        shapeless(Items.WHITE_CONCRETE_POWDER, 8).requires(Items.SAND, 4).requires(Items.GRAVEL, 4).requires(mortar)
                .unlockedBy("has_mortar", has(mortar)).save(stone, id("white_concrete_powder"));
        shapeless(Items.FURNACE, 1).requires(CraftingItems.part(CraftingPart.STONE_CASING)).requires(stoneHinge)
                .unlockedBy("has_stone_casing", has(CraftingItems.part(CraftingPart.STONE_CASING))).save(stone, id("furnace"));
        shapeless(Items.SMOKER, 1).requires(Items.FURNACE).requires(CraftingItems.woodPart(WoodPart.WOODEN_PANEL), 2)
                .unlockedBy("has_furnace", has(Items.FURNACE)).save(stone, id("smoker"));
        shapeless(Items.GRINDSTONE, 1).requires(CraftingItems.part(CraftingPart.STONE_BASE)).requires(trimmedStick, 2)
                .requires(CraftingItems.trimmedPlanks(), 2).unlockedBy("has_stone_base", has(CraftingItems.part(CraftingPart.STONE_BASE)))
                .save(stone, id("grindstone"));
        shapeless(Items.LEVER, 1).requires(stoneHinge).requires(trimmedStick).unlockedBy("has_stone_hinge", has(stoneHinge)).save(stone, id("lever"));
    }

    private static ShapelessRecipeBuilder shapeless(Item result, int count) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, count);
    }
}
