package com.impact.realisticcrafting.forge.datagen;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/** Writes {@code impactscrafting:hammering} recipes. They have no recipe book entry, so no unlock advancement. */
final class HammeringRecipeBuilder {
    private final Ingredient input;
    private final Ingredient hammer;
    private final ItemLike result;

    private HammeringRecipeBuilder(Ingredient input, Ingredient hammer, ItemLike result) {
        this.input = input;
        this.hammer = hammer;
        this.result = result;
    }

    static HammeringRecipeBuilder of(ItemLike input, Ingredient hammer, ItemLike result) {
        return of(Ingredient.of(input), hammer, result);
    }

    static HammeringRecipeBuilder of(Ingredient input, Ingredient hammer, ItemLike result) {
        return new HammeringRecipeBuilder(input, hammer, result);
    }

    void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        JsonObject fields = new JsonObject();
        fields.add("input", input.toJson());
        fields.add("hammer", hammer.toJson());
        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("item", ResourceIds.itemId(result.asItem()).toString());
        fields.add("result", resultObject);
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
            return CraftingRecipeTypes.hammeringSerializer();
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
