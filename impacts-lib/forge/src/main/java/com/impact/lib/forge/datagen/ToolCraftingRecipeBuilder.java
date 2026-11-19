package com.impact.lib.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.ToolCrafting;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Writes {@code impactslib:tool_crafting} recipes in data generation, shaped or shapeless. These recipes stay out of
 * the recipe book, so they have no unlock advancement.
 */
public final class ToolCraftingRecipeBuilder {
    private static final int DEFAULT_TOOL_DAMAGE = 1;
    private static final float ALWAYS = 1.0F;

    private final ItemLike result;
    private final int resultCount;
    private final List<String> pattern = new ArrayList<>();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<Ingredient> tools = new ArrayList<>();
    private final List<JsonObject> byproducts = new ArrayList<>();
    private final List<JsonObject> remainders = new ArrayList<>();
    private int toolDamage = DEFAULT_TOOL_DAMAGE;
    private Integer craftTicks;
    private Ingredient nbtSource;

    private ToolCraftingRecipeBuilder(ItemLike result, int resultCount) {
        this.result = result;
        this.resultCount = resultCount;
    }

    public static ToolCraftingRecipeBuilder of(ItemLike result, int resultCount) {
        return new ToolCraftingRecipeBuilder(result, resultCount);
    }

    public static ToolCraftingRecipeBuilder of(ItemLike result) {
        return of(result, 1);
    }

    /** A shapeless ingredient. A recipe with no pattern lines is shapeless. */
    public ToolCraftingRecipeBuilder requires(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public ToolCraftingRecipeBuilder requires(ItemLike item) {
        return requires(Ingredient.of(item));
    }

    public ToolCraftingRecipeBuilder pattern(String line) {
        pattern.add(line);
        return this;
    }

    public ToolCraftingRecipeBuilder define(char symbol, Ingredient ingredient) {
        key.put(symbol, ingredient);
        return this;
    }

    public ToolCraftingRecipeBuilder define(char symbol, ItemLike item) {
        return define(symbol, Ingredient.of(item));
    }

    public ToolCraftingRecipeBuilder tool(Ingredient tool) {
        tools.add(tool);
        return this;
    }

    public ToolCraftingRecipeBuilder toolDamage(int damage) {
        toolDamage = damage;
        return this;
    }

    public ToolCraftingRecipeBuilder byproduct(ItemLike item, int count, float chance) {
        JsonObject byproduct = itemJson(item, count);
        if (chance < ALWAYS) {
            byproduct.addProperty("chance", chance);
        }
        byproducts.add(byproduct);
        return this;
    }

    public ToolCraftingRecipeBuilder remainder(Ingredient ingredient, ItemLike leftBehind) {
        JsonObject remainder = new JsonObject();
        remainder.add("ingredient", ingredient.toJson());
        remainder.add("result", itemJson(leftBehind, 1));
        remainders.add(remainder);
        return this;
    }

    public ToolCraftingRecipeBuilder craftTicks(int ticks) {
        craftTicks = ticks;
        return this;
    }

    public ToolCraftingRecipeBuilder copyNbtFrom(Ingredient source) {
        nbtSource = source;
        return this;
    }

    public void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        output.accept(new Result(recipeId, toJson()));
    }

    private JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (pattern.isEmpty()) {
            JsonArray ingredientArray = new JsonArray();
            ingredients.forEach(ingredient -> ingredientArray.add(ingredient.toJson()));
            json.add("ingredients", ingredientArray);
        } else {
            JsonArray patternArray = new JsonArray();
            pattern.forEach(patternArray::add);
            json.add("pattern", patternArray);
            JsonObject keyObject = new JsonObject();
            key.forEach((symbol, ingredient) -> keyObject.add(String.valueOf(symbol), ingredient.toJson()));
            json.add("key", keyObject);
        }
        JsonArray toolArray = new JsonArray();
        tools.forEach(tool -> toolArray.add(tool.toJson()));
        json.add("tools", toolArray);
        if (toolDamage != DEFAULT_TOOL_DAMAGE) {
            json.addProperty("tool_damage", toolDamage);
        }
        json.add("result", itemJson(result, resultCount));
        addArrayIfAny(json, "byproducts", byproducts);
        addArrayIfAny(json, "remainders", remainders);
        if (craftTicks != null) {
            json.addProperty("craft_ticks", craftTicks);
        }
        if (nbtSource != null) {
            json.add("copy_nbt_from", nbtSource.toJson());
        }
        return json;
    }

    private static void addArrayIfAny(JsonObject json, String field, List<JsonObject> entries) {
        if (entries.isEmpty()) {
            return;
        }
        JsonArray array = new JsonArray();
        entries.forEach(array::add);
        json.add(field, array);
    }

    private static JsonObject itemJson(ItemLike item, int count) {
        JsonObject itemObject = new JsonObject();
        itemObject.addProperty("item", ResourceIds.itemId(item.asItem()).toString());
        if (count != 1) {
            itemObject.addProperty("count", count);
        }
        return itemObject;
    }

    private record Result(ResourceLocation id, JsonObject recipeFields) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            recipeFields.entrySet().forEach(field -> json.add(field.getKey(), field.getValue()));
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ToolCrafting.serializer();
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
