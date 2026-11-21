package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.boneNeedles;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.has;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.id;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.overhaulRecipe;

/**
 * Bed parts: the Bed Frame (a Wooden Frame with 3 Trimmed Planks as slats), the Comforter and the 16 Sheets, sewn with
 * the Bone Needle. White wool makes either a comforter or a white sheet, so they differ in shape: a sheet is 3 wool in a
 * row over its Twine hem, a comforter is wool, Twine, wool over a third wool.
 */
final class CraftingBedRecipes {
    private CraftingBedRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> beds = overhaulRecipe(output, OverhaulCategory.BEDS);
        Item frame = CraftingItems.woodPart(WoodPart.WOODEN_FRAME);
        Item twine = CraftingItems.part(CraftingPart.TWINE);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, CraftingItems.bedFrame()).requires(frame).requires(CraftingItems.trimmedPlanks(), 3)
                .unlockedBy("has_frame", has(frame)).save(beds, id("bed_frame"));
        ToolCraftingRecipeBuilder.of(CraftingItems.comforter()).pattern("WTW").pattern(" W ").define('W', Items.WHITE_WOOL).define('T', twine)
                .tool(boneNeedles()).save(beds, id("comforter"));
        for (DyeColor color : DyeColor.values()) {
            Item wool = ResourceIds.item(ResourceIds.of("minecraft", color.getName() + "_wool")).orElseThrow();
            ToolCraftingRecipeBuilder.of(CraftingItems.sheet(color)).pattern("WWW").pattern(" T ").define('W', wool).define('T', twine)
                    .tool(boneNeedles()).save(beds, id(color.getName() + "_sheet"));
        }
    }
}
