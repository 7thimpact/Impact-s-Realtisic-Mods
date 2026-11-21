package com.impact.realisticcrafting.forge.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Consumer;

/** A vanilla recipe builder's recipe with NBT on its result (a per-wood part carrying its wood). Forge reads the "nbt" string. */
final class NbtResultRecipe {
    private static final String RESULT_KEY = "result";
    private static final String NBT_KEY = "nbt";

    private final RecipeBuilder builder;
    private final CompoundTag resultNbt;

    private NbtResultRecipe(RecipeBuilder builder, CompoundTag resultNbt) {
        this.builder = builder;
        this.resultNbt = resultNbt;
    }

    static NbtResultRecipe of(RecipeBuilder builder, CompoundTag resultNbt) {
        return new NbtResultRecipe(builder, resultNbt);
    }

    void save(Consumer<FinishedRecipe> output, ResourceLocation recipeId) {
        builder.save(recipe -> output.accept(new WithResultNbt(recipe, resultNbt)), recipeId);
    }

    private record WithResultNbt(FinishedRecipe delegate, CompoundTag resultNbt) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            delegate.serializeRecipeData(json);
            json.getAsJsonObject(RESULT_KEY).addProperty(NBT_KEY, resultNbt.toString());
        }

        @Override
        public ResourceLocation getId() {
            return delegate.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return delegate.getType();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return delegate.serializeAdvancement();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return delegate.getAdvancementId();
        }
    }
}
