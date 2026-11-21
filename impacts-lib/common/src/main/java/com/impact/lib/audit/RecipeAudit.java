package com.impact.lib.audit;

import com.impact.lib.ImpactsLib;
import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Which Impact mod replaced or turned off each vanilla recipe right now: the Impact mods whose files hold each
 * {@code minecraft:} recipe file. Only one Impact mod may override a given vanilla recipe; two is a conflict.
 */
public final class RecipeAudit {
    private static final String RECIPES_FOLDER = "recipes";
    private static final String JSON_SUFFIX = ".json";
    private static final String MINECRAFT_NAMESPACE = "minecraft";

    private RecipeAudit() {
    }

    /** What happened to one vanilla recipe: replaced by an Impact mod's version, or turned off by its conditions. */
    public record Entry(ResourceLocation recipeId, Set<String> overridingMods, boolean isTurnedOff) {
    }

    public static List<Entry> audit(ResourceManager resources, RecipeManager recipes) {
        Set<ResourceLocation> vanillaRecipeFiles = resources.listResources(RECIPES_FOLDER,
                location -> location.getNamespace().equals(MINECRAFT_NAMESPACE) && location.getPath().endsWith(JSON_SUFFIX)).keySet();
        List<Entry> entries = new ArrayList<>();
        for (ResourceLocation fileLocation : vanillaRecipeFiles) {
            Set<String> impactMods = impactModsWith(fileLocation);
            if (!impactMods.isEmpty()) {
                ResourceLocation recipeId = recipeIdOf(fileLocation);
                entries.add(new Entry(recipeId, impactMods, recipes.byKey(recipeId).isEmpty()));
            }
        }
        return entries;
    }

    public static List<RecipeConflict> overriddenByTwoMods(List<Entry> entries) {
        return entries.stream()
                .filter(entry -> entry.overridingMods().size() > 1)
                .map(entry -> new RecipeConflict(entry.recipeId(), entry.recipeId(), "overridden by " + entry.overridingMods()))
                .toList();
    }

    private static Set<String> impactModsWith(ResourceLocation fileLocation) {
        return LoaderPlatform.current().modsWithDataFile(fileLocation).stream()
                .filter(modId -> modId.startsWith(ImpactsLib.MOD_ID_PREFIX))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // data/minecraft/recipes/bread.json is the recipe minecraft:bread.
    private static ResourceLocation recipeIdOf(ResourceLocation fileLocation) {
        String path = fileLocation.getPath();
        String recipePath = path.substring(RECIPES_FOLDER.length() + 1, path.length() - JSON_SUFFIX.length());
        return fileLocation.withPath(recipePath);
    }
}
