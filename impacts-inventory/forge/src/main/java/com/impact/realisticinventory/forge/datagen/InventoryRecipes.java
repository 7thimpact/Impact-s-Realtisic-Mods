package com.impact.realisticinventory.forge.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.ToolCrafting;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.lib.forge.datagen.ForeignRecipeBuilder;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.forge.condition.PacksEnabledCondition;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackRecipes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.function.Consumer;

/**
 * Inventory's pack recipes: one set without Crafting installed (leather, string, chests, iron) and one with it
 * (Crafting's parts, sewn with its Bone Needle). Every pack recipe also needs {@code packs.enabled}. The two bigger
 * packs are upgrades that keep the smaller pack's contents and color. Crafting's items are named by ID, since Crafting
 * isn't loaded while Inventory generates its data.
 */
final class InventoryRecipes extends RecipeProvider {
    private static final String CRAFTING_MOD_ID = "impactscrafting";
    private static final String COPY_NBT_FROM_FIELD = "copy_nbt_from";
    private static final String WITH_CRAFTING_SUFFIX = "_with_" + CRAFTING_MOD_ID;
    private static final JsonElement LEATHER_PANEL = ForeignRecipeBuilder.item("impactscrafting:leather_panel");
    private static final JsonElement TWINE = ForeignRecipeBuilder.item("impactscrafting:twine");
    private static final JsonElement BONE_NEEDLE = ForeignRecipeBuilder.item("impactscrafting:bone_needle");
    private static final JsonElement CHEST_TOP = ForeignRecipeBuilder.item("impactscrafting:chest_top");
    private static final JsonElement WOODEN_FRAME = ForeignRecipeBuilder.item("impactscrafting:wooden_frame");
    private static final JsonElement IRON_RIVETS = ForeignRecipeBuilder.item("impactscrafting:iron_rivets");
    private static final JsonElement IRON_RODS = ForeignRecipeBuilder.tag("impactscrafting:iron_rods");

    InventoryRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        buildRecipesAlone(output);
        buildRecipesWithCrafting(output);
    }

    private void buildRecipesAlone(Consumer<FinishedRecipe> output) {
        ICondition[] aloneConditions = {PacksEnabledCondition.INSTANCE, ConditionalRecipes.modNotLoaded(CRAFTING_MOD_ID)};
        Consumer<FinishedRecipe> conditionalOutput = recipe -> output.accept(ConditionalRecipes.withConditions(recipe, aloneConditions));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, InventoryItems.satchel())
                .pattern("S S")
                .pattern("LLL")
                .define('S', Items.STRING)
                .define('L', Items.LEATHER)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(conditionalOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, InventoryItems.backpack())
                .pattern("LCL")
                .pattern("LPL")
                .define('L', Items.LEATHER)
                .define('C', Items.CHEST)
                .define('P', InventoryItems.satchel())
                .unlockedBy("has_satchel", has(InventoryItems.satchel()))
                .save(recipe -> conditionalOutput.accept(new PackUpgradeType(recipe)));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, InventoryItems.framePack())
                .pattern("I I")
                .pattern("SPS")
                .pattern("I I")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .define('P', InventoryItems.backpack())
                .unlockedBy("has_backpack", has(InventoryItems.backpack()))
                .save(recipe -> conditionalOutput.accept(new PackUpgradeType(recipe)));
    }

    // The Frame Pack's Iron Rods and Iron Rivets are stone-tier parts, so Crafting asks for the Makeshift Stone Table on its own.
    private void buildRecipesWithCrafting(Consumer<FinishedRecipe> output) {
        ICondition[] craftingConditions = {PacksEnabledCondition.INSTANCE, ConditionalRecipes.modLoaded(CRAFTING_MOD_ID)};
        Consumer<FinishedRecipe> conditionalOutput = recipe -> output.accept(ConditionalRecipes.withConditions(recipe, craftingConditions));
        JsonElement satchel = ForeignRecipeBuilder.ingredient(Ingredient.of(InventoryItems.satchel()));
        JsonElement backpack = ForeignRecipeBuilder.ingredient(Ingredient.of(InventoryItems.backpack()));
        // The satchel's second Twine is its strap; without it the recipe would be Crafting's leather boots.
        ForeignRecipeBuilder.of(ToolCrafting.serializer())
                .ingredient(LEATHER_PANEL, 2)
                .ingredient(TWINE, 2)
                .tool(BONE_NEEDLE)
                .result(ResourceIds.itemId(InventoryItems.satchel()).toString(), 1)
                .save(conditionalOutput, craftingRecipeId("satchel"));
        ForeignRecipeBuilder.of(ToolCrafting.serializer())
                .ingredient(satchel)
                .ingredient(LEATHER_PANEL, 2)
                .ingredient(CHEST_TOP)
                .ingredient(TWINE)
                .tool(BONE_NEEDLE)
                .result(ResourceIds.itemId(InventoryItems.backpack()).toString(), 1)
                .field(COPY_NBT_FROM_FIELD, satchel)
                .save(conditionalOutput, craftingRecipeId("backpack"));
        ForeignRecipeBuilder.of(ToolCrafting.serializer())
                .ingredient(backpack)
                .ingredient(WOODEN_FRAME)
                .ingredient(IRON_RODS, 2)
                .ingredient(IRON_RIVETS)
                .result(ResourceIds.itemId(InventoryItems.framePack()).toString(), 1)
                .field(COPY_NBT_FROM_FIELD, backpack)
                .save(conditionalOutput, craftingRecipeId("frame_pack"));
    }

    // Named like ConditionalRecipes.pair's recipes: satchel_with_impactscrafting.
    private static ResourceLocation craftingRecipeId(String packName) {
        return ResourceIds.of(InventoryMod.MOD_ID, packName + WITH_CRAFTING_SUFFIX);
    }

    /** A shaped recipe written as Inventory's pack upgrade, which copies the old pack's NBT into the new one. */
    private record PackUpgradeType(FinishedRecipe shapedRecipe) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            shapedRecipe.serializeRecipeData(json);
        }

        @Override
        public ResourceLocation getId() {
            return shapedRecipe.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return PackRecipes.packUpgradeSerializer();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return shapedRecipe.serializeAdvancement();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return shapedRecipe.getAdvancementId();
        }
    }
}
