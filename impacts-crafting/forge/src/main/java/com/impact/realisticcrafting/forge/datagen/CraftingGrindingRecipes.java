package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.GrindingRecipeBuilder;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.lib.grinding.GrinderTier;
import com.impact.lib.grinding.Grinding;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.DifferenceIngredient;

import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.has;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.id;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.overhaulRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.partRecipe;

/**
 * Grinding: Lib's four grinder items from Crafting's parts, and every dye, powder and dye mix on Lib's grinding type
 * (wood unless marked stone; doubled outputs match vanilla). Green and lime dye stay smelted. Without Crafting, Food
 * ships simpler grinder recipes.
 */
final class CraftingGrindingRecipes {
    private static final String GRINDING_FOLDER = "grinding/";

    private CraftingGrindingRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        grinders(partRecipe(output));
        Consumer<FinishedRecipe> dyes = overhaulRecipe(output, OverhaulCategory.DYES);
        ground(dyes, Items.BONE_MEAL, 3, GrinderTier.STONE, Items.BONE);
        ground(dyes, Items.WHITE_DYE, 1, GrinderTier.WOOD, Items.BONE_MEAL, Items.LILY_OF_THE_VALLEY);
        ground(dyes, Items.LIGHT_GRAY_DYE, 1, GrinderTier.WOOD, Items.AZURE_BLUET, Items.OXEYE_DAISY, Items.WHITE_TULIP);
        ground(dyes, Items.BLACK_DYE, 1, GrinderTier.WOOD, Items.INK_SAC, Items.WITHER_ROSE);
        ground(dyes, Items.BROWN_DYE, 1, GrinderTier.WOOD, Items.COCOA_BEANS);
        ground(dyes, Items.RED_DYE, 1, GrinderTier.WOOD, Items.POPPY, Items.RED_TULIP, Items.BEETROOT);
        ground(dyes, Items.RED_DYE, 2, GrinderTier.WOOD, Items.ROSE_BUSH);
        ground(dyes, Items.ORANGE_DYE, 1, GrinderTier.WOOD, Items.ORANGE_TULIP, Items.TORCHFLOWER);
        ground(dyes, Items.YELLOW_DYE, 1, GrinderTier.WOOD, Items.DANDELION);
        ground(dyes, Items.YELLOW_DYE, 2, GrinderTier.WOOD, Items.SUNFLOWER);
        ground(dyes, Items.BLUE_DYE, 1, GrinderTier.WOOD, Items.CORNFLOWER);
        ground(dyes, Items.BLUE_DYE, 1, GrinderTier.STONE, Items.LAPIS_LAZULI);
        ground(dyes, Items.LIGHT_BLUE_DYE, 1, GrinderTier.WOOD, Items.BLUE_ORCHID);
        ground(dyes, Items.MAGENTA_DYE, 1, GrinderTier.WOOD, Items.ALLIUM);
        ground(dyes, Items.MAGENTA_DYE, 2, GrinderTier.WOOD, Items.LILAC);
        ground(dyes, Items.PINK_DYE, 1, GrinderTier.WOOD, Items.PINK_TULIP, Items.PINK_PETALS);
        ground(dyes, Items.PINK_DYE, 2, GrinderTier.WOOD, Items.PEONY);
        ground(dyes, Items.CYAN_DYE, 2, GrinderTier.WOOD, Items.PITCHER_PLANT);
        ground(dyes, Items.SUGAR, 1, GrinderTier.WOOD, Items.SUGAR_CANE);
        ground(dyes, Items.BLAZE_POWDER, 2, GrinderTier.STONE, Items.BLAZE_ROD);
        mixed(dyes, Items.ORANGE_DYE, Items.RED_DYE, Items.YELLOW_DYE);
        mixed(dyes, Items.PINK_DYE, Items.RED_DYE, Items.WHITE_DYE);
        mixed(dyes, Items.LIGHT_BLUE_DYE, Items.BLUE_DYE, Items.WHITE_DYE);
        mixed(dyes, Items.CYAN_DYE, Items.BLUE_DYE, Items.GREEN_DYE);
        mixed(dyes, Items.PURPLE_DYE, Items.BLUE_DYE, Items.RED_DYE);
        mixed(dyes, Items.MAGENTA_DYE, Items.PURPLE_DYE, Items.PINK_DYE);
        mixed(dyes, Items.LIME_DYE, Items.GREEN_DYE, Items.WHITE_DYE);
        mixed(dyes, Items.GRAY_DYE, Items.BLACK_DYE, Items.WHITE_DYE);
        mixed(dyes, Items.LIGHT_GRAY_DYE, Items.GRAY_DYE, Items.WHITE_DYE);
    }

    // The Wooden Grinding Stick's trimming tool can't be flint: flint over a Trimmed Stick is the Flint Chisel.
    private static void grinders(Consumer<FinishedRecipe> parts) {
        Item trimmedPlanks = CraftingItems.trimmedPlanks();
        Item shapedStone = CraftingItems.part(CraftingPart.SHAPED_STONE);
        Item trimmedStick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Grinding.woodenBowl()).requires(Items.BOWL).requires(trimmedPlanks)
                .unlockedBy("has_bowl", has(Items.BOWL)).save(parts, id("wooden_grinder_bowl"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Grinding.stoneBowl()).pattern("S S").pattern(" S ").define('S', shapedStone)
                .unlockedBy("has_shaped_stone", has(shapedStone)).save(parts, id("stone_grinder_bowl"));
        Ingredient trimmingToolsButFlint = DifferenceIngredient.of(Ingredient.of(CraftingTags.TRIMMING_TOOLS), Ingredient.of(Items.FLINT));
        ToolCraftingRecipeBuilder.of(Grinding.woodenStick()).requires(trimmedStick).tool(trimmingToolsButFlint)
                .byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F).save(parts, id("wooden_grinding_stick"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Grinding.stoneStick()).pattern("S").pattern("T").define('S', shapedStone).define('T', trimmedStick)
                .unlockedBy("has_shaped_stone", has(shapedStone)).save(parts, id("stone_grinding_stick"));
    }

    private static void ground(Consumer<FinishedRecipe> dyes, Item result, int count, GrinderTier tier, Item... inputs) {
        for (Item input : inputs) {
            GrindingRecipeBuilder.of(result, count).requires(input).minimumTier(tier)
                    .save(dyes, id(GRINDING_FOLDER + path(result) + "_from_" + path(input)));
        }
    }

    private static void mixed(Consumer<FinishedRecipe> dyes, Item result, Item firstDye, Item secondDye) {
        GrindingRecipeBuilder.of(result, 2).requires(firstDye).requires(secondDye).minimumTier(GrinderTier.WOOD)
                .save(dyes, id(GRINDING_FOLDER + path(result) + "_from_" + path(firstDye) + "_and_" + path(secondDye)));
    }

    private static String path(Item item) {
        return ResourceIds.itemId(item).getPath();
    }
}
