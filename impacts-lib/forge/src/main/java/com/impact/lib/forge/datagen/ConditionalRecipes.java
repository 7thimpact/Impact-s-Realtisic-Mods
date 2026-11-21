package com.impact.lib.forge.datagen;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.List;
import java.util.function.Consumer;

/**
 * Recipes that load only under conditions. Recipes that change when another Impact mod is installed ship in pairs:
 * one gated by {@code forge:mod_loaded <mod>}, one by {@code forge:not} of it (Lib's spec, "Integration API").
 */
public final class ConditionalRecipes {
    private static final String CONDITIONS_KEY = "conditions";
    private static final String WITH_MOD_SEPARATOR = "_with_";

    private ConditionalRecipes() {
    }

    public static ICondition modLoaded(String modId) {
        return new ModLoadedCondition(modId);
    }

    public static ICondition modNotLoaded(String modId) {
        return new NotCondition(new ModLoadedCondition(modId));
    }

    /** The same recipe, loaded only when every condition holds. Its unlock advancement gets the same conditions. */
    public static FinishedRecipe withConditions(FinishedRecipe recipe, ICondition... conditions) {
        return new ConditionalFinishedRecipe(recipe, recipe.getId(), List.of(conditions));
    }

    /**
     * Writes the recipe used without the mod under its own ID, and the one used with the mod under
     * {@code <id>_with_<mod>}, so exactly one of the two loads.
     */
    public static void pair(Consumer<FinishedRecipe> output, String modId, FinishedRecipe withoutMod, FinishedRecipe withMod) {
        output.accept(withConditions(withoutMod, modNotLoaded(modId)));
        ResourceLocation withModId = ResourceIds.of(withMod.getId().getNamespace(), withoutMod.getId().getPath() + WITH_MOD_SEPARATOR + modId);
        output.accept(new ConditionalFinishedRecipe(withMod, withModId, List.of(modLoaded(modId))));
    }

    private record ConditionalFinishedRecipe(FinishedRecipe delegate, ResourceLocation id, List<ICondition> conditions) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            delegate.serializeRecipeData(json);
            json.add(CONDITIONS_KEY, CraftingHelper.serialize(conditions.toArray(ICondition[]::new)));
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return delegate.getType();
        }

        @Override
        public JsonObject serializeAdvancement() {
            JsonObject advancement = delegate.serializeAdvancement();
            if (advancement != null) {
                advancement.add(CONDITIONS_KEY, CraftingHelper.serialize(conditions.toArray(ICondition[]::new)));
            }
            return advancement;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            ResourceLocation delegateAdvancementId = delegate.getAdvancementId();
            if (delegateAdvancementId == null) {
                return null;
            }
            String delegatePath = delegate.getId().getPath();
            String advancementPath = delegateAdvancementId.getPath().replace(delegatePath, id.getPath());
            return ResourceIds.of(delegateAdvancementId.getNamespace(), advancementPath);
        }
    }
}
