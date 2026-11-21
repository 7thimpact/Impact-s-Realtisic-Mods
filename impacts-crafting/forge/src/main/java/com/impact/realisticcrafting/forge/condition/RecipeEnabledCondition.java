package com.impact.realisticcrafting.forge.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * {@code impactscrafting:recipe_enabled}: true unless the recipe ID is in {@code recipes.disabled}. Shared parts
 * (Trimmed Planks, Shaped Stone, Twine, ...) load under it, since every catalog category uses them.
 */
public record RecipeEnabledCondition(ResourceLocation recipe) implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(CraftingMod.MOD_ID, "recipe_enabled");
    private static final String RECIPE_FIELD = "recipe";

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return !CraftingCommonConfig.get().disabledRecipes.get().contains(recipe.toString());
    }

    public static final class Serializer implements IConditionSerializer<RecipeEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, RecipeEnabledCondition condition) {
            json.addProperty(RECIPE_FIELD, condition.recipe().toString());
        }

        @Override
        public RecipeEnabledCondition read(JsonObject json) {
            String recipeId = GsonHelper.getAsString(json, RECIPE_FIELD);
            return new RecipeEnabledCondition(ResourceIds.tryParse(recipeId)
                    .orElseThrow(() -> new JsonParseException("Not a recipe ID: " + recipeId)));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
