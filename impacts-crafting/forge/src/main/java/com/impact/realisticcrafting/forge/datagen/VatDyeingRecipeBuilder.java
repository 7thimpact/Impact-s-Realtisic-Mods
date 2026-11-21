package com.impact.realisticcrafting.forge.datagen;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Writes {@code impactscrafting:vat_dyeing} recipes: an input, results keyed by dye color (or {@code none}), and optional dip ticks. */
final class VatDyeingRecipeBuilder {
    private static final int DEFAULT_DIP_TICKS = 10;

    private final Ingredient input;
    private final Map<String, ItemLike> results = new LinkedHashMap<>();
    private int dipTicks = DEFAULT_DIP_TICKS;

    private VatDyeingRecipeBuilder(Ingredient input) {
        this.input = input;
    }

    static VatDyeingRecipeBuilder of(Ingredient input) {
        return new VatDyeingRecipeBuilder(input);
    }

    VatDyeingRecipeBuilder result(String colorKey, ItemLike result) {
        results.put(colorKey, result);
        return this;
    }

    VatDyeingRecipeBuilder dipTicks(int ticks) {
        dipTicks = ticks;
        return this;
    }

    void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        JsonObject fields = new JsonObject();
        fields.add("input", input.toJson());
        JsonObject resultObject = new JsonObject();
        results.forEach((colorKey, result) -> {
            JsonObject item = new JsonObject();
            item.addProperty("item", ResourceIds.itemId(result.asItem()).toString());
            resultObject.add(colorKey, item);
        });
        fields.add("results", resultObject);
        if (dipTicks != DEFAULT_DIP_TICKS) {
            fields.addProperty("dip_ticks", dipTicks);
        }
        output.accept(new Result(recipeId, fields));
    }

    private record Result(ResourceLocation id, JsonObject fields) implements FinishedRecipe {
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
            return CraftingRecipeTypes.vatDyeingSerializer();
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
