package com.impact.realisticvitality.forge.datagen;

import com.google.gson.JsonElement;
import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.ToolCrafting;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.lib.forge.datagen.ForeignRecipeBuilder;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.config.VitalityFeatures;
import com.impact.realisticvitality.config.VitalityGear;
import com.impact.realisticvitality.forge.condition.FeatureEnabledCondition;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityRecipeSerializers;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.function.Consumer;

/**
 * Vitality's gear made from Impact's Realistic Crafting's parts, loaded only with Crafting (each replaces the version
 * made from vanilla materials). Crafting's items are written by ID, since Crafting isn't loaded while Vitality generates
 * its data. Sewn gear is Lib's tool crafting with the Bone Needle; iron and copper parts put a recipe on the Makeshift
 * Stone Table by themselves, and gold and diamonds on the Crafting Table. Also the loose Air Tank's hammering and the
 * needle-sewn linings.
 */
final class VitalityCraftingRecipes {
    private static final String WITH_CRAFTING_SUFFIX = "_with_" + VitalityMod.CRAFTING_MOD_ID;
    private static final JsonElement TRIMMED_STICK = crafting("trimmed_stick");
    private static final JsonElement LEATHER_PANEL = crafting("leather_panel");
    private static final JsonElement LEATHER_STRIP = crafting("leather_strip");
    private static final JsonElement TWINE = crafting("twine");
    private static final JsonElement BONE_NEEDLE = crafting("bone_needle");
    private static final JsonElement WOODEN_BUCKET = crafting("wooden_bucket");
    private static final JsonElement WOODEN_PANEL = crafting("wooden_panel");
    private static final JsonElement IRON_RIVETS = crafting("iron_rivets");
    private static final JsonElement IRON_HINGE = crafting("iron_hinge");
    private static final JsonElement REDSTONE_MECHANISM = crafting("redstone_mechanism");
    private static final JsonElement STONE_CASING = crafting("stone_casing");
    private static final JsonElement IRON_PLATES = ForeignRecipeBuilder.tag("impactscrafting:iron_plates");
    private static final JsonElement HAMMERS = ForeignRecipeBuilder.tag("impactscrafting:hammers");
    private static final String HAMMERING_TYPE = "impactscrafting:hammering";

