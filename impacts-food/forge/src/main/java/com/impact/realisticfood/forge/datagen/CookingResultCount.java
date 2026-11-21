package com.impact.realisticfood.forge.datagen;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

/**
 * A cooking recipe that gives more than one item (Loaf Dough on a campfire gives 3 Flatbread). Vanilla's builder writes
 * the result as a plain ID; Forge also reads an object with a count, which this writes instead.
 */
record CookingResultCount(FinishedRecipe cookingRecipe, ItemLike result, int count) implements FinishedRecipe {
    @Override
    public void serializeRecipeData(JsonObject json) {
        cookingRecipe.serializeRecipeData(json);
        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("item", ResourceIds.itemId(result.asItem()).toString());
        resultObject.addProperty("count", count);
        json.add("result", resultObject);
    }

    @Override
    public ResourceLocation getId() {
        return cookingRecipe.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return cookingRecipe.getType();
    }

    @Override
    public JsonObject serializeAdvancement() {
        return cookingRecipe.serializeAdvancement();
    }

    @Override
    public ResourceLocation getAdvancementId() {
        return cookingRecipe.getAdvancementId();
    }
}
