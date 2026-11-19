package com.impact.realisticfood.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticfood.soup.SoupRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Writes {@code impactsfood:soup} recipes: one set of ingredients and the bowl food they make. The cook time and
 * bowls per set are left to their defaults (the server config's cook time, 2 bowls).
 */
final class SoupRecipeBuilder {
    private final ItemLike result;
    private final List<Ingredient> ingredients = new ArrayList<>();

    private SoupRecipeBuilder(ItemLike result) {
        this.result = result;
    }

    static SoupRecipeBuilder soup(ItemLike result) {
        return new SoupRecipeBuilder(result);
    }

    SoupRecipeBuilder requires(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    SoupRecipeBuilder requires(ItemLike item, int count) {
        for (int copy = 0; copy < count; copy++) {
            ingredients.add(Ingredient.of(item));
        }
        return this;
    }

    void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        output.accept(new Result(recipeId, List.copyOf(ingredients), result));
    }

    private record Result(ResourceLocation id, List<Ingredient> ingredients, ItemLike result) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray ingredientArray = new JsonArray();
            ingredients.forEach(ingredient -> ingredientArray.add(ingredient.toJson()));
            json.add("ingredients", ingredientArray);
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", ResourceIds.itemId(result.asItem()).toString());
            json.add("result", resultObject);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SoupRecipes.serializer();
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