    private VitalityCraftingRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        waterGear(output);
        breathingGear(output);
        clothing(output);
        SpecialRecipeBuilder.special(VitalityRecipeSerializers.armorLining()).save(conditional(output,
                FeatureEnabledCondition.of(VitalityFeatures.LININGS), ConditionalRecipes.modLoaded(VitalityMod.CRAFTING_MOD_ID)),
                VitalityMod.MOD_ID + ":armor_lining");
    }

    private static void waterGear(Consumer<FinishedRecipe> output) {
        shapeless(container(WaterContainer.WOODEN_CUP))
                .ingredient(itemIngredient(Items.BOWL))
                .ingredient(TRIMMED_STICK)
                .save(withCrafting(output, VitalityGear.WOODEN_CUP), recipeId(VitalityGear.WOODEN_CUP));
        // The Leather Strip is the neck; without it the recipe would be Crafting's leather boots.
        sewn(container(WaterContainer.WATERSKIN))
                .ingredient(LEATHER_PANEL, 2)
                .ingredient(TWINE)
                .ingredient(LEATHER_STRIP)
                .save(withCrafting(output, VitalityGear.WATERSKIN), recipeId(VitalityGear.WATERSKIN));
        shaped(container(WaterContainer.CANTEEN))
                .pattern(" S ")
                .pattern("PRP")
                .pattern(" P ")
                .key('S', LEATHER_STRIP)
                .key('P', IRON_PLATES)
                .key('R', IRON_RIVETS)
                .save(withCrafting(output, VitalityGear.CANTEEN), recipeId(VitalityGear.CANTEEN));
        sewn(container(WaterContainer.INSULATED_FLASK))
                .ingredient(itemIngredient(container(WaterContainer.CANTEEN)))
                .ingredient(itemIngredient(Items.GOLD_INGOT), 2)
                .ingredient(ForeignRecipeBuilder.ingredient(Ingredient.of(ItemTags.WOOL)), 2)
                .ingredient(TWINE)
                .save(withCrafting(output, VitalityGear.INSULATED_FLASK), recipeId(VitalityGear.INSULATED_FLASK));
        sewn(container(WaterContainer.HYDRATION_PACK))
                .ingredient(itemIngredient(container(WaterContainer.CANTEEN)))
                .ingredient(LEATHER_PANEL, 4)
                .ingredient(itemIngredient(Items.DIAMOND), 2)
                .ingredient(TWINE)
                .save(withCrafting(output, VitalityGear.HYDRATION_PACK), recipeId(VitalityGear.HYDRATION_PACK));
        // The funnel of Wooden Panels over the bucket.
        shaped(VitalityBlocks.rainBarrel())
                .pattern("P P")
                .pattern(" B ")
                .key('P', WOODEN_PANEL)
                .key('B', WOODEN_BUCKET)
                .save(withCrafting(output, VitalityGear.RAIN_BARREL), recipeId(VitalityGear.RAIN_BARREL));
        shaped(VitalityBlocks.waterFilter())
                .pattern("F")
                .pattern("B")
                .pattern("I")
                .key('F', itemIngredient(VitalityItems.charcoalFilter()))
                .key('B', WOODEN_BUCKET)
                .key('I', itemIngredient(Items.IRON_BARS))
                .save(withCrafting(output, VitalityGear.WATER_FILTER), recipeId(VitalityGear.WATER_FILTER));
    }

    private static void breathingGear(Consumer<FinishedRecipe> output) {
        shapeless(VitalityItems.charcoalFilter())
                .ingredient(itemIngredient(Items.CHARCOAL))
                .ingredient(ForeignRecipeBuilder.ingredient(Ingredient.of(ItemTags.WOOL)))
                .ingredient(LEATHER_STRIP)
                .save(withCrafting(output, VitalityGear.CHARCOAL_FILTER), recipeId(VitalityGear.CHARCOAL_FILTER));
        ForeignRecipeBuilder.of(ToolCrafting.serializer())
                .pattern("WTW")
                .key('W', ForeignRecipeBuilder.ingredient(Ingredient.of(ItemTags.WOOL)))
                .key('T', TWINE)
                .tool(BONE_NEEDLE)
                .result(itemId(VitalityItems.mask(BreathingMask.CLOTH_MASK)), 1)
                .save(withCrafting(output, VitalityGear.CLOTH_MASK), recipeId(VitalityGear.CLOTH_MASK));
        shapeless(VitalityItems.airBladder())
                .ingredient(LEATHER_PANEL)
                .ingredient(TWINE)
                .save(withCrafting(output, VitalityGear.AIR_BLADDER), recipeId(VitalityGear.AIR_BLADDER));
        sewn(VitalityItems.mask(BreathingMask.RESPIRATOR))
                .ingredient(LEATHER_PANEL)
                .ingredient(itemIngredient(Items.GLASS_PANE))
                .ingredient(TWINE)
                .save(withCrafting(output, VitalityGear.RESPIRATOR), recipeId(VitalityGear.RESPIRATOR));
        airTank(output);
        shaped(VitalityItems.mask(BreathingMask.REBREATHER))
                .pattern("GRG")
                .pattern(" B ")
                .pattern(" V ")
                .key('G', itemIngredient(Items.GOLD_INGOT))
                .key('R', itemIngredient(VitalityItems.mask(BreathingMask.RESPIRATOR)))
                .key('B', itemIngredient(VitalityItems.airBladder()))
                .key('V', IRON_RIVETS)
                .save(withCrafting(output, VitalityGear.REBREATHER), recipeId(VitalityGear.REBREATHER));
        shaped(VitalityBlocks.airCompressor())
                .pattern("CMC")
                .pattern("CSC")
                .key('C', itemIngredient(Items.COPPER_INGOT))
                .key('M', REDSTONE_MECHANISM)
                .key('S', STONE_CASING)
                .save(withCrafting(output, VitalityGear.AIR_COMPRESSOR), recipeId(VitalityGear.AIR_COMPRESSOR));
    }

    // The plates riveted around the valve make a loose tank; hammering seals it into an Air Tank, with Crafting's own recipe type.
    private static void airTank(Consumer<FinishedRecipe> output) {
        shaped(VitalityItems.looseAirTank())
                .pattern("PHP")
                .pattern("P P")
                .pattern(" R ")
                .key('P', IRON_PLATES)
                .key('H', IRON_HINGE)
                .key('R', IRON_RIVETS)
                .save(withCrafting(output, VitalityGear.AIR_TANK), ResourceIds.of(VitalityMod.MOD_ID, "loose_air_tank"));
        ForeignRecipeBuilder.ofType(HAMMERING_TYPE)
                .field("input", itemIngredient(VitalityItems.looseAirTank()))
                .field("hammer", HAMMERS)
                .result(itemId(VitalityItems.tank(AirTank.AIR_TANK)), 1)
                .save(withCrafting(output, VitalityGear.AIR_TANK), ResourceIds.of(VitalityMod.MOD_ID, "air_tank_from_hammering"));
    }

    private static void clothing(Consumer<FinishedRecipe> output) {
        shaped(VitalityItems.strawHat())
                .pattern("WWW")
                .pattern("WTW")
                .key('W', itemIngredient(Items.WHEAT))
                .key('T', TWINE)
                .save(withCrafting(output, VitalityGear.STRAW_HAT), recipeId(VitalityGear.STRAW_HAT));
        // A glass tube with a redstone bulb, on an Iron Plate backing.
        shaped(VitalityItems.thermometer())
                .pattern("G")
                .pattern("P")
                .pattern("R")
                .key('G', itemIngredient(Items.GLASS))
                .key('P', IRON_PLATES)
                .key('R', itemIngredient(Items.REDSTONE))
                .save(withCrafting(output, VitalityGear.THERMOMETER), recipeId(VitalityGear.THERMOMETER));
    }

    private static ForeignRecipeBuilder shaped(ItemLike result) {
        return ForeignRecipeBuilder.of(RecipeSerializer.SHAPED_RECIPE).result(itemId(result), 1);
    }

    private static ForeignRecipeBuilder shapeless(ItemLike result) {
        return ForeignRecipeBuilder.of(RecipeSerializer.SHAPELESS_RECIPE).result(itemId(result), 1);
    }

    /** Sewn with Crafting's Bone Needle, which stays in the grid and wears. */
    private static ForeignRecipeBuilder sewn(ItemLike result) {
        return ForeignRecipeBuilder.of(ToolCrafting.serializer()).tool(BONE_NEEDLE).result(itemId(result), 1);
    }

    private static Consumer<FinishedRecipe> withCrafting(Consumer<FinishedRecipe> output, VitalityGear gear) {
        return conditional(output, FeatureEnabledCondition.of(gear.itemPath), ConditionalRecipes.modLoaded(VitalityMod.CRAFTING_MOD_ID));
    }

    // Named like ConditionalRecipes.pair's recipes: waterskin_with_impactscrafting.
    private static ResourceLocation recipeId(VitalityGear gear) {
        return ResourceIds.of(VitalityMod.MOD_ID, gear.itemPath + WITH_CRAFTING_SUFFIX);
    }

    private static JsonElement crafting(String itemPath) {
        return ForeignRecipeBuilder.item(VitalityMod.CRAFTING_MOD_ID + ":" + itemPath);
    }

    private static JsonElement itemIngredient(ItemLike item) {
        return ForeignRecipeBuilder.ingredient(Ingredient.of(item));
    }

    private static Item container(WaterContainer container) {
        return VitalityItems.container(container);
    }

    private static String itemId(ItemLike item) {
        return ResourceIds.itemId(item.asItem()).toString();
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
