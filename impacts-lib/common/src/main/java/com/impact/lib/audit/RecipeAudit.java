package com.impact.lib.audit;

import com.impact.lib.ImpactsLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Which Impact mod replaced or turned off each vanilla recipe right now, read from the data packs that provide each
 * {@code minecraft:} recipe file. Only one Impact mod may override a given vanilla recipe; two is a conflict.
 */
public final class RecipeAudit {
    private static final String RECIPES_FOLDER = "recipes";
    private static final String JSON_SUFFIX = ".json";
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String MOD_PACK_PREFIX = "mod:";

    private RecipeAudit() {
    }

    /** What happened to one vanilla recipe: replaced by an Impact mod's version, or turned off by its conditions. */
    public record Entry(ResourceLocation recipeId, Set<String> overridingMods, boolean isTurnedOff) {
    }

    public static List<Entry> audit(ResourceManager resources, RecipeManager recipes) {
        Map<ResourceLocation, List<Resource>> vanillaRecipeFiles = resources.listResourceStacks(RECIPES_FOLDER,
                location -> location.getNamespace().equals(MINECRAFT_NAMESPACE) && location.getPath().endsWith(JSON_SUFFIX));
        List<Entry> entries = new ArrayList<>();
        vanillaRecipeFiles.forEach((fileLocation, stack) -> {
            Set<String> impactMods = impactModsIn(stack);
            if (!impactMods.isEmpty()) {
                ResourceLocation recipeId = recipeIdOf(fileLocation);
                entries.add(new Entry(recipeId, impactMods, recipes.byKey(recipeId).isEmpty()));
            }
        });
        return entries;
    }

    public static List<RecipeConflict> overriddenByTwoMods(List<Entry> entries) {
        return entries.stream()
                .filter(entry -> entry.overridingMods().size() > 1)
                .map(entry -> new RecipeConflict(entry.recipeId(), entry.recipeId(), "overridden by " + entry.overridingMods()))
                .toList();
    }

    private static Set<String> impactModsIn(List<Resource> stack) {
        Set<String> impactMods = new LinkedHashSet<>();
        for (Resource resource : stack) {
            String packId = resource.sourcePackId();
            String modId = packId.startsWith(MOD_PACK_PREFIX) ? packId.substring(MOD_PACK_PREFIX.length()) : packId;
            if (modId.startsWith(ImpactsLib.MOD_ID_PREFIX)) {
                impactMods.add(modId);
            }
        }
        return impactMods;
    }

    // data/minecraft/recipes/bread.json is the recipe minecraft:bread.
    private static ResourceLocation recipeIdOf(ResourceLocation fileLocation) {
        String path = fileLocation.getPath();
        String recipePath = path.substring(RECIPES_FOLDER.length() + 1, path.length() - JSON_SUFFIX.length());
        return fileLocation.withPath(recipePath);
    }
}
