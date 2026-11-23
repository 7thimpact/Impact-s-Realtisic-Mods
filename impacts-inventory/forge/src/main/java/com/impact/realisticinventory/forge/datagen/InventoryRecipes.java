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
import com.impact.realisticinventory.pack.PackItem;
import com.impact.realisticinventory.pack.PackKind;
import com.impact.realisticinventory.pack.PackRecipes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.function.Consumer;

/**
 * Inventory's pack recipes: one set without Crafting installed (leather, string, chests, iron, phantom membranes,
 * diamonds) and one with it (Crafting's parts, sewn with its Bone Needle), plus the Netherite Pack's smithing upgrade,
 * the same either way. Every pack recipe also needs {@code packs.enabled}. Each pack past the Satchel is an upgrade that
 * keeps the smaller pack's contents and color, except the Ender Pack, which holds nothing. Crafting's items are named by ID, since Crafting
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
        // Phantom membranes lighten the frame: as roomy, no slowdown.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, pack(PackKind.EXPLORERS_PACK))
                .pattern("M M")
                .pattern("LFL")
                .define('M', Items.PHANTOM_MEMBRANE)
                .define('L', Items.LEATHER)
                .define('F', InventoryItems.framePack())
                .unlockedBy("has_frame_pack", has(InventoryItems.framePack()))
                .save(recipe -> conditionalOutput.accept(new PackUpgradeType(recipe)));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, pack(PackKind.EXPEDITION_PACK))
                .pattern("DGD")
                .pattern("LPL")
                .define('D', Items.DIAMOND)
                .define('G', Items.GOLD_INGOT)
                .define('L', Items.LEATHER)
                .define('P', pack(PackKind.EXPLORERS_PACK))
                .unlockedBy("has_explorers_pack", has(pack(PackKind.EXPLORERS_PACK)))
                .save(recipe -> conditionalOutput.accept(new PackUpgradeType(recipe)));
        // Not an upgrade: the Ender Pack holds nothing, so no pack (and none of its contents) goes into it.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, pack(PackKind.ENDER_PACK))
                .pattern("SLS")
                .pattern("LEL")
                .define('S', Items.SHULKER_SHELL)
                .define('L', Items.LEATHER)
                .define('E', Items.ENDER_CHEST)
                .unlockedBy("has_shulker_shell", has(Items.SHULKER_SHELL))
                .save(conditionalOutput);
        netheriteUpgrade(output);
    }

    // The same with or without Crafting: a smithing table upgrade, as for netherite gear. Smithing keeps the pack's NBT.
    private void netheriteUpgrade(Consumer<FinishedRecipe> output) {
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(pack(PackKind.EXPEDITION_PACK)),
                        Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.TOOLS, pack(PackKind.NETHERITE_PACK))
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(recipe -> output.accept(ConditionalRecipes.withConditions(recipe, PacksEnabledCondition.INSTANCE)),
                        ResourceIds.of(InventoryMod.MOD_ID, "netherite_pack_smithing"));
    }

    private static PackItem pack(PackKind kind) {
        return InventoryItems.pack(kind);
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
        JsonElement framePack = ForeignRecipeBuilder.ingredient(Ingredient.of(InventoryItems.framePack()));
        ForeignRecipeBuilder.of(ToolCrafting.serializer())
                .ingredient(framePack)
                .ingredient(ForeignRecipeBuilder.ingredient(Ingredient.of(Items.PHANTOM_MEMBRANE)), 2)
                .ingredient(LEATHER_PANEL, 2)
                .ingredient(TWINE)
                .tool(BONE_NEEDLE)
                .result(ResourceIds.itemId(pack(PackKind.EXPLORERS_PACK)).toString(), 1)
                .field(COPY_NBT_FROM_FIELD, framePack)
                .save(conditionalOutput, craftingRecipeId("explorers_pack"));
        JsonElement explorersPack = ForeignRecipeBuilder.ingredient(Ingredient.of(pack(PackKind.EXPLORERS_PACK)));
        // Diamonds and gold are advanced-tier materials, so Crafting asks for the Crafting Table.
        ForeignRecipeBuilder.of(ToolCrafting.serializer())
                .ingredient(explorersPack)
                .ingredient(ForeignRecipeBuilder.ingredient(Ingredient.of(Items.DIAMOND)), 2)
                .ingredient(ForeignRecipeBuilder.ingredient(Ingredient.of(Items.GOLD_INGOT)))
                .ingredient(LEATHER_PANEL, 2)
                .ingredient(TWINE)
                .tool(BONE_NEEDLE)
                .result(ResourceIds.itemId(pack(PackKind.EXPEDITION_PACK)).toString(), 1)
                .field(COPY_NBT_FROM_FIELD, explorersPack)
                .save(conditionalOutput, craftingRecipeId("expedition_pack"));
        ForeignRecipeBuilder.of(ToolCrafting.serializer())
                .ingredient(ForeignRecipeBuilder.ingredient(Ingredient.of(Items.ENDER_CHEST)))
                .ingredient(ForeignRecipeBuilder.ingredient(Ingredient.of(Items.SHULKER_SHELL)), 2)
                .ingredient(LEATHER_PANEL, 2)
                .ingredient(TWINE)
                .tool(BONE_NEEDLE)
                .result(ResourceIds.itemId(pack(PackKind.ENDER_PACK)).toString(), 1)
                .save(conditionalOutput, craftingRecipeId("ender_pack"));
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
