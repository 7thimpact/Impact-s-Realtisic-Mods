package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.lib.wood.WoodData;
import com.impact.lib.wood.WoodKind;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
import com.impact.realisticcrafting.forge.condition.RecipeEnabledCondition;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/**
 * The starting recipes: planks and sticks (replacing vanilla's), the per-wood Trimmed Planks, Trimmed Sticks, Shaped
 * Stone, Stone Hinges, Twine and String Clumps, the Bone Needle and the Flint Chisel. Trimming wood gives Wood Scraps
 * (Crafting's tool-crafting overrides give none for crimson and warped wood).
 */
final class CraftingPartRecipes {
    private static final int TRIMMED_PLANKS_PER_LOG = 2;
    private static final int STICKS_PER_TWO_PLANKS = 4;
    private static final int TRIMMED_STICKS_PER_PLANKS = 2;
    private static final int HINGES_PER_STONE = 2;
    private static final Ingredient TRIMMING_TOOLS = Ingredient.of(CraftingTags.TRIMMING_TOOLS);

    private CraftingPartRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        WoodData.VANILLA_WOODS.forEach(wood -> planksAndTrimmedPlanks(output, wood));
        sticks(output);
        trimmedSticks(output);
        Consumer<FinishedRecipe> partOutput = partRecipe(output);
        trimming(ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.SHAPED_STONE))
                .requires(Ingredient.of(Items.COBBLESTONE, Items.COBBLED_DEEPSLATE)), false)
                .save(partOutput, id(CraftingPart.SHAPED_STONE.path));
        trimming(ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.STONE_HINGE), HINGES_PER_STONE)
                .requires(CraftingItems.part(CraftingPart.SHAPED_STONE)), false)
                .save(partOutput, id(CraftingPart.STONE_HINGE.path));
        trimming(ToolCraftingRecipeBuilder.of(CraftingItems.tool(CraftingTool.BONE_NEEDLE)).requires(Items.BONE), false)
                .save(partOutput, id(CraftingTool.BONE_NEEDLE.path));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CraftingItems.tool(CraftingTool.FLINT_CHISEL))
                .pattern("F")
                .pattern("S")
                .define('F', Items.FLINT)
                .define('S', CraftingItems.part(CraftingPart.TRIMMED_STICK))
                .unlockedBy("has_trimmed_stick", has(CraftingItems.part(CraftingPart.TRIMMED_STICK)))
                .save(partOutput, id(CraftingTool.FLINT_CHISEL.path));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CraftingItems.part(CraftingPart.TWINE))
                .pattern("S")
                .pattern("S")
                .define('S', Items.STRING)
                .unlockedBy("has_string", has(Items.STRING))
                .save(partOutput, id(CraftingPart.TWINE.path));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CraftingItems.part(CraftingPart.STRING_CLUMP))
                .pattern("SS")
                .define('S', Items.STRING)
                .unlockedBy("has_string", has(Items.STRING))
                .save(partOutput, id(CraftingPart.STRING_CLUMP.path));
    }

    // A bare log gives 1 planks by hand, so a new player is never stuck; with a trimming tool it gives 2.
    private static void planksAndTrimmedPlanks(Consumer<FinishedRecipe> output, ResourceLocation wood) {
        Item planks = WoodData.vanillaItem(wood, WoodKind.PLANKS).orElseThrow();
        Consumer<FinishedRecipe> woodOutput = overhaulRecipe(output, OverhaulCategory.WOOD);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, planks)
                .requires(WoodData.logTag(wood))
                .group("planks")
                .unlockedBy("has_logs", has(WoodData.logTag(wood)))
                .save(woodOutput, id(wood.getPath() + "_planks_by_hand"));
        trimming(ToolCraftingRecipeBuilder.of(planks, TRIMMED_PLANKS_PER_LOG).requires(Ingredient.of(WoodData.logTag(wood))), true)
                .save(woodOutput, id(wood.getPath() + "_planks"));
        trimming(ToolCraftingRecipeBuilder.of(WoodData.of(CraftingItems.trimmedPlanks(), wood)).requires(planks), true)
                .save(partRecipe(output), id("trimmed_" + wood.getPath() + "_planks"));
    }

    // Vanilla sticks stay an item but need a trimming tool: vanilla's shape (2 planks stacked), at 1 planks to 2 sticks.
    // A single planks with a trimming tool makes Trimmed Planks instead.
    private static void sticks(Consumer<FinishedRecipe> output) {
        trimming(ToolCraftingRecipeBuilder.of(Items.STICK, STICKS_PER_TWO_PLANKS).pattern("P").pattern("P").define('P', Ingredient.of(ItemTags.PLANKS)), true)
                .save(overhaulRecipe(output, OverhaulCategory.WOOD), id("sticks"));
    }

    private static void trimmedSticks(Consumer<FinishedRecipe> output) {
        trimming(ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.TRIMMED_STICK), TRIMMED_STICKS_PER_PLANKS)
                .requires(CraftingItems.trimmedPlanks()), true)
                .save(partRecipe(output), id(CraftingPart.TRIMMED_STICK.path));
    }

    private static ToolCraftingRecipeBuilder trimming(ToolCraftingRecipeBuilder recipe, boolean givesWoodScraps) {
        recipe.tool(TRIMMING_TOOLS);
        if (givesWoodScraps) {
            recipe.byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F);
        }
        return recipe;
    }

    private static ResourceLocation id(String path) {
        return ResourceIds.of(CraftingMod.MOD_ID, path);
    }

    // Shared parts load unless their ID is in recipes.disabled.
    private static Consumer<FinishedRecipe> partRecipe(Consumer<FinishedRecipe> output) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, new RecipeEnabledCondition(recipe.getId())));
    }

    // Replacements of vanilla recipes also follow their catalog category.
    static Consumer<FinishedRecipe> overhaulRecipe(Consumer<FinishedRecipe> output, OverhaulCategory category) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, new OverhaulEnabledCondition(category, recipe.getId())));
    }

    private static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
    }
}
