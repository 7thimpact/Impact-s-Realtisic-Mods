package com.impact.realisticfood.forge.datagen;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticfood.boiling.BoilingRecipes;
import com.impact.realisticfood.boiling.RecipeVessel;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/** Writes {@code impactsfood:boiling} recipes. Pots aren't in the recipe book, so these have no unlock advancement. */
final class BoilingRecipeBuilder {
    private final Ingredient input;
    private final ItemLike result;
    private final int boilTicks;
    private RecipeVessel vessel = RecipeVessel.ANY;
    private int resultCount = 1;

    private BoilingRecipeBuilder(Ingredient input, ItemLike result, int boilTicks) {
        this.input = input;
        this.result = result;
        this.boilTicks = boilTicks;
    }

    static BoilingRecipeBuilder boiling(Ingredient input, ItemLike result, int boilTicks) {
        return new BoilingRecipeBuilder(input, result, boilTicks);
    }

    BoilingRecipeBuilder resultCount(int count) {
        resultCount = count;
        return this;
    }

    BoilingRecipeBuilder vessel(RecipeVessel newVessel) {
        vessel = newVessel;
        return this;
    }

    void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        output.accept(new Result(recipeId, input, result, resultCount, boilTicks, vessel));
    }

    private record Result(ResourceLocation id, Ingredient input, ItemLike result, int resultCount, int boilTicks, RecipeVessel vessel)
            implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", ResourceIds.itemId(result.asItem()).toString());
            if (resultCount != 1) {
                resultObject.addProperty("count", resultCount);
            }
            json.add("result", resultObject);
            json.addProperty("boil_ticks", boilTicks);
            json.addProperty("vessel", vessel.jsonName);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return BoilingRecipes.serializer();
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
