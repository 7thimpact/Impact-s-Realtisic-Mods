package com.impact.realisticcrafting.forge.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
import com.impact.realisticcrafting.recipe.VanillaRecipeReplacements;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Writes a switched-off copy of each vanilla recipe Crafting replaces ({@link VanillaRecipeReplacements}): vanilla's
 * own file, under the condition "not {@code overhaul_enabled}" for its category and ID, so it loads again when the
 * category is off or the ID is in {@code recipes.keepVanilla}. The recipe manager also drops these recipes in code,
 * since a pack loaded after Crafting's (Forge's own copies) would otherwise win.
 */
final class VanillaRecipeOverrides implements DataProvider {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String RECIPES_FOLDER = "recipes/";
    private static final String JSON_EXTENSION = ".json";
    private static final String CONDITIONS_KEY = "conditions";

    private final PackOutput output;
    private final ExistingFileHelper existingFiles;
    VanillaRecipeOverrides(PackOutput output, ExistingFileHelper existingFiles) {
        this.output = output;
        this.existingFiles = existingFiles;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path recipesFolder = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(MINECRAFT_NAMESPACE).resolve(RECIPES_FOLDER);
        CompletableFuture<?>[] writes = VanillaRecipeReplacements.all().entrySet().stream()
                .filter(entry -> hasVanillaRecipe(entry.getKey().getPath()))
                .map(entry -> DataProvider.saveStable(cache, conditionalCopy(entry.getKey().getPath(), entry.getValue()),
                        recipesFolder.resolve(entry.getKey().getPath() + JSON_EXTENSION)))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(writes);
    }

    private JsonObject conditionalCopy(String recipeName, OverhaulCategory category) {
        ResourceLocation recipeId = ResourceIds.of(MINECRAFT_NAMESPACE, recipeName);
        JsonObject recipe = readVanillaRecipe(recipeName);
        ICondition replaced = new OverhaulEnabledCondition(category, recipeId);
        recipe.add(CONDITIONS_KEY, CraftingHelper.serialize(new ICondition[]{new NotCondition(replaced)}));
        return recipe;
    }

    // The list is built from vanilla's block families, so a family variant without a recipe of its own name is skipped.
    private boolean hasVanillaRecipe(String recipeName) {
        return existingFiles.exists(ResourceIds.of(MINECRAFT_NAMESPACE, RECIPES_FOLDER + recipeName + JSON_EXTENSION), PackType.SERVER_DATA);
    }

    private JsonObject readVanillaRecipe(String recipeName) {
        ResourceLocation file = ResourceIds.of(MINECRAFT_NAMESPACE, RECIPES_FOLDER + recipeName + JSON_EXTENSION);
        try (Reader reader = existingFiles.getResource(file, PackType.SERVER_DATA).openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException missing) {
            throw new UncheckedIOException("Vanilla has no recipe " + file, missing);
        }
    }

    @Override
    public String getName() {
        return "Impact's Realistic Crafting vanilla recipe overrides";
    }
}
