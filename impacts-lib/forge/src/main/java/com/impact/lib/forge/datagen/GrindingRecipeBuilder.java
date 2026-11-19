package com.impact.lib.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.lib.grinding.GrinderTier;
import com.impact.lib.grinding.Grinding;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Writes {@code impactslib:grinding} recipes in data generation: one or two ingredients, the result, the lowest grinder
 * tier that can grind them, and optional byproducts. Leave out the grind time to let the grinding stick decide it.
 */
public final class GrindingRecipeBuilder {
    private static final float ALWAYS = 1.0F;

    private final ItemLike result;
    private final int resultCount;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<JsonObject> byproducts = new ArrayList<>();
    private GrinderTier minimumTier = GrinderTier.WOOD;
    private Integer grindTicks;

    private GrindingRecipeBuilder(ItemLike result, int resultCount) {
        this.result = result;
        this.resultCount = resultCount;
    }

    public static GrindingRecipeBuilder of(ItemLike result, int resultCount) {
        return new GrindingRecipeBuilder(result, resultCount);
    }

    public GrindingRecipeBuilder requires(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public GrindingRecipeBuilder requires(ItemLike item) {
        return requires(Ingredient.of(item));
    }

    public GrindingRecipeBuilder minimumTier(GrinderTier tier) {
        minimumTier = tier;
        return this;
    }

    public GrindingRecipeBuilder grindTicks(int ticks) {
        grindTicks = ticks;
        return this;
    }

    public GrindingRecipeBuilder byproduct(ItemLike item, int count, float chance) {
        JsonObject byproduct = itemJson(item, count);
        if (chance < ALWAYS) {
            byproduct.addProperty("chance", chance);
        }
        byproducts.add(byproduct);
        return this;
    }

    public void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        JsonObject json = new JsonObject();
        JsonArray ingredientArray = new JsonArray();
        ingredients.forEach(ingredient -> ingredientArray.add(ingredient.toJson()));
        json.add("ingredients", ingredientArray);
        json.add("result", itemJson(result, resultCount));
        json.addProperty("min_tier", minimumTier.getSerializedName());
        if (grindTicks != null) {
            json.addProperty("grind_ticks", grindTicks);
        }
        if (!byproducts.isEmpty()) {
            JsonArray byproductArray = new JsonArray();
            byproducts.forEach(byproductArray::add);
            json.add("byproducts", byproductArray);
        }
        output.accept(new Result(recipeId, json));
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
            return Grinding.serializer();
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
