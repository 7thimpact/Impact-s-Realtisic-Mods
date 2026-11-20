package com.impact.realisticcrafting.forge.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.impact.lib.ResourceIds;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Switches off the vanilla recipes Crafting replaces. Each is vanilla's own recipe file, copied as it is, under the
 * condition "not {@code overhaul_enabled}" for its category and ID: it loads again when the category is off or the ID
 * is in {@code recipes.keepVanilla}.
 */
final class VanillaRecipeOverrides implements DataProvider {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String RECIPES_FOLDER = "recipes/";
    private static final String JSON_EXTENSION = ".json";
    private static final String CONDITIONS_KEY = "conditions";

    private final PackOutput output;
    private final ExistingFileHelper existingFiles;
    private final Map<String, OverhaulCategory> replacedRecipes = new LinkedHashMap<>();

    VanillaRecipeOverrides(PackOutput output, ExistingFileHelper existingFiles) {
        this.output = output;
        this.existingFiles = existingFiles;
        replace(OverhaulCategory.WOOD, "crafting_table", "stick");
        WoodData.VANILLA_WOODS.forEach(wood -> replace(OverhaulCategory.WOOD, wood.getPath() + "_planks"));
    }

    private void replace(OverhaulCategory category, String... vanillaRecipeNames) {
        for (String recipeName : vanillaRecipeNames) {
            replacedRecipes.put(recipeName, category);
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path recipesFolder = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(MINECRAFT_NAMESPACE).resolve(RECIPES_FOLDER);
        CompletableFuture<?>[] writes = replacedRecipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(cache, conditionalCopy(entry.getKey(), entry.getValue()),
                        recipesFolder.resolve(entry.getKey() + JSON_EXTENSION)))
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
