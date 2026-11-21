package com.impact.realisticcrafting.forge.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import com.impact.realisticcrafting.config.OverhaulCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import java.util.Optional;

/**
 * {@code impactscrafting:overhaul_enabled}: true while a catalog category's switch is on in Crafting's common config,
 * for example {@code {"type": "impactscrafting:overhaul_enabled", "category": "wood"}}. With a {@code recipe}, also
 * false while that recipe ID is in {@code recipes.disabled} or {@code recipes.keepVanilla}. Crafting's replacement
 * recipes load under it (with their own ID), and the vanilla recipes they replace under its {@code forge:not} (with
 * the vanilla ID).
 */
public record OverhaulEnabledCondition(OverhaulCategory category, Optional<ResourceLocation> recipe) implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(CraftingMod.MOD_ID, "overhaul_enabled");
    private static final String CATEGORY_FIELD = "category";
    private static final String RECIPE_FIELD = "recipe";

    public OverhaulEnabledCondition(OverhaulCategory category, ResourceLocation recipe) {
        this(category, Optional.of(recipe));
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return category.isOn() && recipe.map(recipeId -> !isListed(recipeId.toString())).orElse(true);
    }

    private static boolean isListed(String recipeId) {
        CraftingCommonConfig config = CraftingCommonConfig.get();
        return config.disabledRecipes.get().contains(recipeId) || config.keptVanillaRecipes.get().contains(recipeId);
    }

    public static final class Serializer implements IConditionSerializer<OverhaulEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, OverhaulEnabledCondition condition) {
            json.addProperty(CATEGORY_FIELD, condition.category().configKey);
            condition.recipe().ifPresent(recipeId -> json.addProperty(RECIPE_FIELD, recipeId.toString()));
        }

        @Override
        public OverhaulEnabledCondition read(JsonObject json) {
            String categoryName = GsonHelper.getAsString(json, CATEGORY_FIELD);
            OverhaulCategory category = OverhaulCategory.byConfigKey(categoryName)
                    .orElseThrow(() -> new JsonParseException("Unknown Impact's Realistic Crafting catalog category: " + categoryName));
            Optional<ResourceLocation> recipe = json.has(RECIPE_FIELD)
                    ? ResourceIds.tryParse(GsonHelper.getAsString(json, RECIPE_FIELD))
                    : Optional.empty();
            return new OverhaulEnabledCondition(category, recipe);
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
