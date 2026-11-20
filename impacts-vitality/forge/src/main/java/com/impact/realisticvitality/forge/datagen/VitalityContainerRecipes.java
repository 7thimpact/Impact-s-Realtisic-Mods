package com.impact.realisticvitality.forge.datagen;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.forge.condition.FeatureEnabledCondition;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityRecipeSerializers;
import com.impact.realisticvitality.thirst.ContainerContents;
import com.impact.realisticvitality.thirst.WaterContainer;
import com.impact.realisticvitality.thirst.WaterPurity;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.function.Consumer;

/**
 * The drink containers' recipes without Crafting installed (the versions made from Crafting's parts arrive with
 * Crafting), the Wooden Cup purifying on a campfire, and filling containers from bottles in a grid.
 */
final class VitalityContainerRecipes {
    private static final float CUP_PURIFYING_EXPERIENCE = 0.1F;
    private static final int CUP_PURIFYING_TICKS = 300;

    private VitalityContainerRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        gearRecipes(output);
        Consumer<FinishedRecipe> thirstOutput = conditional(output, FeatureEnabledCondition.of(VitalityNeed.THIRST.configKey));
        cupPurifying(thirstOutput, WaterPurity.UNCLEAN);
        cupPurifying(thirstOutput, WaterPurity.SALT);
        SpecialRecipeBuilder.special(VitalityRecipeSerializers.containerFilling()).save(thirstOutput, VitalityMod.MOD_ID + ":container_filling");
    }

    private static void gearRecipes(Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, container(WaterContainer.WOODEN_CUP))
                .requires(Items.BOWL)
                .requires(Items.STICK)
                .unlockedBy("has_bowl", has(Items.BOWL))
                .save(gearOutput(output, WaterContainer.WOODEN_CUP));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, container(WaterContainer.WATERSKIN))
                .pattern(" S ")
                .pattern("L L")
                .pattern(" L ")
                .define('S', Items.STRING)
                .define('L', Items.LEATHER)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(gearOutput(output, WaterContainer.WATERSKIN));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, container(WaterContainer.CANTEEN))
                .pattern(" L ")
                .pattern("I I")
                .pattern(" I ")
                .define('L', Items.LEATHER)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(gearOutput(output, WaterContainer.CANTEEN));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, container(WaterContainer.INSULATED_FLASK))
                .pattern(" G ")
                .pattern("WCW")
                .pattern(" G ")
                .define('G', Items.GOLD_INGOT)
                .define('W', ItemTags.WOOL)
                .define('C', container(WaterContainer.CANTEEN))
                .unlockedBy("has_canteen", has(container(WaterContainer.CANTEEN)))
                .save(gearOutput(output, WaterContainer.INSULATED_FLASK));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, container(WaterContainer.TIDEBOUND_CANTEEN))
                .pattern("PHP")
                .pattern("PCP")
                .define('P', Items.PRISMARINE_SHARD)
                .define('H', Items.HEART_OF_THE_SEA)
                .define('C', container(WaterContainer.CANTEEN))
                .unlockedBy("has_heart_of_the_sea", has(Items.HEART_OF_THE_SEA))
                .save(gearOutput(output, WaterContainer.TIDEBOUND_CANTEEN));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, container(WaterContainer.HYDRATION_PACK))
                .pattern("LLL")
                .pattern("DCD")
                .pattern("L L")
                .define('L', Items.LEATHER)
                .define('D', Items.DIAMOND)
                .define('C', container(WaterContainer.CANTEEN))
                .unlockedBy("has_canteen", has(container(WaterContainer.CANTEEN)))
                .save(gearOutput(output, WaterContainer.HYDRATION_PACK));
    }

    // The Tidebound Canteen needs no Crafting parts, so its recipe stays the same with Crafting installed.
    private static Consumer<FinishedRecipe> gearOutput(Consumer<FinishedRecipe> output, WaterContainer container) {
        FeatureEnabledCondition gearSwitch = FeatureEnabledCondition.of(container.gear.itemPath);
        if (container == WaterContainer.TIDEBOUND_CANTEEN) {
            return conditional(output, gearSwitch);
        }
        return conditional(output, gearSwitch, ConditionalRecipes.modNotLoaded(VitalityMod.CRAFTING_MOD_ID));
    }

    private static void cupPurifying(Consumer<FinishedRecipe> output, WaterPurity purity) {
        ItemStack filled = filledCup(purity);
        ItemStack purified = filledCup(WaterPurity.PURIFIED);
        ResourceLocation id = ResourceIds.of(VitalityMod.MOD_ID, "wooden_cup_purifying_" + purity.serializedName);
        output.accept(new NbtCampfireRecipe(id, filled, purified));
    }

    private static ItemStack filledCup(WaterPurity purity) {
        ItemStack cup = new ItemStack(container(WaterContainer.WOODEN_CUP));
        ContainerContents.water(WaterContainer.WOODEN_CUP.defaultCapacity, purity).writeTo(cup);
        return cup;
    }

    private static Item container(WaterContainer container) {
        return VitalityItems.container(container);
    }

    private static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }

    /** A campfire recipe whose ingredient and result both carry NBT: a filled cup in, the same cup with purified water out. */
    private record NbtCampfireRecipe(ResourceLocation id, ItemStack input, ItemStack result) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("category", "misc");
            json.add("ingredient", PartialNBTIngredient.of(input.getItem(), input.getTag()).toJson());
            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", ResourceIds.itemId(result.getItem()).toString());
            resultJson.addProperty("nbt", result.getTag().toString());
            json.add("result", resultJson);
            json.addProperty("experience", CUP_PURIFYING_EXPERIENCE);
            json.addProperty("cookingtime", CUP_PURIFYING_TICKS);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
