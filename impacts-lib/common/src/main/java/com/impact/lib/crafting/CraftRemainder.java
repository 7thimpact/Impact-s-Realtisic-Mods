package com.impact.lib.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

/** What a used ingredient leaves in its grid slot (a water bottle leaves a glass bottle). */
public record CraftRemainder(Ingredient ingredient, ItemStack result) {
    private static final String INGREDIENT_FIELD = "ingredient";
    private static final String RESULT_FIELD = "result";

    public static CraftRemainder fromJson(JsonObject json) {
        return new CraftRemainder(Ingredient.fromJson(json.get(INGREDIENT_FIELD)),
                ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, RESULT_FIELD)));
    }

    public static CraftRemainder fromNetwork(FriendlyByteBuf buffer) {
        return new CraftRemainder(Ingredient.fromNetwork(buffer), buffer.readItem());
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        ingredient.toNetwork(buffer);
        buffer.writeItem(result);
    }
}
