package com.impact.lib.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Writes a recipe that uses another Impact mod's items. That mod isn't loaded while this one generates its data, so its
 * items and tags are written by ID ({@link #item}, {@link #tag}) and the recipe's JSON is built field by field. Gate it
 * with {@link ConditionalRecipes#withConditions} and {@code forge:mod_loaded} so it only loads alongside that mod.
 * Shapeless recipes take {@link #ingredient}; shaped ones {@link #pattern} and {@link #key}; Lib's tool crafting adds
 * {@link #tool} and any other field through {@link #field}.
 */
public final class ForeignRecipeBuilder {
    private static final String ITEM_KEY = "item";
    private static final String TAG_KEY = "tag";

    private final RecipeSerializer<?> serializer;
    private final JsonArray ingredients = new JsonArray();
    private final JsonArray pattern = new JsonArray();
    private final JsonObject key = new JsonObject();
    private final JsonArray tools = new JsonArray();
    private final Map<String, JsonElement> extraFields = new LinkedHashMap<>();
    private JsonObject result;

    private ForeignRecipeBuilder(RecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    /** A recipe of the serializer's type (vanilla's shaped or shapeless, Lib's tool crafting, a mod's own type). */
    public static ForeignRecipeBuilder of(RecipeSerializer<?> serializer) {
        return new ForeignRecipeBuilder(serializer);
    }

    /** An item ingredient by ID, for items of a mod that isn't loaded here. */
    public static JsonElement item(String itemId) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty(ITEM_KEY, itemId);
        return ingredient;
    }

    /** A tag ingredient by ID. */
    public static JsonElement tag(String tagId) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty(TAG_KEY, tagId);
        return ingredient;
    }

    /** An ingredient of this mod's own (or vanilla's) items. */
    public static JsonElement ingredient(Ingredient ingredient) {
        return ingredient.toJson();
    }

    public ForeignRecipeBuilder ingredient(JsonElement ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public ForeignRecipeBuilder ingredient(JsonElement ingredient, int count) {
        for (int copy = 0; copy < count; copy++) {
            ingredients.add(ingredient);
        }
        return this;
    }

    public ForeignRecipeBuilder pattern(String row) {
        pattern.add(row);
        return this;
    }

    public ForeignRecipeBuilder key(char symbol, JsonElement ingredient) {
        key.add(String.valueOf(symbol), ingredient);
        return this;
    }

    /** A tool that stays in the grid and wears (Lib's tool crafting). */
    public ForeignRecipeBuilder tool(JsonElement tool) {
        tools.add(tool);
        return this;
    }

    public ForeignRecipeBuilder result(String itemId, int count) {
        result = new JsonObject();
        result.addProperty(ITEM_KEY, itemId);
        if (count > 1) {
            result.addProperty("count", count);
        }
        return this;
    }

    /** Any other field the recipe type reads ({@code copy_nbt_from}, {@code category}, ...). */
    public ForeignRecipeBuilder field(String name, JsonElement value) {
        extraFields.put(name, value);
        return this;
    }

    public FinishedRecipe build(ResourceLocation recipeId) {
        if (result == null) {
            throw new IllegalStateException("Recipe " + recipeId + " has no result");
        }
        JsonObject fields = new JsonObject();
        if (!ingredients.isEmpty()) {
            fields.add("ingredients", ingredients);
        }
        if (!pattern.isEmpty()) {
            fields.add("pattern", pattern);
            fields.add("key", key);
        }
        if (!tools.isEmpty()) {
            fields.add("tools", tools);
        }
        fields.add("result", result);
        extraFields.forEach(fields::add);
        return new Result(recipeId, serializer, fields);
    }

    public void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        output.accept(build(recipeId));
    }

    private record Result(ResourceLocation id, RecipeSerializer<?> serializer, JsonObject fields) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            fields.entrySet().forEach(field -> json.add(field.getKey(), field.getValue()));
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        // Recipes for another mod's items carry no unlock advancement: the advancement would name items this mod can't see.
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
